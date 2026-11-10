package restaurant.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * The restaurant aggregate: tables, waiting queue, customers and the running score. All game rules
 * that change customers, orders and tables go through here, so the staff tasks and the controller
 * never touch another object's state directly.
 */
public class Restaurant {
    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final HappyHour happyHour = new HappyHour();

    private int revenueCents;
    private int paidCount;
    private int paidSatisfactionTotal;
    private int servedCount;
    private int unhappyDepartures;

    public Restaurant(Menu menu, int tableCount) {
        this.menu = menu;
        for (int i = 1; i <= tableCount; i++) tables.add(new Table(i));
    }

    // ---- queries ----
    public Menu getMenu() { return menu; }
    public HappyHour getHappyHour() { return happyHour; }
    public List<Table> getTables() { return Collections.unmodifiableList(tables); }
    public WaitingQueue getQueue() { return queue; }
    public int getRevenueCents() { return revenueCents; }
    public int getPaidCount() { return paidCount; }
    public int getServedCount() { return servedCount; }
    public int getUnhappyDepartures() { return unhappyDepartures; }
    public int getPaidSatisfactionTotal() { return paidSatisfactionTotal; }

    /** Average satisfaction recorded at payment; empty if nobody has paid yet. */
    public OptionalDouble getAverageSatisfaction() {
        return paidCount == 0 ? OptionalDouble.empty() : OptionalDouble.of((double) paidSatisfactionTotal / paidCount);
    }

    public List<Customer> getCustomers() { return Collections.unmodifiableList(customers); }

    public List<Customer> getActiveCustomers() {
        List<Customer> active = new ArrayList<>();
        for (Customer c : customers) if (c.isActive()) active.add(c);
        return active;
    }

    public Optional<Customer> findCustomer(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    public int freeTableCount() {
        int n = 0;
        for (Table t : tables) if (t.isFree()) n++;
        return n;
    }

    // ---- step 1: arrivals ----
    public void admit(Customer c) {
        customers.add(c);
        queue.join(c);
    }

    // ---- waiter / chef actions ----
    /** Seats a queued customer at the lowest-numbered free table. */
    public Table seat(Customer c) {
        Table t = tables.stream().filter(Table::isFree).findFirst()
                .orElseThrow(() -> new IllegalStateException("no free table"));
        c.seatAt(t);
        queue.remove(c);
        return t;
    }

    /** The customer chooses a dish; the bill is locked in at this moment. */
    public Order takeOrder(Customer c) {
        MenuItem item = c.chooseItem(menu);
        Order order = new Order(c, item, quote(c, item));
        c.placeOrder(order);
        return order;
    }

    /**
     * Price a customer will be billed for an item: the customer's own rule first (e.g. VIP discount),
     * then Happy Hour if it is active right now. The result is locked into the order.
     */
    public int quote(Customer c, MenuItem item) {
        return happyHour.applyTo(c.billFor(item));
    }

    public void serve(Customer c, int turn) {
        c.getOrder().markServed();
        c.startEating(turn);
        servedCount++;
    }

    // ---- step 4: eating and payment ----
    /** Customers served on an earlier turn become READY_TO_PAY. */
    public List<Customer> finishEating(int turn) {
        List<Customer> done = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getState() == CustomerState.EATING && c.getServedTurn() < turn) {
                c.finishEating(turn);
                done.add(c);
            }
        }
        return done;
    }

    /** The READY_TO_PAY customer who has waited longest (ties: earlier arrival) who can pay this turn. */
    public Optional<Customer> nextToPay(int turn) {
        return customers.stream()
                .filter(c -> c.getState() == CustomerState.READY_TO_PAY && c.getReadyToPayTurn() < turn)
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId));
    }

    public void collectPayment(Customer c) {
        int bill = c.getOrder().getBillCents();
        c.pay();
        revenueCents += bill;
        paidCount++;
        paidSatisfactionTotal += c.getRecordedSatisfaction();
    }

    // ---- Happy Hour ----
    /** Customers still waiting for a table or food recover satisfaction. Returns who gained how much. */
    public Map<Customer, Integer> applyHappyHourRecovery() {
        Map<Customer, Integer> gained = new LinkedHashMap<>();
        for (Customer c : customers) {
            if (c.isWaitingForService()) {
                int points = c.recoverSatisfaction(HappyHour.RECOVERY_POINTS);
                if (points > 0) gained.put(c, points);
            }
        }
        return gained;
    }

    /** End-of-turn bookkeeping for time-limited policies. */
    public void endTurn() {
        happyHour.endTurn();
    }

    // ---- step 5: waiting and abandonment ----
    /** Applies the end-of-turn waiting loss. Returns the customers who left unhappy. */
    public List<Customer> applyWaitingLoss() {
        List<Customer> left = new ArrayList<>();
        for (Customer c : customers) {
            if (c.isWaitingForService() && c.loseSatisfaction()) {
                if (c.getState() == CustomerState.WAITING) queue.remove(c);
                c.abandon();
                unhappyDepartures++;
                left.add(c);
            }
        }
        return left;
    }
}
