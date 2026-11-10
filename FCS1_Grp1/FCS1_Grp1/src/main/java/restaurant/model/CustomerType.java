package restaurant.model;

/** Factory for the kinds of customer that can appear in an arrival schedule. */
public enum CustomerType {
    REGULAR {
        @Override public Customer create(int id, int arrivalTurn) { return new RegularCustomer(id, arrivalTurn); }
    },
    VIP {
        @Override public Customer create(int id, int arrivalTurn) { return new VIPCustomer(id, arrivalTurn); }
    },
    CRITIC {
        @Override public Customer create(int id, int arrivalTurn) { return new CriticCustomer(id, arrivalTurn); }
    };

    public abstract Customer create(int id, int arrivalTurn);
}
