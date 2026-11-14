package restaurant.model;

import java.util.ArrayList;
import java.util.List;

/** The ordered list of things a customer can order. */
public class Menu {
    private final List<MenuItem> offerings = new ArrayList<>();

    /** Adds an item and returns the menu so calls can be chained. */
    public Menu add(MenuItem item) {
        offerings.add(item);
        return this;
    }

    /** A copy of the list, so callers cannot change the menu behind its back. */
    public List<MenuItem> getOfferings() {
        return new ArrayList<>(offerings);
    }

    /** The base menu required by the brief: Salad $9 (1 unit), Burger $15 (1 unit), Pasta $18 (2 units). */
    public static Menu standard() {
        Menu menu = new Menu();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        return menu;
    }
}
