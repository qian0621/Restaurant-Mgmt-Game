package restaurant.model;

/** Money helpers. All amounts in the game are integer cents; dollars are only formatted for display. */
public final class Money {
    private Money() {
    }

    /** Formats cents as dollars with two decimals, e.g. 1350 -> "$13.50". */
    public static String format(int cents) {
        int dollars = cents / 100;
        int rest = cents % 100;
        String pad = "";
        if (rest < 10) {
            pad = "0";
        }
        return "$" + dollars + "." + pad + rest;
    }

    /** percent% of cents, rounded half-up to the nearest cent. */
    public static int percentOf(int cents, int percent) {
        return (cents * percent + 50) / 100;
    }
}
