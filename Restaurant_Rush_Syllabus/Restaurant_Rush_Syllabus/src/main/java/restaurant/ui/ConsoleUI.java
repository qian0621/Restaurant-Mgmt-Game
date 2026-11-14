package restaurant.ui;

import java.util.List;
import java.util.Scanner;
import restaurant.control.CommandKind;
import restaurant.control.GameConfig;
import restaurant.control.GameQuitException;
import restaurant.control.GameResult;
import restaurant.control.GameUI;
import restaurant.control.ManagerCommand;
import restaurant.control.RoleOptions;
import restaurant.model.Customer;
import restaurant.model.Money;
import restaurant.model.Order;
import restaurant.model.Restaurant;
import restaurant.model.Table;

/**
 * Boundary class: reads the manager's decisions with a Scanner and prints the state. It contains no
 * game rules. Lines starting with '#' are ignored (comments in script files). With echo on, typed
 * input is repeated so that piped runs produce a readable transcript.
 */
public class ConsoleUI implements GameUI {
    private final Scanner in;
    private final boolean echo;

    public ConsoleUI(Scanner in, boolean echo) {
        this.in = in;
        this.echo = echo;
    }

    // ---------- input ----------

    /** Prints the prompt and returns the next line that is not a '#' comment. */
    private String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        while (true) {
            if (!in.hasNextLine()) {
                System.out.println();
                throw new GameQuitException("Input ended before the game finished.");
            }
            String line = in.nextLine();
            if (isComment(line)) {
                continue;
            }
            if (echo) {
                System.out.println(line);
            }
            return line;
        }
    }

    private static boolean isComment(String line) {
        Scanner words = new Scanner(line);
        return words.hasNext() && words.next().startsWith("#");
    }

    @Override
    public ManagerCommand promptStaff(String role, List<String> options) {
        String shown = "nothing to do (wait)";
        if (options.size() > 0) {
            shown = join(options, " | ");
        }
        System.out.println("  " + role + " options: " + shown);
        return parse(readLine("  " + role + "> "));
    }

    /** Asks whether to switch Happy Hour on. Enter, 'skip' or 'no' declines; 'hh' or 'yes' accepts. */
    @Override
    public boolean askHappyHour() {
        while (true) {
            String line = readLine("  Happy Hour ('hh' to activate, Enter to skip)> ");
            Scanner words = new Scanner(line);
            if (!words.hasNext()) {
                return false;
            }
            String answer = words.next().toUpperCase();
            if (!words.hasNext()) {
                if (answer.equals("HH") || answer.equals("YES") || answer.equals("Y")) {
                    return true;
                }
                if (answer.equals("SKIP") || answer.equals("NO") || answer.equals("N") || answer.equals("WAIT")) {
                    return false;
                }
                if (answer.equals("QUIT") || answer.equals("EXIT")) {
                    throw new GameQuitException("The manager quit the game.");
                }
            }
            showError("Type 'hh' to start Happy Hour or press Enter to skip.");
        }
    }

    /** Splits what was typed into words and builds a ManagerCommand. */
    private static ManagerCommand parse(String raw) {
        Scanner words = new Scanner(raw);
        if (!words.hasNext()) {
            return new ManagerCommand(CommandKind.WAIT, null, 0, null);
        }
        String first = words.next();
        String upper = first.toUpperCase();
        if (!words.hasNext()) {
            if (upper.equals("WAIT") || upper.equals("W")) {
                return new ManagerCommand(CommandKind.WAIT, null, 0, null);
            }
            if (upper.equals("HELP") || upper.equals("?")) {
                return new ManagerCommand(CommandKind.HELP, null, 0, null);
            }
            if (upper.equals("QUIT") || upper.equals("EXIT")) {
                return new ManagerCommand(CommandKind.QUIT, null, 0, null);
            }
            return notUnderstood(first);
        }
        String second = words.next();
        if (words.hasNext()) {
            return notUnderstood(first + " " + second + " ...");
        }
        try {
            int id = Integer.parseInt(second);
            return new ManagerCommand(CommandKind.TASK, first, id, null);
        } catch (NumberFormatException e) {
            return new ManagerCommand(CommandKind.INVALID, null, 0,
                    "'" + second + "' is not a customer number. Use e.g. 'seat 3'.");
        }
    }

    private static ManagerCommand notUnderstood(String text) {
        return new ManagerCommand(CommandKind.INVALID, null, 0,
                "Could not understand '" + text + "'. Type a task such as 'seat 3', 'wait', or 'help'.");
    }

    // ---------- output ----------

    @Override
    public void showWelcome(GameConfig config) {
        System.out.println(repeat('=', 78));
        System.out.println(" RESTAURANT RUSH - you are the manager");
        System.out.println(repeat('=', 78));
        System.out.println(" " + config.getMaxTurns() + " turns, " + config.getTableCount() + " tables. Win: >= "
                + config.getMinPaidCustomers() + " paid customers, revenue >= "
                + Money.format(config.getMinRevenueCents()) + ", average satisfaction >= "
                + config.getMinAverageSatisfaction() + ".");
        showHelp();
    }

    @Override
    public void showHelp() {
        System.out.println(" Each turn you give the Host, Waiter and Chef one task each (or 'wait'). Commands:");
        System.out.println("   seat <n>   Host or Waiter: seat queued customer n at a free table (both may seat, different customers)");
        System.out.println("   order <n>  Waiter: take seated customer n's order");
        System.out.println("   serve <n>  Waiter: serve customer n's READY order");
        System.out.println("   cook <n>   Chef: advance customer n's order by one preparation unit");
        System.out.println("   wait       do nothing with this staff member (an empty line works too)");
        System.out.println("   hh         (at the Happy Hour prompt) switch on Happy Hour, once per game: 2 turns, 20% off new orders,");
        System.out.println("              waiting customers recover 15 satisfaction at the start of each of those turns");
        System.out.println("   help, quit");
    }

    @Override
    public void showTurnHeader(int turn, int maxTurns) {
        System.out.println();
        System.out.println(repeat('=', 78));
        System.out.println(" TURN " + turn + " / " + maxTurns);
        System.out.println(repeat('=', 78));
    }

    @Override
    public void showEvents(String heading, List<String> events) {
        if (events.size() == 0) {
            return;
        }
        System.out.println(heading);
        for (String e : events) {
            System.out.println("  * " + e);
        }
    }

    @Override
    public void showError(String message) {
        System.out.println("  ! " + message + " Choose again.");
    }

    @Override
    public void showBoard(Restaurant r, int turn, int maxTurns, List<String> notices,
                          List<RoleOptions> available) {
        System.out.println(repeat('-', 78));
        System.out.println(" Turn " + turn + "/" + maxTurns + " | Revenue " + Money.format(r.getRevenueCents())
                + " | Avg satisfaction (paid) " + formatAverage(r.getPaidSatisfactionTotal(), r.getPaidCount()));
        System.out.println(" Paid " + r.getPaidCount() + " | Dishes served " + r.getServedCount()
                + " | Unhappy departures " + r.getUnhappyDepartures());
        for (String n : notices) {
            System.out.println(" " + n);
        }
        String queue = "";
        for (Customer c : r.getQueue().snapshot()) {
            if (queue.length() > 0) {
                queue = queue + ", ";
            }
            queue = queue + c + " (sat " + c.getSatisfaction() + ")";
        }
        if (queue.length() == 0) {
            queue = "(empty)";
        }
        System.out.println(" Waiting queue: " + queue);
        String tables = "";
        for (Table t : r.getTables()) {
            if (tables.length() > 0) {
                tables = tables + "  ";
            }
            String who = "free";
            if (!t.isFree()) {
                who = "#" + t.getOccupant().getId();
            }
            tables = tables + "T" + t.getNumber() + ": " + who;
        }
        System.out.println(" Tables: " + tables);
        System.out.println(" Customers:");
        System.out.println("   " + pad("#", 4) + " " + pad("Type", 8) + " " + pad("State", 13) + " "
                + pad("Sat", 4) + " Order");
        List<Customer> active = r.getActiveCustomers();
        for (Customer c : active) {
            System.out.println("   " + pad("" + c.getId(), 4) + " " + pad(c.getTypeName(), 8) + " "
                    + pad("" + c.getState(), 13) + " " + pad("" + c.getSatisfaction(), 4) + " "
                    + describeOrder(c.getOrder()));
        }
        if (active.size() == 0) {
            System.out.println("   (no customers in the restaurant)");
        }
        System.out.println(" Staff tasks available:");
        for (RoleOptions o : available) {
            String text = "-";
            if (o.getTasks().size() > 0) {
                text = join(o.getTasks(), " | ");
            }
            System.out.println("   " + pad(o.getRole(), 8) + " " + text);
        }
        System.out.println(repeat('-', 78));
    }

    @Override
    public void showTurnSummary(Restaurant r, int turn) {
        System.out.println(" End of turn " + turn + ": served " + r.getServedCount() + " | unhappy departures "
                + r.getUnhappyDepartures() + " | revenue " + Money.format(r.getRevenueCents())
                + " | avg satisfaction (paid) " + formatAverage(r.getPaidSatisfactionTotal(), r.getPaidCount())
                + " | turns used " + turn);
    }

    @Override
    public void showResult(GameConfig config, GameResult result) {
        System.out.println();
        System.out.println(repeat('=', 78));
        System.out.println(" GAME OVER after " + config.getMaxTurns() + " turns");
        System.out.println(repeat('=', 78));
        System.out.println(" Paid customers: " + result.getPaidCount() + " (need " + config.getMinPaidCustomers()
                + ") " + tick(result.isPaidTargetMet()));
        System.out.println(" Revenue: " + Money.format(result.getRevenueCents()) + " (need "
                + Money.format(config.getMinRevenueCents()) + ") " + tick(result.isRevenueTargetMet()));
        System.out.println(" Average satisfaction: " + formatAverage(result.getSatisfactionTotal(), result.getPaidCount())
                + " (need " + config.getMinAverageSatisfaction() + ") " + tick(result.isSatisfactionTargetMet()));
        if (result.isVictory()) {
            System.out.println(" *** VICTORY ***");
        } else {
            System.out.println(" *** DEFEAT ***");
        }
    }

    // ---------- small formatting helpers (no String.format needed) ----------

    private static String tick(boolean ok) {
        if (ok) {
            return "[met]";
        }
        return "[NOT met]";
    }

    /** total / count rounded half-up to one decimal using whole numbers, or "N/A" when count is 0. */
    private static String formatAverage(int total, int count) {
        if (count == 0) {
            return "N/A";
        }
        int tenths = (20 * total + count) / (2 * count);
        return (tenths / 10) + "." + (tenths % 10);
    }

    private static String describeOrder(Order o) {
        if (o == null) {
            return "-";
        }
        String progress = "";
        if (o.isCookable()) {
            progress = " (" + o.getRemainingUnits() + "/" + o.getItem().getPrepUnits() + " units left)";
        }
        return o.getItem().getName() + " " + Money.format(o.getBillCents()) + " [" + o.getStatus() + "]" + progress;
    }

    private static String repeat(char ch, int times) {
        String text = "";
        for (int i = 0; i < times; i++) {
            text = text + ch;
        }
        return text;
    }

    /** Pads with spaces on the right up to the given width (like %-8s). */
    private static String pad(String text, int width) {
        while (text.length() < width) {
            text = text + " ";
        }
        return text;
    }

    private static String join(List<String> parts, String separator) {
        String text = "";
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                text = text + separator;
            }
            text = text + parts.get(i);
        }
        return text;
    }
}
