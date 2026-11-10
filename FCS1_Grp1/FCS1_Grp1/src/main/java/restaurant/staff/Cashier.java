package restaurant.staff;

import java.util.Optional;
import restaurant.model.Customer;
import restaurant.model.Money;
import restaurant.model.Restaurant;

/** Automatically collects one payment per turn, oldest READY_TO_PAY customer first. */
public class Cashier extends Staff {
    public Cashier() { super("Cashier"); }

    /** Returns a message if a payment was collected this turn. */
    public Optional<String> collectPayment(Restaurant restaurant, int turn) {
        Optional<Customer> next = restaurant.nextToPay(turn);
        if (next.isEmpty()) return Optional.empty();
        Customer c = next.get();
        int bill = c.getOrder().getBillCents();
        int before = c.getSatisfaction();
        restaurant.collectPayment(c);
        int recorded = c.getRecordedSatisfaction();
        String review = recorded == before ? "" : " [review adjusted satisfaction " + before + " -> " + recorded + "]";
        return Optional.of("Cashier collects " + Money.format(bill) + " from " + c + " (PAID, satisfaction "
                + recorded + " recorded" + review + ", table freed).");
    }
}
