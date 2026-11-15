package restaurant.control;

public class GameQuitException extends RuntimeException {
    public GameQuitException(String message) {
        super(message);
    }
}
