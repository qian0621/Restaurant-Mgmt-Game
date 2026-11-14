package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.Restaurant;

/** Serve an order that was already READY when the turn began. */
public class ServeOrderTask extends StaffTask {
    public static final String VERB = "serve";

    public ServeOrderTask(String actorRole, Customer target) {
        super(actorRole, VERB, target);
    }

    @Override
    public void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException {
        Order o = getTarget().getOrder();
        if (o == null) {
            throw new InvalidChoiceException("Customer #" + getTarget().getId() + " has no order to serve.");
        }
        if (o.getStatus() != OrderStatus.READY) {
            throw new InvalidChoiceException("Customer #" + getTarget().getId() + "'s " + o.getItem().getName()
                    + " is " + o.getStatus() + ", not READY. A dish finished this turn can only be served next turn.");
        }
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Order o = getTarget().getOrder();
        restaurant.serve(getTarget(), turn);
        return getActorRole() + " serves " + o.getItem().getName() + " to " + getTarget() + " (SERVED).";
    }
}
