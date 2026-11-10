package restaurant.staff;

import java.util.Optional;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.Restaurant;

/** Serve an order that was already READY when the turn began. */
public class ServeOrderTask extends StaffTask {
    public static final String VERB = "serve";

    public ServeOrderTask(String actorRole, Customer target) { super(actorRole, target); }

    @Override public String describe() { return VERB + " " + getTarget().getId(); }

    @Override
    public Optional<String> validate(Restaurant restaurant, TurnPlan plan) {
        Order o = getTarget().getOrder();
        if (o == null) {
            return fail("Customer #" + getTarget().getId() + " has no order to serve.");
        }
        if (o.getStatus() != OrderStatus.READY) {
            return fail("Customer #" + getTarget().getId() + "'s " + o.getItem().getName() + " is " + o.getStatus()
                    + ", not READY. A dish finished this turn can only be served next turn.");
        }
        return Optional.empty();
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Order o = getTarget().getOrder();
        restaurant.serve(getTarget(), turn);
        return getActorRole() + " serves " + o.getItem().getName() + " to " + getTarget() + " (SERVED).";
    }
}
