package restaurant.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The ordered list of things a customer can order. */
public class Menu {
    private final List<MenuItem> offerings = new ArrayList<>();

    public Menu add(MenuItem item) {
        offerings.add(item);
        return this;
    }

    public List<MenuItem> getOfferings() {
        return Collections.unmodifiableList(offerings);
    }

    /** The base menu required by the brief. */
    public static Menu standard() {
        return new Menu()
                .add(new MenuItem("Salad", 900, 1))
                .add(new MenuItem("Burger", 1500, 1))
                .add(new MenuItem("Pasta", 1800, 2));
    }
}
