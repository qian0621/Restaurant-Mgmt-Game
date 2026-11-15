package restaurant.control;

public class InvalidChoiceException extends Exception {
    public InvalidChoiceException(String reason) {
        super(reason);
    }
}
