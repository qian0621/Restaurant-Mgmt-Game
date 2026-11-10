package restaurant.model;

/**
 * A combo of two existing menu items. Preparation units are the sum of the components; the price
 * is 90% of the combined price, rounded to the nearest cent. It is a MenuItem, so ordering,
 * cooking and billing treat it like any other dish.
 */
public class ComboMeal extends MenuItem {
    public static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), PRICE_PERCENT),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() { return first; }
    public MenuItem getSecond() { return second; }
}
