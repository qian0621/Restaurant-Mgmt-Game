package restaurant.control;

/** Thrown when the manager quits or the input ends before the game is over. */
public class GameQuitException extends RuntimeException {
    public GameQuitException(String message) {
        super(message);
    }
}
