package restaurant.model;

import java.util.List;

public abstract class Customer {
    public static final int START_SATISFACTION = 100;

    private final int id;
    private final int arrivalTurn;
    private int satisfaction = START_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;
    private int recordedSatisfaction = -1;

    protected Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }


    public abstract String getTypeName();

    public abstract int getLossPerWaitingTurn();

    public MenuItem chooseItem(Menu menu) {
        List<MenuItem> offerings = menu.getOfferings();
        return offerings.get((id - 1) % offerings.size());
    }

    public int billFor(MenuItem item) {
        return item.getPriceCents();
    }

    protected int satisfactionAtPayment() {
        return satisfaction;
    }

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getRecordedSatisfaction() {
        return recordedSatisfaction;
    }

    /** Waiting for a table or for food: these are the customers who lose satisfaction. */
    public boolean isWaitingForService() {
        return state == CustomerState.WAITING || state == CustomerState.SEATED
                || state == CustomerState.ORDERED;
    }

    /** Still in the restaurant (has neither paid nor left). */
    public boolean isActive() {
        return state != CustomerState.PAID && state != CustomerState.LEFT;
    }

    // ---- state transitions (package-private: only Restaurant calls them) ----

    void seatAt(Table t) {
        require(CustomerState.WAITING);
        t.occupy(this);
        table = t;
        state = CustomerState.SEATED;
    }

    void placeOrder(Order o) {
        require(CustomerState.SEATED);
        order = o;
        state = CustomerState.ORDERED;
    }

    void startEating(int turn) {
        require(CustomerState.ORDERED);
        servedTurn = turn;
        state = CustomerState.EATING;
    }

    void finishEating(int turn) {
        require(CustomerState.EATING);
        readyToPayTurn = turn;
        state = CustomerState.READY_TO_PAY;
    }

    void pay() {
        require(CustomerState.READY_TO_PAY);
        recordedSatisfaction = satisfactionAtPayment();
        order.markPaid();
        table.release();
        state = CustomerState.PAID;
    }

    boolean loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getLossPerWaitingTurn());
        return satisfaction == 0;
    }

    void abandon() {
        if (order != null) {
            order.cancel();
        }
        if (table != null) {
            table.release();
        }
        state = CustomerState.LEFT;
    }

    private void require(CustomerState expected) {
        if (state != expected) {
            throw new GameRuleException("Customer #" + id + " is " + state + ", expected " + expected);
        }
    }

    @Override
    public String toString() {
        return "#" + id + " " + getTypeName();
    }
}
