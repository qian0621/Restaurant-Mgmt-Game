package restaurant.model;

/** One customer's order. The bill is locked in when the order is placed. */
public class Order {
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private int remainingUnits;
    private OrderStatus status = OrderStatus.PLACED;

    Order(Customer customer, MenuItem item, int billCents) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.remainingUnits = item.getPrepUnits();
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getRemainingUnits() {
        return remainingUnits;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /** True while the Chef can still advance this order. */
    public boolean isCookable() {
        return status == OrderStatus.PLACED || status == OrderStatus.PREPARING;
    }

    /** Spend one preparation unit; the last unit makes the order READY. */
    public void cookOneUnit() {
        if (!isCookable()) {
            throw new GameRuleException("Order is " + status + ", cannot cook");
        }
        remainingUnits--;
        if (remainingUnits == 0) {
            status = OrderStatus.READY;
        } else {
            status = OrderStatus.PREPARING;
        }
    }

    void markServed() {
        if (status != OrderStatus.READY) {
            throw new GameRuleException("Order is " + status + ", cannot serve");
        }
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new GameRuleException("Order is " + status + ", cannot pay");
        }
        status = OrderStatus.PAID;
    }

    void cancel() {
        if (status != OrderStatus.PAID) {
            status = OrderStatus.CANCELLED;
        }
    }
}
