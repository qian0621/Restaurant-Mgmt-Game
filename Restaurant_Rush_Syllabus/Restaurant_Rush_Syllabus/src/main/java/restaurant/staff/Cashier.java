package restaurant.staff;

import restaurant.model.Customer;
import restaurant.model.Money;
import restaurant.model.Restaurant;

/** Automatically collects one payment per turn, oldest READY_TO_PAY customer first. */
public class Cashier extends Staff {
    public Cashier() {
        super("Cashier");
    }

    /** Returns a message if a payment was collected this turn, otherwise null. */
    public String collectPayment(Restaurant restaurant, int turn) {
        Customer c = restaurant.nextToPay(turn);
        if (c == null) {
            return null;
        }
        int bill = c.getOrder().getBillCents();
        int before = c.getSatisfaction();
        restaurant.collectPayment(c);
        int recorded = c.getRecordedSatisfaction();
        String review = "";
        if (recorded != before) {
            review = " [review adjusted satisfaction " + before + " -> " + recorded + "]";
        }
        return "Cashier collects " + Money.format(bill) + " from " + c + " (PAID, satisfaction "
                + recorded + " recorded" + review + ", table freed).";
    }
}
