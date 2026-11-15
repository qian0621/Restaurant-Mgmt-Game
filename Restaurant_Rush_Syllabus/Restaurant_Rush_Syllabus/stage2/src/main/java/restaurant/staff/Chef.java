package restaurant.staff;

import java.util.Arrays;
import java.util.List;
import restaurant.model.Customer;

public class Chef extends ManagedStaff {
    public Chef() {
        super("Chef");
    }

    @Override
    public List<String> getVerbs() {
        return Arrays.asList(CookOrderTask.VERB);
    }

    @Override
    protected StaffTask createTask(String verb, Customer target) {
        return new CookOrderTask(getRole(), target);
    }
}
