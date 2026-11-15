package restaurant.control;

import java.util.ArrayList;
import java.util.List;
import restaurant.model.Customer;
import restaurant.staff.StaffTask;

public class TurnPlan {
    private final List<StaffTask> tasks = new ArrayList<>();

    public void add(StaffTask task) {
        tasks.add(task);
    }

    public List<StaffTask> getTasks() {
        return new ArrayList<>(tasks);
    }

    public int tablesClaimed() {
        int n = 0;
        for (StaffTask t : tasks) {
            if (t.claimsTable()) {
                n++;
            }
        }
        return n;
    }

    public boolean willHaveOrder(Customer c) {
        for (StaffTask t : tasks) {
            if (t.createsOrder() && t.getTarget() == c) {
                return true;
            }
        }
        return false;
    }

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
