package restaurant.staff;

import restaurant.control.InvalidChoiceException;
import restaurant.control.TurnPlan;
import restaurant.model.Customer;
import restaurant.model.CustomerState;
import restaurant.model.Money;
import restaurant.model.Order;
import restaurant.model.Restaurant;

/** Take a seated customer's order. The customer decides what to eat; the choice is shown afterwards. */
public class TakeOrderTask extends StaffTask {
    public static final String VERB = "order";

    public TakeOrderTask(String actorRole, Customer target) {
        super(actorRole, VERB, target);
    }

    @Override
    public void validate(Restaurant restaurant, TurnPlan plan) throws InvalidChoiceException {
        Customer c = getTarget();
        if (c.getState() != CustomerState.SEATED) {
            throw new InvalidChoiceException("Customer #" + c.getId()
                    + " is not seated waiting to order (currently " + c.getState() + ").");
        }
    }

    @Override
    public String execute(Restaurant restaurant, int turn) {
        Order o = restaurant.takeOrder(getTarget());
        return getActorRole() + " takes " + getTarget() + "'s order: " + o.getItem().getName()
                + " (" + Money.format(o.getBillCents()) + ", " + o.getItem().getPrepUnits() + " prep unit(s)).";
    }

    @Override
    public boolean createsOrder() {
        return true;
    }
}
