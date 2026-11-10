package restaurant.staff;

import java.util.List;
import restaurant.model.Customer;

/** Seats at most one queued customer per turn, in addition to the Waiter's own task. */
public class Host extends ManagedStaff {
    public Host() { super("Host"); }

    @Override public List<String> getVerbs() { return List.of(SeatCustomerTask.VERB); }

    @Override
    protected StaffTask createTask(String verb, Customer target) {
        return new SeatCustomerTask(getRole(), target);
    }
}
