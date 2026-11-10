package restaurant.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import restaurant.control.GameConfig;
import restaurant.control.GameQuitException;
import restaurant.control.GameResult;
import restaurant.model.Customer;
import restaurant.model.Money;
import restaurant.model.Order;
import restaurant.model.Restaurant;
import restaurant.model.Table;

/**
 * Boundary class. It reads the manager's decisions and prints the state; it contains no game rules.
 * Lines starting with '#' are ignored (comments in script files). With echo on, typed input is
 * repeated so that piped runs produce a readable transcript.
 */
public class ConsoleUI {
    private static final String LINE = "-".repeat(78);

    private final BufferedReader in;
    private final PrintStream out;
    private final boolean echo;

    public ConsoleUI(BufferedReader in, PrintStream out, boolean echo) {
        this.in = in;
        this.out = out;
        this.echo = echo;
    }

    // ---------- input ----------
    private String readLine(String prompt) {
        try {
            out.print(prompt);
            out.flush();
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    out.println();
                    throw new GameQuitException("Input ended before the game finished.");
                }
                if (line.trim().startsWith("#")) {
                    continue;
                }
                if (echo) out.println(line);
                return line;
            }
        } catch (IOException e) {
            throw new GameQuitException("Could not read input: " + e.getMessage());
        }
    }

    public ManagerCommand promptStaff(String role, List<String> options) {
        String shown = options.isEmpty() ? "nothing to do (wait)" : String.join(" | ", options);
        out.println("  " + role + " options: " + shown);
        return ManagerCommand.parse(readLine("  " + role + "> "));
    }

    /** Asks whether to switch Happy Hour on this turn. Enter, 'skip' or 'no' declines. */
    public boolean askHappyHour() {
        while (true) {
            String answer = readLine("  Happy Hour ('hh' to activate, Enter to skip)> ").trim().toLowerCase();
            if (answer.equals("hh") || answer.equals("yes") || answer.equals("y")) return true;
            if (answer.isEmpty() || answer.equals("skip") || answer.equals("no") || answer.equals("n")
                    || answer.equals("wait")) return false;
            if (answer.equals("quit") || answer.equals("exit")) throw new GameQuitException("The manager quit the game.");
            showError("Type 'hh' to start Happy Hour or press Enter to skip.");
        }
    }

    // ---------- output ----------
    public void showWelcome(GameConfig config) {
        out.println("=".repeat(78));
        out.println(" RESTAURANT RUSH - you are the manager");
        out.println("=".repeat(78));
        out.printf(" %d turns, %d tables. Win: >= %d paid customers, revenue >= %s, average satisfaction >= %d.%n",
                config.getMaxTurns(), config.getTableCount(), config.getMinPaidCustomers(),
                Money.format(config.getMinRevenueCents()), config.getMinAverageSatisfaction());
        showHelp();
    }

    public void showHelp() {
        out.println(" Each turn you give the Host, Waiter and Chef one task each (or 'wait'). Commands:");
        out.println("   seat <n>   Host or Waiter: seat queued customer n at a free table (both may seat, different customers)");
        out.println("   order <n>  Waiter: take seated customer n's order");
        out.println("   serve <n>  Waiter: serve customer n's READY order");
        out.println("   cook <n>   Chef: advance customer n's order by one preparation unit");
        out.println("   wait       do nothing with this staff member (an empty line works too)");
        out.println("   hh         (at the Happy Hour prompt) switch on Happy Hour, once per game: 2 turns, 20% off new orders,");
        out.println("              waiting customers recover 15 satisfaction at the start of each of those turns");
        out.println("   help, quit");
    }

    public void showTurnHeader(int turn, int maxTurns) {
        out.println();
        out.println("=".repeat(78));
        out.printf(" TURN %d / %d%n", turn, maxTurns);
        out.println("=".repeat(78));
    }

    public void showEvents(String heading, List<String> events) {
        if (events.isEmpty()) return;
        out.println(heading);
        for (String e : events) out.println("  * " + e);
    }

    public void showError(String message) { out.println("  ! " + message + " Choose again."); }

    public void showMessage(String message) { out.println(message); }

    public void showBoard(Restaurant r, int turn, int maxTurns, List<String> notices,
                          Map<String, List<String>> availableTasks) {
        out.println(LINE);
        out.printf(" Turn %d/%d | Revenue %s | Avg satisfaction (paid) %s%n",
                turn, maxTurns, Money.format(r.getRevenueCents()), formatAverage(r.getAverageSatisfaction()));
        out.printf(" Paid %d | Dishes served %d | Unhappy departures %d%n",
                r.getPaidCount(), r.getServedCount(), r.getUnhappyDepartures());
        for (String n : notices) out.println(" " + n);
        StringBuilder queue = new StringBuilder();
        for (Customer c : r.getQueue().snapshot()) {
            queue.append(queue.length() == 0 ? "" : ", ").append(c).append(" (sat ").append(c.getSatisfaction()).append(")");
        }
        out.println(" Waiting queue: " + (queue.length() == 0 ? "(empty)" : queue));
        StringBuilder tables = new StringBuilder();
        for (Table t : r.getTables()) {
            tables.append(tables.length() == 0 ? "" : "  ").append("T").append(t.getNumber()).append(": ")
                    .append(t.isFree() ? "free" : "#" + t.getOccupant().getId());
        }
        out.println(" Tables: " + tables);
        out.println(" Customers:");
        out.printf("   %-4s %-8s %-13s %-4s %s%n", "#", "Type", "State", "Sat", "Order");
        boolean any = false;
        for (Customer c : r.getActiveCustomers()) {
            any = true;
            out.printf("   %-4d %-8s %-13s %-4d %s%n", c.getId(), c.getTypeName(), c.getState(),
                    c.getSatisfaction(), describeOrder(c.getOrder()));
        }
        if (!any) out.println("   (no customers in the restaurant)");
        out.println(" Staff tasks available:");
        for (Map.Entry<String, List<String>> e : availableTasks.entrySet()) {
            out.printf("   %-8s %s%n", e.getKey(), e.getValue().isEmpty() ? "-" : String.join(" | ", e.getValue()));
        }
        out.println(LINE);
    }

    public void showTurnSummary(Restaurant r, int turn) {
        out.println(" End of turn " + turn + ": served " + r.getServedCount() + " | unhappy departures "
                + r.getUnhappyDepartures() + " | revenue " + Money.format(r.getRevenueCents())
                + " | avg satisfaction (paid) " + formatAverage(r.getAverageSatisfaction())
                + " | turns used " + turn);
    }

    public void showResult(GameConfig config, GameResult result) {
        out.println();
        out.println("=".repeat(78));
        out.println(" GAME OVER after " + config.getMaxTurns() + " turns");
        out.println("=".repeat(78));
        out.printf(" Paid customers: %d (need %d) %s%n", result.paidCount(), config.getMinPaidCustomers(), tick(result.paidTargetMet()));
        out.printf(" Revenue: %s (need %s) %s%n", Money.format(result.revenueCents()),
                Money.format(config.getMinRevenueCents()), tick(result.revenueTargetMet()));
        out.printf(" Average satisfaction: %s (need %d) %s%n", formatAverage(result.averageSatisfaction()),
                config.getMinAverageSatisfaction(), tick(result.satisfactionTargetMet()));
        out.println(result.isVictory() ? " *** VICTORY ***" : " *** DEFEAT ***");
    }

    private static String tick(boolean ok) { return ok ? "[met]" : "[NOT met]"; }

    private static String formatAverage(OptionalDouble avg) {
        return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
    }

    private static String describeOrder(Order o) {
        if (o == null) return "-";
        String progress = o.isCookable() ? " (" + o.getRemainingUnits() + "/" + o.getItem().getPrepUnits() + " units left)" : "";
        return o.getItem().getName() + " " + Money.format(o.getBillCents()) + " [" + o.getStatus() + "]" + progress;
    }
}
