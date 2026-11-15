package restaurant.control;

public class GameResult {
    private final int paidCount;
    private final int revenueCents;
    private final int satisfactionTotal;
    private final boolean paidTargetMet;
    private final boolean revenueTargetMet;
    private final boolean satisfactionTargetMet;

    public GameResult(int paidCount, int revenueCents, int satisfactionTotal, boolean paidTargetMet,
                      boolean revenueTargetMet, boolean satisfactionTargetMet) {
        this.paidCount = paidCount;
        this.revenueCents = revenueCents;
        this.satisfactionTotal = satisfactionTotal;
        this.paidTargetMet = paidTargetMet;
        this.revenueTargetMet = revenueTargetMet;
        this.satisfactionTargetMet = satisfactionTargetMet;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getSatisfactionTotal() {
        return satisfactionTotal;
    }

    public boolean isPaidTargetMet() {
        return paidTargetMet;
    }

    public boolean isRevenueTargetMet() {
        return revenueTargetMet;
    }

    public boolean isSatisfactionTargetMet() {
        return satisfactionTargetMet;
    }

    public boolean isVictory() {
        return paidTargetMet && revenueTargetMet && satisfactionTargetMet;
    }
}
