package restaurant.model;

import java.util.ArrayList;
import java.util.List;

/** First-come-first-served list of customers waiting for a table. */
public class WaitingQueue {
    private final List<Customer> waiting = new ArrayList<>();

    void join(Customer c) {
        waiting.add(c);
    }

    void remove(Customer c) {
        waiting.remove(c);
    }

    /** A copy of the queue in arrival order. */
    public List<Customer> snapshot() {
        return new ArrayList<>(waiting);
    }

    public boolean isEmpty() {
        return waiting.size() == 0;
    }
}
