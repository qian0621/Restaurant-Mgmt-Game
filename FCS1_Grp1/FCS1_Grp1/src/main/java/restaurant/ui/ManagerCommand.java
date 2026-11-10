package restaurant.ui;

/** What the manager typed, parsed into a form the controller can act on. */
public record ManagerCommand(Kind kind, String verb, int targetId, String message) {

    public enum Kind { TASK, WAIT, HELP, QUIT, INVALID }

    public static ManagerCommand parse(String raw) {
        String text = raw.trim().toLowerCase();
        if (text.isEmpty() || text.equals("wait") || text.equals("w")) return new ManagerCommand(Kind.WAIT, null, 0, null);
        if (text.equals("help") || text.equals("?")) return new ManagerCommand(Kind.HELP, null, 0, null);
        if (text.equals("quit") || text.equals("exit")) return new ManagerCommand(Kind.QUIT, null, 0, null);
        String[] parts = text.split("\\s+");
        if (parts.length == 2) {
            try {
                int id = Integer.parseInt(parts[1].replace("#", ""));
                return new ManagerCommand(Kind.TASK, parts[0], id, null);
            } catch (NumberFormatException e) {
                return new ManagerCommand(Kind.INVALID, null, 0,
                        "'" + parts[1] + "' is not a customer number. Use e.g. 'seat 3'.");
            }
        }
        return new ManagerCommand(Kind.INVALID, null, 0,
                "Could not understand '" + raw.trim() + "'. Type a task such as 'seat 3', 'wait', or 'help'.");
    }
}
