package restaurant.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import restaurant.model.Customer;
import restaurant.model.CustomerType;
import restaurant.model.HappyHour;
import restaurant.model.Restaurant;
import restaurant.staff.Cashier;
import restaurant.staff.Chef;
import restaurant.staff.Host;
import restaurant.staff.ManagedStaff;
import restaurant.staff.StaffTask;
import restaurant.staff.Waiter;
import restaurant.ui.ConsoleUI;
import restaurant.ui.ManagerCommand;

/**
 * Control class: runs the six steps of each turn. It asks the UI for decisions, asks staff tasks to
 * validate and execute themselves, and asks the restaurant to apply the turn-end rules. It never
 * inspects what kind of customer or task it is handling.
 */
public class GameController {
    private final GameConfig config;
    private final Restaurant restaurant;
    private final ConsoleUI ui;
    /** Staff the manager directs, in the order they act in step 3. */
    private final List<ManagedStaff> roster = new ArrayList<>();
    private final Cashier cashier = new Cashier();
    private int nextCustomerId = 1;

    public GameController(GameConfig config, ConsoleUI ui) {
        this.config = config;
        this.ui = ui;
        this.restaurant = config.newRestaurant();
        roster.add(new Host());
        roster.add(new Waiter());
        roster.add(new Chef());
    }

    public Restaurant getRestaurant() { return restaurant; }

    /** Plays all turns and returns the result. Throws GameQuitException if the manager quits. */
    public GameResult run() {
        ui.showWelcome(config);
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        GameResult result = config.evaluate(restaurant);       // step 6
        ui.showResult(config, result);
        return result;
    }

    private void playTurn(int turn) {
        ui.showTurnHeader(turn, config.getMaxTurns());
        List<String> events = new ArrayList<>();

        arrivals(turn, events);                                   // step 1
        ui.showEvents("Arrivals:", events);
        events.clear();
        if (restaurant.getHappyHour().isActive()) {                // second Happy Hour turn
            ui.showEvents("Happy Hour continues:", happyHourRecovery());
        }
        ui.showBoard(restaurant, turn, config.getMaxTurns(), happyHourNotice(), describeAvailable(new TurnPlan()));
        offerHappyHour();

        TurnPlan plan = collectChoices();                         // step 2

        events.clear();
        for (StaffTask task : plan.getTasks()) {                  // step 3
            events.add(task.execute(restaurant, turn));
        }
        ui.showEvents("Kitchen and serving:", events);

        events.clear();
        for (Customer c : restaurant.finishEating(turn)) {        // step 4
            events.add(c + " finishes eating and is READY_TO_PAY.");
        }
        cashier.collectPayment(restaurant, turn).ifPresent(events::add);
        ui.showEvents("Eating and payment:", events);

        events.clear();
        for (Customer c : restaurant.applyWaitingLoss()) {        // step 5
            events.add(c + " lost patience and leaves without paying (order cancelled, table freed).");
        }
        restaurant.endTurn();
        ui.showEvents("Waiting and abandonment:", events);
        ui.showTurnSummary(restaurant, turn);
    }

    // ---- step 1 ----
    private void arrivals(int turn, List<String> events) {
        Optional<CustomerType> type = config.getSchedule().typeAt(turn);
        if (type.isPresent()) {
            Customer c = type.get().create(nextCustomerId++, turn);
            restaurant.admit(c);
            events.add(c + " arrives and joins the waiting queue (satisfaction " + c.getSatisfaction() + ").");
        }
    }

    // ---- Happy Hour (Stage 3) ----
    private void offerHappyHour() {
        HappyHour hh = restaurant.getHappyHour();
        if (hh.isAvailable() && ui.askHappyHour()) {
            hh.activate();
            List<String> events = new ArrayList<>();
            events.add("Happy Hour is ON for " + HappyHour.DURATION_TURNS + " turns: new orders get "
                    + (100 - HappyHour.BILL_PERCENT) + "% off.");
            events.addAll(happyHourRecovery());
            ui.showEvents("Happy Hour:", events);
        }
    }

    private List<String> happyHourRecovery() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<Customer, Integer> e : restaurant.applyHappyHourRecovery().entrySet()) {
            lines.add(e.getKey() + " recovers " + e.getValue() + " satisfaction (now "
                    + e.getKey().getSatisfaction() + ").");
        }
        return lines;
    }

    private List<String> happyHourNotice() {
        HappyHour hh = restaurant.getHappyHour();
        if (hh.isActive()) {
            return List.of("Happy Hour: ACTIVE - " + hh.getRemainingTurns() + " turn(s) remaining including this one");
        }
        return List.of(hh.isAvailable() ? "Happy Hour: available (once per game)" : "Happy Hour: used up");
    }

    // ---- step 2 ----
    private TurnPlan collectChoices() {
        TurnPlan plan = new TurnPlan();
        for (ManagedStaff staff : roster) {
            StaffTask task = chooseTask(staff, plan);
            if (task != null) plan.add(task);
        }
        return plan;
    }

    /** Keeps asking until the manager gives a valid task or waits. Invalid choices cost nothing. */
    private StaffTask chooseTask(ManagedStaff staff, TurnPlan plan) {
        while (true) {
            List<String> options = new ArrayList<>();
            for (StaffTask t : staff.availableTasks(restaurant, plan)) options.add(t.describe());
            ManagerCommand cmd = ui.promptStaff(staff.getRole(), options);
            switch (cmd.kind()) {
                case WAIT -> { return null; }
                case QUIT -> throw new GameQuitException("The manager quit the game.");
                case HELP -> ui.showHelp();
                case INVALID -> ui.showError(cmd.message());
                case TASK -> {
                    StaffTask task = buildTask(staff, cmd, plan);
                    if (task != null) return task;
                }
            }
        }
    }

    /** Returns a validated task, or null after telling the manager why the choice failed. */
    private StaffTask buildTask(ManagedStaff staff, ManagerCommand cmd, TurnPlan plan) {
        Optional<Customer> customer = restaurant.findCustomer(cmd.targetId());
        if (customer.isEmpty()) {
            ui.showError("There is no customer #" + cmd.targetId() + ".");
            return null;
        }
        Optional<StaffTask> task = staff.taskFor(cmd.verb(), customer.get());
        if (task.isEmpty()) {
            ui.showError("The " + staff.getRole() + " cannot '" + cmd.verb() + "'. " + staff.getRole()
                    + " tasks: " + String.join(", ", staff.getVerbs()) + ".");
            return null;
        }
        Optional<String> problem = task.get().validate(restaurant, plan);
        if (problem.isPresent()) {
            ui.showError(problem.get());
            return null;
        }
        Optional<StaffTask> clash = plan.findConflict(task.get());
        if (clash.isPresent()) {
            ui.showError("That clashes with a task already chosen this turn (" + clash.get().getActorRole()
                    + ": " + clash.get().describe() + ").");
            return null;
        }
        return task.get();
    }

    private Map<String, List<String>> describeAvailable(TurnPlan plan) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (ManagedStaff staff : roster) {
            List<String> options = new ArrayList<>();
            for (StaffTask t : staff.availableTasks(restaurant, plan)) options.add(t.describe());
            result.put(staff.getRole(), options);
        }
        result.put(cashier.getRole(), List.of("collects one payment automatically"));
        return result;
    }
}
