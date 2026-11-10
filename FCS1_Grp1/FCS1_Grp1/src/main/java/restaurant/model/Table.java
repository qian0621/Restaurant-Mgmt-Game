package restaurant.model;

/** A table seats at most one customer at a time. */
public class Table {
    private final int number;
    private Customer occupant;

    public Table(int number) { this.number = number; }

    public int getNumber() { return number; }
    public Customer getOccupant() { return occupant; }
    public boolean isFree() { return occupant == null; }

    void occupy(Customer customer) {
        if (occupant != null) throw new IllegalStateException("Table " + number + " is occupied");
        occupant = customer;
    }

    void release() { occupant = null; }
}
