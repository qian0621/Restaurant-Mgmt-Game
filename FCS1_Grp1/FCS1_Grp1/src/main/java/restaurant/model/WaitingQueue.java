package restaurant.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** FIFO list of customers waiting for a table. */
public class WaitingQueue {
    private final LinkedList<Customer> waiting = new LinkedList<>();

    void join(Customer c) { waiting.addLast(c); }
    void remove(Customer c) { waiting.remove(c); }
    public List<Customer> snapshot() { return new ArrayList<>(waiting); }
    public boolean isEmpty() { return waiting.isEmpty(); }
}
