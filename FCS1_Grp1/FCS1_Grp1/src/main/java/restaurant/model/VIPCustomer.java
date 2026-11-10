package restaurant.model;

/** Patient but demanding: loses only 5 satisfaction per waiting turn and gets 10% off the bill. */
public class VIPCustomer extends Customer {
    public static final int LOSS_PER_TURN = 5;
    public static final int BILL_PERCENT = 90;

    public VIPCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }

    @Override public String getTypeName() { return "VIP"; }
    @Override public int getLossPerWaitingTurn() { return LOSS_PER_TURN; }

    @Override
    public int billFor(MenuItem item) {
        return Money.percentOf(item.getPriceCents(), BILL_PERCENT);
    }
}
