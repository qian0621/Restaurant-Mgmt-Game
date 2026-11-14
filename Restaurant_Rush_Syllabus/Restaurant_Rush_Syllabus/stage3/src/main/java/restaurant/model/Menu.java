package restaurant.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final List<MenuItem> offerings = new ArrayList<>();

    public Menu add(MenuItem item) {
        offerings.add(item);
        return this;
    }

    public List<MenuItem> getOfferings() {
        return new ArrayList<>(offerings);
    }

    public static Menu standard() {
        Menu menu = new Menu();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        return menu;
    }
}
