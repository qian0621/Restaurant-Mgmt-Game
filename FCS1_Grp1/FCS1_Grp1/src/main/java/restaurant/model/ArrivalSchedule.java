package restaurant.model;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Which kind of customer (if any) arrives at the start of each turn. */
public class ArrivalSchedule {
    private final Map<Integer, CustomerType> arrivals = new TreeMap<>();

    public ArrivalSchedule add(int turn, CustomerType type) {
        arrivals.put(turn, type);
        return this;
    }

    public Optional<CustomerType> typeAt(int turn) { return Optional.ofNullable(arrivals.get(turn)); }
    public int size() { return arrivals.size(); }
}
