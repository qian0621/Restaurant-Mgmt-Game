package restaurant.staff;

import java.util.Optional;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

/**
 * One thing a member of staff can be told to do in a turn (Command pattern). The manager chooses
 * tasks in step 2; they are validated then and executed later, in step 3.
 */
public abstract class StaffTask {
    private final String actorRole;
    private final Customer target;

    protected StaffTask(String actorRole, Customer target) {
        this.actorRole = actorRole;
        this.target = target;
    }

    public String getActorRole() { return actorRole; }
    public Customer getTarget() { return target; }

    /** Short text such as "seat 3", used in the lists of available tasks. */
    public abstract String describe();

    /** Empty if the task is allowed, otherwise the reason it is not. Must not change any state. */
    public abstract Optional<String> validate(Restaurant restaurant, TurnPlan plan);

    /** Carries the task out and returns a one-line description of what happened. */
    public abstract String execute(Restaurant restaurant, int turn);

    /** True if this task uses up one free table (so two seating tasks cannot share the last table). */
    public boolean claimsTable() { return false; }

    /** True if this task creates the target customer's order this turn. */
    public boolean createsOrder() { return false; }

    /** True if this task cannot be planned together with {@code other}. */
    public boolean conflictsWith(StaffTask other) { return false; }

    protected static Optional<String> fail(String reason) { return Optional.of(reason); }
}
