package restaurant.staff;

public abstract class Staff {
    private final String role;

    protected Staff(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
