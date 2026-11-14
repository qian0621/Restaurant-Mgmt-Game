package restaurant.model;

/** A dish on the menu: name, price in cents and number of preparation units the Chef must spend. */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        if (priceCents < 0 || prepUnits < 1) {
            throw new IllegalArgumentException("price must be >= 0 and prep units >= 1");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }

    @Override
    public String toString() {
        return name;
    }
}
