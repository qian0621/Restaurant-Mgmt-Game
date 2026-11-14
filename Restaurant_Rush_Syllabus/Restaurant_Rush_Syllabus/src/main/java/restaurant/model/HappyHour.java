package restaurant.model;

/**
 * Pricing policy the manager may switch on once per game. It lasts for the turn it is activated
 * and the next one. While active, new orders get 20% off and customers still waiting for a table
 * or food recover 15 satisfaction at the start of the turn.
 */
public class HappyHour {
    public static final int DURATION_TURNS = 2;
    public static final int BILL_PERCENT = 80;
    public static final int RECOVERY_POINTS = 15;

    private boolean used;
    private int remainingTurns;

    /** Can still be switched on (it has not been used this game). */
    public boolean isAvailable() {
        return !used;
    }

    public boolean isActive() {
        return remainingTurns > 0;
    }

    /** Turns left, counting the current one while active. */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void activate() {
        if (used) {
            throw new GameRuleException("Happy Hour can only be used once per game");
        }
        used = true;
        remainingTurns = DURATION_TURNS;
    }

    /** Called once at the end of every turn. */
    public void endTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    /** Applies the discount to a bill that is being locked in now. */
    public int applyTo(int billCents) {
        if (isActive()) {
            return Money.percentOf(billCents, BILL_PERCENT);
        }
        return billCents;
    }
}
