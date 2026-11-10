package restaurant.model;

/** Money helpers. All amounts in the game are integer cents; dollars are only formatted for display. */
public final class Money {
    private Money() { }

    /** Formats cents as dollars with two decimals, e.g. 1350 -> "$13.50". */
    public static String format(long cents) {
        return String.format("$%d.%02d", cents / 100, Math.abs(cents % 100));
    }

    /** percent% of cents, rounded half-up to the nearest cent. */
    public static int percentOf(int cents, int percent) {
        return (int) (((long) cents * percent + 50) / 100);
    }
}
