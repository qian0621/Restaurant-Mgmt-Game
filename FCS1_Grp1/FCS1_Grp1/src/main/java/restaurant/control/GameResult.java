package restaurant.control;

import java.util.OptionalDouble;

/** The outcome of the objective check after the last turn. */
public record GameResult(int paidCount, int revenueCents, OptionalDouble averageSatisfaction,
                         boolean paidTargetMet, boolean revenueTargetMet, boolean satisfactionTargetMet) {

    public boolean isVictory() { return paidTargetMet && revenueTargetMet && satisfactionTargetMet; }
}
