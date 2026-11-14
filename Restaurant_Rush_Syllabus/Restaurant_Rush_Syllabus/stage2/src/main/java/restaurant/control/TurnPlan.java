package restaurant.control;

import java.util.ArrayList;
import java.util.List;
import restaurant.model.Customer;
import restaurant.staff.StaffTask;

/**
 * The tasks the manager has chosen so far this turn (step 2). Tasks are kept in the order in which
 * staff act in step 3, and the plan answers the "what will already have happened" questions that
 * validation needs, such as how many tables are already spoken for.
 */
public class TurnPlan {
    private final List<StaffTask> tasks = new ArrayList<>();

    public void add(StaffTask task) {
        tasks.add(task);
    }

    public List<StaffTask> getTasks() {
        return new ArrayList<>(tasks);
    }

    /** How many free tables the planned tasks will use up. */
    public int tablesClaimed() {
        int n = 0;
        for (StaffTask t : tasks) {
            if (t.claimsTable()) {
                n++;
            }
        }
        return n;
    }

    /** True if a planned task will take this customer's order this turn. */
    public boolean willHaveOrder(Customer c) {
        for (StaffTask t : tasks) {
            if (t.createsOrder() && t.getTarget() == c) {
                return true;
            }
        }
        return false;
    }

    /** The already-planned task that clashes with {@code candidate}, or null if none does. */
    public StaffTask findConflict(StaffTask candidate) {
        for (StaffTask t : tasks) {
            if (t.conflictsWith(candidate) || candidate.conflictsWith(t)) {
                return t;
            }
        }
        return null;
    }

    public boolean conflicts(StaffTask candidate) {
        return findConflict(candidate) != null;
    }
}
