package restaurant.staff;

import java.util.ArrayList;
import java.util.List;
import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

/**
 * Staff who carry out at most one task per turn, chosen by the manager. Subclasses only say which
 * verbs they understand and how to build the task (an abstract class with abstract methods); the
 * checking of rules lives in the tasks.
 */
public abstract class ManagedStaff extends Staff {
    protected ManagedStaff(String role) {
        super(role);
    }

    /** Verbs this role understands, e.g. "seat", "order", "serve". */
    public abstract List<String> getVerbs();

    /** Builds the task for a verb that getVerbs() contains. */
    protected abstract StaffTask createTask(String verb, Customer target);

    /** The task for a typed verb (any letter case), or null if this role does not understand it. */
    public StaffTask taskFor(String verb, Customer target) {
        for (String known : getVerbs()) {
            if (known.toUpperCase().equals(verb.toUpperCase())) {
                return createTask(known, target);
            }
        }
        return null;
    }

    /** Every task this member of staff could legally be given right now. */
    public List<StaffTask> availableTasks(Restaurant restaurant, TurnPlan plan) {
        List<StaffTask> result = new ArrayList<>();
        for (Customer c : restaurant.getActiveCustomers()) {
            for (String verb : getVerbs()) {
                StaffTask task = createTask(verb, c);
                try {
                    task.validate(restaurant, plan);
                    if (!plan.conflicts(task)) {
                        result.add(task);
                    }
                } catch (InvalidChoiceException e) {
                    // not allowed right now, so it is simply not offered
                }
            }
        }
        return result;
    }
}
