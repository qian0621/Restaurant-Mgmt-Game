package restaurant.model;

import java.util.ArrayList;
import java.util.List;

/** Which kind of customer (if any) arrives at the start of each turn. */
public class ArrivalSchedule {
    private final List<Arrival> arrivals = new ArrayList<>();

    /** Adds an arrival and returns the schedule so calls can be chained. */
    public ArrivalSchedule add(int turn, CustomerType type) {
        arrivals.add(new Arrival(turn, type));
        return this;
    }

    /** The type arriving on this turn, or null if nobody arrives. */
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
