package restaurant.staff;

import java.util.Optional;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.CustomerState;
import restaurant.model.Restaurant;
import restaurant.model.Table;

/** Seat one queued customer at a free table. */
public class SeatCustomerTask extends StaffTask {
    public static final String VERB = "seat";

    public SeatCustomerTask(String actorRole, Customer target) { super(actorRole, target); }

    @Override public String describe() { return VERB + " " + getTarget().getId(); }

    @Override
    public Optional<String> validate(Restaurant restaurant, TurnPlan plan) {
        Customer c = getTarget();
        if (c.getState() != CustomerState.WAITING) {
            return fail("Customer #" + c.getId() + " is not in the waiting queue (currently " + c.getState() + ").");
        }
        if (restaurant.freeTableCount() - plan.tablesClaimed() <= 0) {
            return fail("There is no free table to seat customer #" + c.getId() + ".");
        }
        return Optional.empty();
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Table t = restaurant.seat(getTarget());
        return getActorRole() + " seats " + getTarget() + " at table " + t.getNumber() + ".";
    }

    @Override public boolean claimsTable() { return true; }

    @Override
    public boolean conflictsWith(StaffTask other) {
        return other instanceof SeatCustomerTask && other.getTarget() == getTarget();
    }
}
