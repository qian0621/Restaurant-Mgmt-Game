package restaurant.model;

public class RegularCustomer extends Customer {
    public static final int LOSS_PER_TURN = 8;

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }

    @Override
    public int getLossPerWaitingTurn() {
        return LOSS_PER_TURN;
    }
}
