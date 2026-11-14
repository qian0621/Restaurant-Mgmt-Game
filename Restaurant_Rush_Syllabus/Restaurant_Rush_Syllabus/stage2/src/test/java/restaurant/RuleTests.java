package restaurant;

import java.util.List;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.CriticCustomer;
import restaurant.model.Customer;
import restaurant.model.CustomerState;
import restaurant.model.Money;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.RegularCustomer;
import restaurant.model.Restaurant;
import restaurant.model.VIPCustomer;
import restaurant.staff.Cashier;
import restaurant.staff.Chef;
import restaurant.staff.CookOrderTask;
import restaurant.staff.SeatCustomerTask;
import restaurant.staff.ServeOrderTask;
import restaurant.staff.StaffTask;
import restaurant.staff.TakeOrderTask;
import restaurant.staff.Waiter;

/** Dependency-free rule tests: run with java -cp out restaurant.RuleTests. Exit code 1 on failure. */
public class RuleTests {
    private static int passed;
    private static int failed;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  ok   " + name);
        } else {
            failed++;
            System.out.println("  FAIL " + name);
        }
    }

    private static Restaurant newRestaurant() {
        return GameConfig.base().newRestaurant();
    }

    private static Customer arrive(Restaurant r, int id, int turn) {
        Customer c = new RegularCustomer(id, turn);
        r.admit(c);
        return c;
    }

    /** The reason a task is refused, or null if it is allowed. */
    private static String problem(StaffTask task, Restaurant r, TurnPlan plan) {
        try {
            task.validate(r, plan);
            return null;
        } catch (InvalidChoiceException e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) {
        moneyIsExactInCents();
        dishFinishedThisTurnCannotBeServedThisTurn();
        eatingTakesAFullTurnAndPaymentComesAfterThat();
        invalidChoicesAreRejectedWithAReason();
        chefMayCookAnOrderTakenThisTurn();
        pastaNeedsTwoUnits();
        abandonmentCancelsOrderAndFreesTable();
        servedCustomersLoseNothing();
        noFreeTableBlocksSeating();
        objectiveChecksAllThreeTargets();
        illegalStepsThrowGameRuleException();
        vipLosesLessAndPays10PercentLess();
        criticLosesMoreOrdersPastaAndWritesReview();
        allCustomerTypesShareTheSameFlow();
        scheduleHasVipAndCritic();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void moneyIsExactInCents() {
        check("Money formats cents with two decimals", Money.format(1350).equals("$13.50") && Money.format(900).equals("$9.00"));
        check("Money keeps a leading zero in the cents", Money.format(1805).equals("$18.05"));
        check("percentOf rounds half-up to the cent", Money.percentOf(2430, 90) == 2187 && Money.percentOf(1, 50) == 1);
    }

    static void dishFinishedThisTurnCannotBeServedThisTurn() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);                              // Salad, 1 unit
        StaffTask cook = new Chef().taskFor("cook", c);
        StaffTask serve = new ServeOrderTask("Waiter", c);
        check("serve is rejected while the dish is still PLACED", problem(serve, r, new TurnPlan()) != null);
        cook.execute(r, 2);                          // Chef acts after the Waiter, so the Waiter's check ran first
        check("last unit makes the order READY", c.getOrder().getStatus() == OrderStatus.READY);
        check("serve is allowed on the next turn", problem(serve, r, new TurnPlan()) == null);
    }

    static void eatingTakesAFullTurnAndPaymentComesAfterThat() {
        Restaurant r = newRestaurant();
        Cashier cashier = new Cashier();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);
        c.getOrder().cookOneUnit();
        r.serve(c, 4);                               // served on turn 4
        check("customer served on turn 4 is not ready to pay on turn 4", r.finishEating(4).size() == 0);
        check("customer becomes READY_TO_PAY on turn 5", r.finishEating(5).size() == 1
                && c.getState() == CustomerState.READY_TO_PAY);
        check("cashier cannot charge on the turn the customer became ready", cashier.collectPayment(r, 5) == null);
        check("cashier charges on turn 6", cashier.collectPayment(r, 6) != null);
        check("payment adds the bill to revenue", r.getRevenueCents() == 900);
        check("payment marks customer and order PAID and frees the table",
                c.getState() == CustomerState.PAID && c.getOrder().getStatus() == OrderStatus.PAID
                        && r.freeTableCount() == 2);
        check("recorded satisfaction is kept", c.getRecordedSatisfaction() == 100);
    }

    static void invalidChoicesAreRejectedWithAReason() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        String why = problem(new TakeOrderTask("Waiter", c), r, new TurnPlan());
        check("cannot take an order from a customer who is not seated", why != null && why.contains("WAITING"));
        check("cannot cook before an order exists", problem(new CookOrderTask("Chef", c), r, new TurnPlan()) != null);
        check("a waiting customer can be seated", problem(new SeatCustomerTask("Waiter", c), r, new TurnPlan()) == null);
    }

    static void chefMayCookAnOrderTakenThisTurn() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        TurnPlan plan = new TurnPlan();
        StaffTask take = new TakeOrderTask("Waiter", c);
        plan.add(take);
        check("chef may be assigned the order the Waiter takes this turn",
                problem(new CookOrderTask("Chef", c), r, plan) == null);
        check("without the Waiter's task the chef cannot", problem(new CookOrderTask("Chef", c), r, new TurnPlan()) != null);
        take.execute(r, 2);
        new CookOrderTask("Chef", c).execute(r, 2);
        check("the order cooked in the same turn is READY", c.getOrder().getStatus() == OrderStatus.READY);
    }

    static void pastaNeedsTwoUnits() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 3, 7);                // customer #3 orders Pasta (rotation rule)
        r.seat(c);
        Order o = r.takeOrder(c);
        check("customer #3 chooses Pasta ($18.00, 2 units)", o.getItem().getName().equals("Pasta")
                && o.getBillCents() == 1800 && o.getItem().getPrepUnits() == 2);
        o.cookOneUnit();
        check("after one unit the order is PREPARING", o.getStatus() == OrderStatus.PREPARING);
        o.cookOneUnit();
        check("after two units the order is READY", o.getStatus() == OrderStatus.READY);
    }

    static void abandonmentCancelsOrderAndFreesTable() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);
        for (int i = 0; i < 12; i++) {
            r.applyWaitingLoss();                    // 100 - 12*8 = 4
        }
        check("satisfaction falls 8 per turn", c.getSatisfaction() == 4 && c.getState() == CustomerState.ORDERED);
        check("customer reaching 0 leaves", r.applyWaitingLoss().size() == 1 && c.getSatisfaction() == 0
                && c.getState() == CustomerState.LEFT);
        check("their order is cancelled and table freed", c.getOrder().getStatus() == OrderStatus.CANCELLED
                && r.freeTableCount() == 2);
        check("departure is counted and revenue untouched", r.getUnhappyDepartures() == 1 && r.getRevenueCents() == 0);
    }

    static void servedCustomersLoseNothing() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);
        c.getOrder().cookOneUnit();
        r.serve(c, 3);
        r.applyWaitingLoss();
        check("a served customer loses no satisfaction", c.getSatisfaction() == 100);
    }

    static void noFreeTableBlocksSeating() {
        Restaurant r = newRestaurant();
        Customer a = arrive(r, 1, 1);
        Customer b = arrive(r, 2, 1);
        Customer c = arrive(r, 3, 1);
        r.seat(a);
        r.seat(b);
        String why = problem(new SeatCustomerTask("Waiter", c), r, new TurnPlan());
        check("seating fails when both tables are taken", why != null && why.contains("no free table"));
        List<StaffTask> offered = new Waiter().availableTasks(r, new TurnPlan());
        boolean anySeat = false;
        for (StaffTask t : offered) {
            if (t.describe().startsWith("seat")) {
                anySeat = true;
            }
        }
        check("waiter offers no seat task then", !anySeat);
    }

    static void objectiveChecksAllThreeTargets() {
        Restaurant r = newRestaurant();
        GameResult none = GameConfig.base().evaluate(r);
        check("nobody paid is a defeat", !none.isVictory() && !none.isPaidTargetMet() && !none.isRevenueTargetMet()
                && !none.isSatisfactionTargetMet());
    }

    static void illegalStepsThrowGameRuleException() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        boolean refused = false;
        try {
            r.takeOrder(c);                          // c is still WAITING, not SEATED
        } catch (restaurant.model.GameRuleException e) {
            refused = true;
        }
        check("skipping a step in the dining flow throws GameRuleException", refused);
    }

    static void finishCooking(Order o) {
        while (o.isCookable()) {
            o.cookOneUnit();
        }
    }

    static void vipLosesLessAndPays10PercentLess() {
        Restaurant r = newRestaurant();
        Customer vip = new VIPCustomer(2, 4);          // customer #2 -> Burger ($15.00)
        r.admit(vip);
        r.seat(vip);
        Order o = r.takeOrder(vip);
        check("VIP bill is 10% off: Burger $13.50", o.getBillCents() == 1350);
        r.applyWaitingLoss();
        check("VIP loses 5 per waiting turn", vip.getSatisfaction() == 95);
    }

    static void criticLosesMoreOrdersPastaAndWritesReview() {
        Restaurant r = newRestaurant();
        Customer critic = new CriticCustomer(1, 1);    // customer #1 would be a Salad for a Regular
        r.admit(critic);
        r.seat(critic);
        Order o = r.takeOrder(critic);
        check("Critic always orders the most elaborate offering (Pasta, 2 units)",
                o.getItem().getName().equals("Pasta") && o.getItem().getPrepUnits() == 2);
        r.applyWaitingLoss();
        check("Critic loses 12 per waiting turn", critic.getSatisfaction() == 88);

        Restaurant happy = newRestaurant();
        Customer glowing = new CriticCustomer(1, 1);
        happy.admit(glowing);
        happy.seat(glowing);
        finishCooking(happy.takeOrder(glowing));
        happy.serve(glowing, 2);
        happy.finishEating(3);
        new Cashier().collectPayment(happy, 4);
        check("Critic at 100 (>= 60) gives a glowing review, capped at 100", glowing.getRecordedSatisfaction() == 100);

        Restaurant sad = newRestaurant();
        Customer harsh = new CriticCustomer(1, 1);
        sad.admit(harsh);
        sad.seat(harsh);
        finishCooking(sad.takeOrder(harsh));
        for (int i = 0; i < 4; i++) {
            sad.applyWaitingLoss();                  // 100 - 48 = 52
        }
        sad.serve(harsh, 5);
        sad.finishEating(6);
        new Cashier().collectPayment(sad, 7);
        check("Critic at 52 (< 60) gives a harsh review: 42", harsh.getRecordedSatisfaction() == 42);
    }

    static void allCustomerTypesShareTheSameFlow() {
        Customer[] all = { new RegularCustomer(1, 1), new VIPCustomer(1, 1), new CriticCustomer(1, 1) };
        boolean ok = true;
        for (Customer c : all) {
            Restaurant r = newRestaurant();
            r.admit(c);
            r.seat(c);
            finishCooking(r.takeOrder(c));
            r.serve(c, 3);
            r.finishEating(4);
            new Cashier().collectPayment(r, 5);
            ok = ok && c.getState() == CustomerState.PAID;
        }
        check("Regular, VIP and Critic all follow seat-order-cook-serve-eat-pay", ok);
    }

    static void scheduleHasVipAndCritic() {
        GameConfig cfg = GameConfig.base();
        check("six arrivals; turn 4 is a VIP and turn 10 a Critic",
                cfg.getSchedule().size() == 6
                        && cfg.getSchedule().typeAt(4) == restaurant.model.CustomerType.VIP
                        && cfg.getSchedule().typeAt(10) == restaurant.model.CustomerType.CRITIC);
    }
}
