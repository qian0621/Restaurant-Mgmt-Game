package restaurant.model;

/** The kinds of customer that can appear in an arrival schedule, and how to build each one. */
public enum CustomerType {
    REGULAR, VIP, CRITIC;

    /** Builds a customer of this type (a switch expression over the enum). */
    public Customer create(int id, int arrivalTurn) {
        return switch (this) {
            case REGULAR -> new RegularCustomer(id, arrivalTurn);
            case VIP -> new VIPCustomer(id, arrivalTurn);
            case CRITIC -> new CriticCustomer(id, arrivalTurn);
        };
    }
}
