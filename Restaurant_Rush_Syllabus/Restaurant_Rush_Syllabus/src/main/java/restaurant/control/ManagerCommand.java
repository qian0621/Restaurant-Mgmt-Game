package restaurant.control;

/** What the manager typed, already split into a form the controller can act on. */
public class ManagerCommand {
    private final CommandKind kind;
    private final String verb;
    private final int targetId;
    private final String message;

    public ManagerCommand(CommandKind kind, String verb, int targetId, String message) {
        this.kind = kind;
        this.verb = verb;
        this.targetId = targetId;
        this.message = message;
    }

    public CommandKind getKind() {
        return kind;
    }

    /** The task word, such as "seat" (only for TASK). */
    public String getVerb() {
        return verb;
    }

    /** The customer number (only for TASK). */
    public int getTargetId() {
        return targetId;
    }

    /** The explanation to show (only for INVALID). */
    public String getMessage() {
        return message;
    }
}
