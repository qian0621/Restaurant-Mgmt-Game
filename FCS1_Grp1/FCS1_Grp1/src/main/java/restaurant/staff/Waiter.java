package restaurant.staff;

import java.util.List;
import restaurant.model.Customer;

/** One task per turn: seat a queued customer, take an order, or serve a READY order. */
public class Waiter extends ManagedStaff {
    public Waiter() { super("Waiter"); }

    @Override
    public List<String> getVerbs() {
        return List.of(SeatCustomerTask.VERB, TakeOrderTask.VERB, ServeOrderTask.VERB);
    }

    @Override
    protected StaffTask createTask(String verb, Customer target) {
        return switch (verb) {
            case SeatCustomerTask.VERB -> new SeatCustomerTask(getRole(), target);
            case TakeOrderTask.VERB -> new TakeOrderTask(getRole(), target);
            default -> new ServeOrderTask(getRole(), target);
        };
    }
}
