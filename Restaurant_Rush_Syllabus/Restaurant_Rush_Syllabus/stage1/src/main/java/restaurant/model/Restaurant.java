package restaurant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private int revenueCents;
    private int paidCount;
    private int paidSatisfactionTotal;
    private int servedCount;
    private int unhappyDepartures;

    public Restaurant(Menu menu, int tableCount) {
        this.menu = menu;
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }

    // ---- queries ----

    public Menu getMenu() {
        return menu;
    }

    public List<Table> getTables() {
        return new ArrayList<>(tables);
    }

    public WaitingQueue getQueue() {
        return queue;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public int getPaidSatisfactionTotal() {
        return paidSatisfactionTotal;
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    /** Customers still inside the restaurant (a stream with a method reference). */
    public List<Customer> getActiveCustomers() {
        return customers.stream().filter(Customer::isActive).collect(Collectors.toList());
    }

    /** The customer with this number, or null if there is none. */
    public Customer findCustomer(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public int freeTableCount() {
        int n = 0;
        for (Table t : tables) {
            if (t.isFree()) {
                n++;
            }
        }
        return n;
    }

    // ---- step 1: arrivals ----

    public void admit(Customer c) {
        customers.add(c);
        queue.join(c);
    }

    // ---- waiter and chef actions ----

    /** Seats a queued customer at the lowest-numbered free table. */
    public Table seat(Customer c) {
        for (Table t : tables) {
            if (t.isFree()) {
                c.seatAt(t);
                queue.remove(c);
                return t;
            }
        }
        throw new GameRuleException("no free table");
    }

    /** The customer chooses a dish; the bill is locked in at this moment. */
    public Order takeOrder(Customer c) {
        MenuItem item = c.chooseItem(menu);
        Order order = new Order(c, item, quote(c, item));
        c.placeOrder(order);
        return order;
    }

    /** The bill a customer will get for an item. */
    public int quote(Customer c, MenuItem item) {
        return item.getPriceCents();
    }

    public void serve(Customer c, int turn) {
        c.getOrder().markServed();
        c.startEating(turn);
        servedCount++;
    }

    // ---- step 4: eating and payment ----

    /** Customers served on an earlier turn become READY_TO_PAY. Returns who changed. */
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

    /**
     * The READY_TO_PAY customer who has waited longest and is allowed to pay this turn (a customer
     * who became ready this turn pays next turn at the earliest). Null if there is none.
     */
    public Customer nextToPay(int turn) {
        Customer best = null;
        for (Customer c : customers) {
            if (c.getState() == CustomerState.READY_TO_PAY && c.getReadyToPayTurn() < turn) {
                if (best == null || c.getReadyToPayTurn() < best.getReadyToPayTurn()) {
                    best = c;
                }
            }
        }
        return best;
    }

    public void collectPayment(Customer c) {
        int bill = c.getOrder().getBillCents();
        c.pay();
        revenueCents += bill;
        paidCount++;
        paidSatisfactionTotal += c.getRecordedSatisfaction();
    }

    // ---- step 5: waiting and abandonment ----

    /** Applies the end-of-turn waiting loss. Returns the customers who left unhappy. */
    public List<Customer> applyWaitingLoss() {
        List<Customer> left = new ArrayList<>();
        for (Customer c : customers) {
            if (c.isWaitingForService() && c.loseSatisfaction()) {
                if (c.getState() == CustomerState.WAITING) {
                    queue.remove(c);
                }
                c.abandon();
                unhappyDepartures++;
                left.add(c);
            }
        }
        return left;
    }
}
