package restaurant;

import java.util.Optional;
import restaurant.control.GameConfig;
import restaurant.control.GameResult;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.ComboMeal;
import restaurant.model.CriticCustomer;
import restaurant.model.HappyHour;
import restaurant.model.MenuItem;
import restaurant.model.CustomerState;
import restaurant.model.VIPCustomer;
import restaurant.model.Money;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.RegularCustomer;
import restaurant.model.Restaurant;
import restaurant.staff.Cashier;
import restaurant.staff.Chef;
import restaurant.staff.CookOrderTask;
import restaurant.staff.Host;
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
        if (ok) { passed++; System.out.println("  ok   " + name); }
        else { failed++; System.out.println("  FAIL " + name); }
    }

    private static void finishCooking(Order o) {
        while (o.isCookable()) o.cookOneUnit();
    }

    private static Restaurant newRestaurant() { return GameConfig.base().newRestaurant(); }

    private static Customer arrive(Restaurant r, int id, int turn) {
        Customer c = new RegularCustomer(id, turn);
        r.admit(c);
        return c;
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
        averageIsNotAvailableBeforeFirstPayment();
        objectiveChecksAllThreeTargets();
        vipLosesLessAndPays10PercentLess();
        criticLosesMoreOrdersPastaAndWritesReview();
        allCustomerTypesShareTheSameFlow();
        comboPriceAndUnits();
        customersChooseTheComboByTheDocumentedRule();
        happyHourPricingAndLockIn();
        happyHourLastsTwoTurnsAndOnlyOnce();
        happyHourRecoversWaitingCustomersOnly();
        hostAndWaiterCanSeatTwoDifferentCustomers();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    static void moneyIsExactInCents() {
        check("Money formats cents with two decimals", Money.format(1350).equals("$13.50") && Money.format(900).equals("$9.00"));
        check("percentOf rounds half-up to the cent", Money.percentOf(2430, 90) == 2187 && Money.percentOf(1, 50) == 1);
    }

    static void dishFinishedThisTurnCannotBeServedThisTurn() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);                              // Salad, 1 unit
        StaffTask cook = new Chef().taskFor("cook", c).orElseThrow();
        StaffTask serve = new ServeOrderTask("Waiter", c);
        check("serve is rejected while the dish is still PLACED", serve.validate(r, new TurnPlan()).isPresent());
        cook.execute(r, 2);                          // Chef acts after the Waiter, so the Waiter's check ran first
        check("last unit makes the order READY", c.getOrder().getStatus() == OrderStatus.READY);
        check("serve is allowed on the next turn", serve.validate(r, new TurnPlan()).isEmpty());
    }

    static void eatingTakesAFullTurnAndPaymentComesAfterThat() {
        Restaurant r = newRestaurant();
        Cashier cashier = new Cashier();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        r.takeOrder(c);
        c.getOrder().cookOneUnit();
        r.serve(c, 4);                               // served on turn 4
        check("customer served on turn 4 is not ready to pay on turn 4", r.finishEating(4).isEmpty());
        check("customer becomes READY_TO_PAY on turn 5", r.finishEating(5).size() == 1
                && c.getState() == CustomerState.READY_TO_PAY);
        check("cashier cannot charge on the turn the customer became ready", cashier.collectPayment(r, 5).isEmpty());
        check("cashier charges on turn 6", cashier.collectPayment(r, 6).isPresent());
        check("payment adds the bill to revenue", r.getRevenueCents() == 900);
        check("payment marks customer and order PAID and frees the table",
                c.getState() == CustomerState.PAID && c.getOrder().getStatus() == OrderStatus.PAID
                        && r.freeTableCount() == 2);
        check("recorded satisfaction is kept", c.getRecordedSatisfaction() == 100);
    }

    static void invalidChoicesAreRejectedWithAReason() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        Optional<String> why = new TakeOrderTask("Waiter", c).validate(r, new TurnPlan());
        check("cannot take an order from a customer who is not seated", why.isPresent() && why.get().contains("WAITING"));
        check("cannot cook before an order exists", new CookOrderTask("Chef", c).validate(r, new TurnPlan()).isPresent());
        check("a waiting customer can be seated", new SeatCustomerTask("Waiter", c).validate(r, new TurnPlan()).isEmpty());
    }

    static void chefMayCookAnOrderTakenThisTurn() {
        Restaurant r = newRestaurant();
        Customer c = arrive(r, 1, 1);
        r.seat(c);
        TurnPlan plan = new TurnPlan();
        StaffTask take = new TakeOrderTask("Waiter", c);
        plan.add(take);
        check("chef may be assigned the order the Waiter takes this turn",
                new CookOrderTask("Chef", c).validate(r, plan).isEmpty());
        check("without the Waiter's task the chef cannot", new CookOrderTask("Chef", c).validate(r, new TurnPlan()).isPresent());
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
        for (int i = 0; i < 12; i++) r.applyWaitingLoss();   // 100 - 12*8 = 4
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
        Customer a = arrive(r, 1, 1), b = arrive(r, 2, 1), c = arrive(r, 3, 1);
        r.seat(a);
        r.seat(b);
        Optional<String> why = new SeatCustomerTask("Waiter", c).validate(r, new TurnPlan());
        check("seating fails when both tables are taken", why.isPresent() && why.get().contains("no free table"));
        check("waiter offers no seat task then", new Waiter().availableTasks(r, new TurnPlan()).stream()
                .noneMatch(t -> t.describe().startsWith("seat")));
    }

    static void averageIsNotAvailableBeforeFirstPayment() {
        Restaurant r = newRestaurant();
        check("average satisfaction is N/A before anyone pays", r.getAverageSatisfaction().isEmpty());
    }

    static void objectiveChecksAllThreeTargets() {
        Restaurant r = newRestaurant();
        GameResult none = GameConfig.base().evaluate(r);
        check("nobody paid is a defeat", !none.isVictory() && !none.paidTargetMet() && !none.revenueTargetMet()
                && !none.satisfactionTargetMet());
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
        check("Critic always orders the most elaborate offering (the 3-unit Combo)", o.getItem() instanceof ComboMeal
                && o.getItem().getPrepUnits() == 3);
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
        for (int i = 0; i < 4; i++) sad.applyWaitingLoss();   // 100 - 48 = 52
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
            ok &= c.getState() == CustomerState.PAID;
        }
        check("Regular, VIP and Critic all follow seat-order-cook-serve-eat-pay", ok);
    }

    static void comboPriceAndUnits() {
        MenuItem salad = new MenuItem("Salad", 900, 1), pasta = new MenuItem("Pasta", 1800, 2);
        ComboMeal combo = new ComboMeal(salad, pasta);
        check("Salad + Pasta combo costs $24.30", combo.getPriceCents() == 2430);
        check("combo preparation units are the sum of its parts (3)", combo.getPrepUnits() == 3);
        ComboMeal other = new ComboMeal(new MenuItem("Burger", 1500, 1), salad);
        check("combo price is rounded to the nearest cent ($21.60)", other.getPriceCents() == 2160);
        check("the game's menu offers the combo", GameConfig.base().newMenu().getOfferings().get(3) instanceof ComboMeal);
    }

    static void customersChooseTheComboByTheDocumentedRule() {
        Restaurant r = newRestaurant();
        Customer c4 = arrive(r, 4, 10);                 // ((4-1) mod 4) = 3 -> the combo
        r.seat(c4);
        check("a Regular numbered 4 orders the combo", r.takeOrder(c4).getItem() instanceof ComboMeal);
    }

    static void happyHourPricingAndLockIn() {
        Restaurant r = new Restaurant(GameConfig.base().newMenu(), 3);   // three tables so nobody has to leave
        Customer early = arrive(r, 1, 1);                // Salad
        r.seat(early);
        Order before = r.takeOrder(early);               // placed before Happy Hour
        r.getHappyHour().activate();
        Customer vip = new VIPCustomer(2, 4);            // Burger
        r.admit(vip);
        r.seat(vip);
        Order during = r.takeOrder(vip);
        check("VIP Burger during Happy Hour: $15 x 0.9 x 0.8 = $10.80", during.getBillCents() == 1080);
        check("orders already placed are not changed by Happy Hour", before.getBillCents() == 900);
        r.endTurn();
        r.endTurn();                                     // Happy Hour is over
        check("the discount stays locked in after Happy Hour ends", during.getBillCents() == 1080);
        Customer late = arrive(r, 3, 7);                 // Pasta
        r.seat(late);
        check("orders taken after Happy Hour pay full price", r.takeOrder(late).getBillCents() == 1800);
    }

    static void happyHourLastsTwoTurnsAndOnlyOnce() {
        HappyHour hh = new HappyHour();
        check("Happy Hour starts available and inactive", hh.isAvailable() && !hh.isActive());
        hh.activate();
        check("activating gives 2 turns remaining", hh.isActive() && hh.getRemainingTurns() == 2);
        hh.endTurn();
        check("1 turn remaining after the first turn", hh.isActive() && hh.getRemainingTurns() == 1);
        hh.endTurn();
        check("inactive after the second turn", !hh.isActive());
        boolean refused = false;
        try { hh.activate(); } catch (IllegalStateException e) { refused = true; }
        check("it cannot be activated twice in one game", refused && !hh.isAvailable());
    }

    static void happyHourRecoversWaitingCustomersOnly() {
        Restaurant r = newRestaurant();
        Customer waiting = arrive(r, 1, 1);
        r.applyWaitingLoss();
        r.applyWaitingLoss();                            // 100 -> 84
        Customer eating = arrive(r, 2, 1);
        r.seat(eating);
        r.takeOrder(eating).cookOneUnit();
        r.serve(eating, 1);
        r.applyHappyHourRecovery();
        check("waiting customer recovers 15", waiting.getSatisfaction() == 99);
        r.applyHappyHourRecovery();
        check("recovery is capped at 100", waiting.getSatisfaction() == 100);
        check("a served customer is not affected", eating.getSatisfaction() == 100);
    }

    static void hostAndWaiterCanSeatTwoDifferentCustomers() {
        Restaurant r = newRestaurant();
        Customer a = arrive(r, 1, 1), b = arrive(r, 2, 1), c = arrive(r, 3, 1);
        TurnPlan plan = new TurnPlan();
        StaffTask hostSeats = new Host().taskFor("seat", a).orElseThrow();
        check("Host can seat a queued customer", hostSeats.validate(r, plan).isEmpty());
        plan.add(hostSeats);
        StaffTask waiterSame = new Waiter().taskFor("seat", a).orElseThrow();
        check("Waiter cannot seat the same customer in the same turn", plan.conflicts(waiterSame));
        StaffTask waiterOther = new Waiter().taskFor("seat", b).orElseThrow();
        check("Waiter can seat a different customer in the same turn",
                waiterOther.validate(r, plan).isEmpty() && !plan.conflicts(waiterOther));
        plan.add(waiterOther);
        check("with both tables claimed a third seating is refused", new SeatCustomerTask("Waiter", c).validate(r, plan).isPresent());
        hostSeats.execute(r, 4);
        waiterOther.execute(r, 4);
        check("both customers end up seated at different tables", a.getTable() != b.getTable() && r.freeTableCount() == 0);
    }
}
