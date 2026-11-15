package restaurant.staff;

import java.util.Arrays;
import java.util.List;
import restaurant.model.Customer;

public class Waiter extends ManagedStaff {
    public Waiter() {
        super("Waiter");
    }

    @Override
    public List<String> getVerbs() {
        return Arrays.asList(SeatCustomerTask.VERB, TakeOrderTask.VERB, ServeOrderTask.VERB);
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
