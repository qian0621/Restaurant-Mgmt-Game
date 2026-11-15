package restaurant.control;

import java.util.List;

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
