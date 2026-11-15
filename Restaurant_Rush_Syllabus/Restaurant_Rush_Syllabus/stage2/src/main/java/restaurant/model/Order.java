package restaurant.model;

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

    public boolean isCookable() {
        return status == OrderStatus.PLACED || status == OrderStatus.PREPARING;
    }

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
