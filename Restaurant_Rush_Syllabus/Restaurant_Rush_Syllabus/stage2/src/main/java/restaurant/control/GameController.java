package restaurant.control;

import java.util.ArrayList;
import java.util.List;
import restaurant.model.Customer;
import restaurant.model.CustomerType;
import restaurant.model.Restaurant;
import restaurant.staff.Cashier;
import restaurant.staff.Chef;
import restaurant.staff.ManagedStaff;
import restaurant.staff.StaffTask;
import restaurant.staff.Waiter;

public class GameController {
    private final GameConfig config;
    private final Restaurant restaurant;
    private final GameUI ui;
    private final List<ManagedStaff> roster = new ArrayList<>();
    private final Cashier cashier = new Cashier();
    private int nextCustomerId = 1;

    public GameController(GameConfig config, GameUI ui) {
        this.config = config;
        this.ui = ui;
        this.restaurant = config.newRestaurant();
        roster.add(new Waiter());
        roster.add(new Chef());
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public GameResult run() {
        ui.showWelcome(config);
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        GameResult result = config.evaluate(restaurant);       
        ui.showResult(config, result);
        return result;
    }

    private void playTurn(int turn) {
        ui.showTurnHeader(turn, config.getMaxTurns());
        List<String> events = new ArrayList<>();

        arrivals(turn, events);                                   
        ui.showEvents("Arrivals:", events);
        List<String> notices = new ArrayList<>();
        ui.showBoard(restaurant, turn, config.getMaxTurns(), notices, describeAvailable(new TurnPlan()));

        TurnPlan plan = collectChoices();                         

        events = new ArrayList<>();
        for (StaffTask task : plan.getTasks()) {                  
            events.add(task.execute(restaurant, turn));
        }
        ui.showEvents("Kitchen and serving:", events);

        events = new ArrayList<>();
        for (Customer c : restaurant.finishEating(turn)) {        
            events.add(c + " finishes eating and is READY_TO_PAY.");
        }
        String payment = cashier.collectPayment(restaurant, turn);
        if (payment != null) {
            events.add(payment);
        }
        ui.showEvents("Eating and payment:", events);

        events = new ArrayList<>();
        for (Customer c : restaurant.applyWaitingLoss()) {        
            events.add(c + " lost patience and leaves without paying (order cancelled, table freed).");
        }
        ui.showEvents("Waiting and abandonment:", events);
        ui.showTurnSummary(restaurant, turn);
    }


    private void arrivals(int turn, List<String> events) {
        CustomerType type = config.getSchedule().typeAt(turn);
        if (type != null) {
            Customer c = type.create(nextCustomerId, turn);
            nextCustomerId++;
            restaurant.admit(c);
            events.add(c + " arrives and joins the waiting queue (satisfaction " + c.getSatisfaction() + ").");
        }
    }


    private TurnPlan collectChoices() {
        TurnPlan plan = new TurnPlan();
        for (ManagedStaff staff : roster) {
            StaffTask task = chooseTask(staff, plan);
            if (task != null) {
                plan.add(task);
            }
        }
        return plan;
    }

    private StaffTask chooseTask(ManagedStaff staff, TurnPlan plan) {
        while (true) {
            List<String> options = new ArrayList<>();
            for (StaffTask t : staff.availableTasks(restaurant, plan)) {
                options.add(t.describe());
            }
            ManagerCommand cmd = ui.promptStaff(staff.getRole(), options);
            switch (cmd.getKind()) {
                case WAIT -> {
                    return null;
                }
                case QUIT -> throw new GameQuitException("The manager quit the game.");
                case HELP -> ui.showHelp();
                case INVALID -> ui.showError(cmd.getMessage());
                case TASK -> {
                    StaffTask task = buildTask(staff, cmd, plan);
                    if (task != null) {
                        return task;
                    }
                }
            }
        }
    }

    private StaffTask buildTask(ManagedStaff staff, ManagerCommand cmd, TurnPlan plan) {
        Customer customer = restaurant.findCustomer(cmd.getTargetId());
        if (customer == null) {
            ui.showError("There is no customer #" + cmd.getTargetId() + ".");
            return null;
        }
        StaffTask task = staff.taskFor(cmd.getVerb(), customer);
        if (task == null) {
            ui.showError("The " + staff.getRole() + " cannot '" + cmd.getVerb() + "'. " + staff.getRole()
                    + " tasks: " + joinWords(staff.getVerbs()) + ".");
            return null;
        }
        try {
            task.validate(restaurant, plan);
        } catch (InvalidChoiceException e) {
            ui.showError(e.getMessage());
            return null;
        }
        StaffTask clash = plan.findConflict(task);
        if (clash != null) {
            ui.showError("That clashes with a task already chosen this turn (" + clash.getActorRole()
                    + ": " + clash.describe() + ").");
            return null;
        }
        return task;
    }

    private List<RoleOptions> describeAvailable(TurnPlan plan) {
        List<RoleOptions> result = new ArrayList<>();
        for (ManagedStaff staff : roster) {
            List<String> options = new ArrayList<>();
            for (StaffTask t : staff.availableTasks(restaurant, plan)) {
                options.add(t.describe());
            }
            result.add(new RoleOptions(staff.getRole(), options));
        }
        List<String> auto = new ArrayList<>();
        auto.add("collects one payment automatically");
        result.add(new RoleOptions(cashier.getRole(), auto));
        return result;
    }

    private static String joinWords(List<String> words) {
        String text = "";
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) {
                text = text + ", ";
            }
            text = text + words.get(i);
        }
        return text;
    }
}
