package restaurant.staff;

/** A member of staff, identified by role. */
public abstract class Staff {
    private final String role;

    protected Staff(String role) { this.role = role; }

    public String getRole() { return role; }
}
