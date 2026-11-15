package restaurant.staff;

import java.util.ArrayList;
import java.util.List;
import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Restaurant;

public abstract class ManagedStaff extends Staff {
    protected ManagedStaff(String role) {
        super(role);
    }

    public abstract List<String> getVerbs();

    protected abstract StaffTask createTask(String verb, Customer target);

    public StaffTask taskFor(String verb, Customer target) {
        for (String known : getVerbs()) {
            if (known.toUpperCase().equals(verb.toUpperCase())) {
                return createTask(known, target);
            }
        }
        return null;
    }

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
                }
            }
        }
        return result;
    }
}
