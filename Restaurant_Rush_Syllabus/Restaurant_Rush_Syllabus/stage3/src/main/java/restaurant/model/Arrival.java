package restaurant.model;

/** One scheduled arrival: on this turn a customer of this type walks in. */
public class Arrival {
    private final int turn;
    private final CustomerType type;

    public Arrival(int turn, CustomerType type) {
        this.turn = turn;
        this.type = type;
    }

    public int getTurn() {
        return turn;
    }

    public CustomerType getType() {
        return type;
    }
}
