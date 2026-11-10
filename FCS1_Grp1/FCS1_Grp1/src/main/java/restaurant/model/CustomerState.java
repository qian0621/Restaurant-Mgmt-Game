package restaurant.model;

/** Where a customer is in the dining flow. */
public enum CustomerState {
    WAITING,      // in the waiting queue
    SEATED,       // at a table, order not yet taken
    ORDERED,      // order taken, food not yet served
    EATING,       // served, eating
    READY_TO_PAY, // finished eating, waiting for the Cashier
    PAID,         // left after paying
    LEFT          // left unhappy without paying
}
