package restaurant.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

/**
 * Staff who carry out at most one task per turn, chosen by the manager. Subclasses only say which
 * verbs they understand and how to build the task; validation lives in the tasks.
 */
public abstract class ManagedStaff extends Staff {
    protected ManagedStaff(String role) { super(role); }

    /** Verbs this role understands, e.g. "seat", "order", "serve". */
    public abstract List<String> getVerbs();

    protected abstract StaffTask createTask(String verb, Customer target);

    public Optional<StaffTask> taskFor(String verb, Customer target) {
        if (!getVerbs().contains(verb)) return Optional.empty();
        return Optional.of(createTask(verb, target));
    }

    /** Every task this member of staff could legally be given right now. */
    public List<StaffTask> availableTasks(Restaurant restaurant, TurnPlan plan) {
        List<StaffTask> result = new ArrayList<>();
        for (Customer c : restaurant.getActiveCustomers()) {
            for (String verb : getVerbs()) {
                StaffTask task = createTask(verb, c);
                if (task.validate(restaurant, plan).isEmpty() && !plan.conflicts(task)) result.add(task);
            }
        }
        return result;
    }
}
