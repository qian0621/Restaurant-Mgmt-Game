package restaurant.model;

/**
 * Impatient (12 per waiting turn) and judgemental. Documented special behaviour:
 * (1) Order preference: always orders the most elaborate offering (most preparation units; the
 *     first such item on the menu if tied), which ties up the Chef.
 * (2) Reaction to service: the review is written when paying. If satisfaction is at least 60 the
 *     review is glowing (+10, max 100); otherwise it is harsh (-10, min 0). The adjusted value is
 *     what counts towards the average satisfaction of paid customers.
 */
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
