package restaurant.control;

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

    public String getVerb() {
        return verb;
    }

    public int getTargetId() {
        return targetId;
    }

    public String getMessage() {
        return message;
    }
}
