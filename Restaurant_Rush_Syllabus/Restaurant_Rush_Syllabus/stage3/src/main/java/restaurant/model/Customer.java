package restaurant.model;

import java.util.List;

/**
 * A customer. Subclasses supply the behaviour that differs between customer kinds; the state
 * machine (WAITING, SEATED, ORDERED, EATING, READY_TO_PAY, PAID, or LEFT) is guarded here so that
 * no caller can skip a step.
 */
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

    // ---- behaviour that differs by customer kind (overridden in subclasses) ----

    public abstract String getTypeName();

    /** Satisfaction lost at the end of a turn in which this customer has not yet been served. */
    public abstract int getLossPerWaitingTurn();

    /**
     * Documented choice rule: customer #k picks the ((k-1) mod n)-th menu offering, so a run is
     * fully reproducible without randomness.
     */
    public MenuItem chooseItem(Menu menu) {
        List<MenuItem> offerings = menu.getOfferings();
        return offerings.get((id - 1) % offerings.size());
    }

    /** What this customer is billed for an item. Full price unless a subclass says otherwise. */
    public int billFor(MenuItem item) {
        return item.getPriceCents();
    }

    /** The satisfaction recorded when this customer pays. Normally the current value. */
    protected int satisfactionAtPayment() {
        return satisfaction;
    }

    // ---- queries ----

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

    /** Recovers satisfaction (capped at 100). Returns the points actually gained. */
    int recoverSatisfaction(int points) {
        int before = satisfaction;
        satisfaction = Math.min(START_SATISFACTION, satisfaction + points);
        return satisfaction - before;
    }

    /** Applies this turn's waiting loss (never below 0). Returns true if the customer reached 0. */
    boolean loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getLossPerWaitingTurn());
        return satisfaction == 0;
    }

    /** Leaves unhappy: the unfinished order is cancelled and the table freed. */
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
