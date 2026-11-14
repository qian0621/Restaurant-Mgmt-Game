package restaurant.control;

import java.util.List;

/** The tasks one role can be given right now, ready for the UI to print. */
public class RoleOptions {
    private final String role;
    private final List<String> tasks;

    public RoleOptions(String role, List<String> tasks) {
        this.role = role;
        this.tasks = tasks;
    }

    public String getRole() {
        return role;
    }

    public List<String> getTasks() {
        return tasks;
    }
}
