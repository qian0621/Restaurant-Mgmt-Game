package restaurant.control;

import restaurant.model.ArrivalSchedule;
import restaurant.model.CustomerType;
import restaurant.model.Menu;
import restaurant.model.Restaurant;

public class GameConfig {
    private final int maxTurns;
    private final int tableCount;
    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final int minAverageSatisfaction;
    private final ArrivalSchedule schedule;

    public GameConfig(int maxTurns, int tableCount, int minPaidCustomers, int minRevenueCents,
                      int minAverageSatisfaction, ArrivalSchedule schedule) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAverageSatisfaction = minAverageSatisfaction;
        this.schedule = schedule;
    }

    public static GameConfig base() {
        ArrivalSchedule schedule = new ArrivalSchedule();
        schedule.add(1, CustomerType.REGULAR);
        schedule.add(4, CustomerType.VIP);
        schedule.add(7, CustomerType.REGULAR);
        schedule.add(10, CustomerType.CRITIC);
        schedule.add(13, CustomerType.REGULAR);
        schedule.add(16, CustomerType.REGULAR);
        return new GameConfig(18, 2, 4, 4500, 60, schedule);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public int getMinRevenueCents() {
        return minRevenueCents;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }

    public ArrivalSchedule getSchedule() {
        return schedule;
    }

    public Menu newMenu() {
        return Menu.standard();
    }

    public Restaurant newRestaurant() {
        return new Restaurant(newMenu(), tableCount);
    }

    public GameResult evaluate(Restaurant r) {
        boolean paidOk = r.getPaidCount() >= minPaidCustomers;
        boolean revenueOk = r.getRevenueCents() >= minRevenueCents;
        boolean satisfactionOk = r.getPaidCount() > 0
                && r.getPaidSatisfactionTotal() >= minAverageSatisfaction * r.getPaidCount();
        return new GameResult(r.getPaidCount(), r.getRevenueCents(), r.getPaidSatisfactionTotal(),
                paidOk, revenueOk, satisfactionOk);
    }
}
