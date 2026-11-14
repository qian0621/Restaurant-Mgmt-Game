package restaurant.model;

public final class Money {
    private Money() {
    }

    public static String format(int cents) {
        int dollars = cents / 100;
        int rest = cents % 100;
        String pad = "";
        if (rest < 10) {
            pad = "0";
        }
        return "$" + dollars + "." + pad + rest;
    }

    public static int percentOf(int cents, int percent) {
        return (cents * percent + 50) / 100;
    }
}
