package restaurant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }


    public Menu getMenu() {
        return menu;
    }

    public List<Table> getTables() {
        return new ArrayList<>(tables);
    }

    public HappyHour getHappyHour() {
        return happyHour;
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

    public List<Customer> getActiveCustomers() {
        return customers.stream().filter(Customer::isActive).collect(Collectors.toList());
    }

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


    public void admit(Customer c) {
        customers.add(c);
        queue.join(c);
    }


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

    public Order takeOrder(Customer c) {
        MenuItem item = c.chooseItem(menu);
        Order order = new Order(c, item, quote(c, item));
        c.placeOrder(order);
        return order;
    }


    public int quote(Customer c, MenuItem item) {
        return happyHour.applyTo(c.billFor(item));
    }

    public void serve(Customer c, int turn) {
        c.getOrder().markServed();
        c.startEating(turn);
        servedCount++;
    }

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

    public List<SatisfactionGain> applyHappyHourRecovery() {
        List<SatisfactionGain> gains = new ArrayList<>();
        for (Customer c : customers) {
            if (c.isWaitingForService()) {
                int points = c.recoverSatisfaction(HappyHour.RECOVERY_POINTS);
                if (points > 0) {
                    gains.add(new SatisfactionGain(c, points));
                }
            }
        }
        return gains;
    }

    public void endTurn() {
        happyHour.endTurn();
    }
}
