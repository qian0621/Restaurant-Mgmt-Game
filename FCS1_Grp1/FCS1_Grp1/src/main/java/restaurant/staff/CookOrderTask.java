package restaurant.staff;

import java.util.Optional;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.Restaurant;

/** Advance one order by one preparation unit. May target an order the Waiter takes this same turn. */
public class CookOrderTask extends StaffTask {
    public static final String VERB = "cook";

    public CookOrderTask(String actorRole, Customer target) { super(actorRole, target); }

    @Override public String describe() { return VERB + " " + getTarget().getId(); }

    @Override
    public Optional<String> validate(Restaurant restaurant, TurnPlan plan) {
        Order o = getTarget().getOrder();
        if (o == null) {
            if (plan.willHaveOrder(getTarget())) return Optional.empty();
            return fail("Customer #" + getTarget().getId() + " has no order yet.");
        }
        if (!o.isCookable()) {
            String why = o.getStatus() == OrderStatus.READY ? "is already READY" : "is " + o.getStatus();
            return fail("Customer #" + getTarget().getId() + "'s " + o.getItem().getName() + " " + why + ", nothing to cook.");
        }
        return Optional.empty();
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Order o = getTarget().getOrder();
        o.cookOneUnit();
        int total = o.getItem().getPrepUnits();
        int done = total - o.getRemainingUnits();
        String tail = o.getStatus() == OrderStatus.READY ? " -> READY" : "";
        return getActorRole() + " cooks " + o.getItem().getName() + " for " + getTarget()
                + " (unit " + done + " of " + total + ")" + tail + ".";
    }
}
