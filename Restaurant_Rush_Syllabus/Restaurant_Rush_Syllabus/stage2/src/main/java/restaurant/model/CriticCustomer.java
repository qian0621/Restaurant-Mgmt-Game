package restaurant.model;

public class CriticCustomer extends Customer {
    public static final int LOSS_PER_TURN = 12;
    public static final int REVIEW_THRESHOLD = 60;
    public static final int REVIEW_SWING = 10;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public int getLossPerWaitingTurn() {
        return LOSS_PER_TURN;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem best = null;
        for (MenuItem item : menu.getOfferings()) {
            if (best == null || item.getPrepUnits() > best.getPrepUnits()) {
                best = item;
            }
        }
        return best;
    }

    @Override
    protected int satisfactionAtPayment() {
        int s = getSatisfaction();
        if (s >= REVIEW_THRESHOLD) {
            return Math.min(Customer.START_SATISFACTION, s + REVIEW_SWING);
        }
        return Math.max(0, s - REVIEW_SWING);
    }
}
