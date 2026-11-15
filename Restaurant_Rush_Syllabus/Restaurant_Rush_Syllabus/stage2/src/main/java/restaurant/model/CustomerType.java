package restaurant.model;

public enum CustomerType {
    REGULAR, VIP, CRITIC;

    public Customer create(int id, int arrivalTurn) {
        return switch (this) {
            case REGULAR -> new RegularCustomer(id, arrivalTurn);
            case VIP -> new VIPCustomer(id, arrivalTurn);
            case CRITIC -> new CriticCustomer(id, arrivalTurn);
        };
    }
}
