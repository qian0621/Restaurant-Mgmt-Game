package restaurant.model;

public class HappyHour {
    public static final int DURATION_TURNS = 2;
    public static final int BILL_PERCENT = 80;
    public static final int RECOVERY_POINTS = 15;

    private boolean used;
    private int remainingTurns;

    public boolean isAvailable() {
        return !used;
    }

    public boolean isActive() {
        return remainingTurns > 0;
    }

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

    public void endTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    public int applyTo(int billCents) {
        if (isActive()) {
            return Money.percentOf(billCents, BILL_PERCENT);
        }
        return billCents;
    }
}
