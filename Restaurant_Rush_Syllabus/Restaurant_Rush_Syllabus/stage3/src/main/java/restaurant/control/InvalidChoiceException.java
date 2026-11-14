package restaurant.control;

/**
 * A checked exception meaning "the manager chose something the rules do not allow". Its message is
 * the reason shown to the manager. Because it is checked, every caller of StaffTask.validate must
 * decide what to do about a refused choice.
 */
public class InvalidChoiceException extends Exception {
    public InvalidChoiceException(String reason) {
        super(reason);
    }
}
