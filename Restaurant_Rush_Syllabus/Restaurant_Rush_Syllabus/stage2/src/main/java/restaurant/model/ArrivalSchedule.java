package restaurant.model;

import java.util.ArrayList;
import java.util.List;

public class ArrivalSchedule {
    private final List<Arrival> arrivals = new ArrayList<>();

    public ArrivalSchedule add(int turn, CustomerType type) {
        arrivals.add(new Arrival(turn, type));
        return this;
    }

    public CustomerType typeAt(int turn) {
        for (Arrival a : arrivals) {
            if (a.getTurn() == turn) {
                return a.getType();
            }
        }
        return null;
    }

    public int size() {
        return arrivals.size();
    }
}
