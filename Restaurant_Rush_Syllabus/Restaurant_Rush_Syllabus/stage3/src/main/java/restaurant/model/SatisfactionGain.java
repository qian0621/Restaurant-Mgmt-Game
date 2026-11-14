package restaurant.model;

/** Records that one customer regained some satisfaction (used to report Happy Hour recovery). */
public class SatisfactionGain {
    private final Customer customer;
    private final int points;

    public SatisfactionGain(Customer customer, int points) {
        this.customer = customer;
        this.points = points;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getPoints() {
        return points;
    }
}
