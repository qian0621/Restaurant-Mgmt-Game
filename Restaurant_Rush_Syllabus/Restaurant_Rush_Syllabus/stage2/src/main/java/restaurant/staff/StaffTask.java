package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

public abstract class StaffTask {
    private final String actorRole;
    private final String verb;
    private final Customer target;

    protected StaffTask(String actorRole, String verb, Customer target) {
        this.actorRole = actorRole;
        this.verb = verb;
        this.target = target;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getVerb() {
        return verb;
    }

    public Customer getTarget() {
        return target;
    }

    public String describe() {
        return verb + " " + target.getId();
    }


    public abstract void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException;

    public abstract String execute(Restaurant restaurant, int turn);

    public boolean claimsTable() {
        return false;
    }

    public boolean createsOrder() {
        return false;
    }

    /** True if this task cannot be planned together with {@code other}. */
    public boolean conflictsWith(StaffTask other) {
        return false;
    }
}
