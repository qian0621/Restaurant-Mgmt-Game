package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.CustomerState;
import restaurant.model.Restaurant;
import restaurant.model.Table;

/** Seat one queued customer at a free table. */
public class SeatCustomerTask extends StaffTask {
    public static final String VERB = "seat";

    public SeatCustomerTask(String actorRole, Customer target) {
        super(actorRole, VERB, target);
    }

    @Override
    public void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException {
        Customer c = getTarget();
        if (c.getState() != CustomerState.WAITING) {
            throw new InvalidChoiceException("Customer #" + c.getId()
                    + " is not in the waiting queue (currently " + c.getState() + ").");
        }
        if (restaurant.freeTableCount() - plan.tablesClaimed() <= 0) {
            throw new InvalidChoiceException("There is no free table to seat customer #" + c.getId() + ".");
        }
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Table t = restaurant.seat(getTarget());
        return getActorRole() + " seats " + getTarget() + " at table " + t.getNumber() + ".";
    }

    @Override
    public boolean claimsTable() {
        return true;
    }

    /** Two seating tasks for the same customer clash. */
    @Override
    public boolean conflictsWith(StaffTask other) {
        return other.getVerb().equals(VERB) && other.getTarget() == getTarget();
    }
}
