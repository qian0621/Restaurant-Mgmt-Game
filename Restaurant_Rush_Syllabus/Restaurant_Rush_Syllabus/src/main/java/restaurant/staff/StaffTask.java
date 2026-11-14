package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

/**
 * One thing a member of staff can be told to do in a turn (the Command pattern). The manager
 * chooses tasks in step 2; they are validated then and executed later, in step 3. Each subclass
 * decides for itself what is allowed (validate) and what happens (execute).
 */
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

    /** Short text such as "seat 3", used in the lists of available tasks. */
    public String describe() {
        return verb + " " + target.getId();
    }

    /**
     * Returns normally if the task is allowed; otherwise throws InvalidChoiceException whose message
     * is the reason. Must not change any state.
     */
    public abstract void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException;

    /** Carries the task out and returns a one-line description of what happened. */
    public abstract String execute(Restaurant restaurant, int turn);

    /** True if this task uses up one free table (so two seating tasks cannot share the last table). */
    public boolean claimsTable() {
        return false;
    }

    /** True if this task creates the target customer's order this turn. */
    public boolean createsOrder() {
        return false;
    }

    /** True if this task cannot be planned together with {@code other}. */
    public boolean conflictsWith(StaffTask other) {
        return false;
    }
}
