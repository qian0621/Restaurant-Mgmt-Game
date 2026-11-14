package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.Restaurant;

/** Advance one order by one preparation unit. May target an order the Waiter takes this same turn. */
public class CookOrderTask extends StaffTask {
    public static final String VERB = "cook";

    public CookOrderTask(String actorRole, Customer target) {
        super(actorRole, VERB, target);
    }

    @Override
    public void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException {
        Order o = getTarget().getOrder();
        if (o == null) {
            if (plan.willHaveOrder(getTarget())) {
                return;
            }
            throw new InvalidChoiceException("Customer #" + getTarget().getId() + " has no order yet.");
        }
        if (!o.isCookable()) {
            String why = "is " + o.getStatus();
            if (o.getStatus() == OrderStatus.READY) {
                why = "is already READY";
            }
            throw new InvalidChoiceException("Customer #" + getTarget().getId() + "'s "
                    + o.getItem().getName() + " " + why + ", nothing to cook.");
        }
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Order o = getTarget().getOrder();
        o.cookOneUnit();
        int total = o.getItem().getPrepUnits();
        int done = total - o.getRemainingUnits();
        String tail = "";
        if (o.getStatus() == OrderStatus.READY) {
            tail = " -> READY";
        }
        return getActorRole() + " cooks " + o.getItem().getName() + " for " + getTarget()
                + " (unit " + done + " of " + total + ")" + tail + ".";
    }
}
