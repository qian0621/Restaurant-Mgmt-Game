package restaurant.control;

import java.util.List;
import restaurant.model.Restaurant;

/**
 * What the controller needs from a user interface. The controller depends on this interface and
 * not on a concrete console class (Dependency Inversion), so a different UI, or a scripted fake
 * UI for testing, can be plugged in without touching the controller.
 */
public interface GameUI {
    void showWelcome(GameConfig config);

    void showHelp();

    void showTurnHeader(int turn, int maxTurns);

    void showEvents(String heading, List<String> events);

    void showBoard(Restaurant restaurant, int turn, int maxTurns, List<String> notices,
                   List<RoleOptions> available);

    /** Shows the options for one role and returns what the manager typed. */
    ManagerCommand promptStaff(String role, List<String> options);

    /** Asks whether to switch Happy Hour on this turn. */
    boolean askHappyHour();

    void showError(String message);

    void showTurnSummary(Restaurant restaurant, int turn);

    void showResult(GameConfig config, GameResult result);
}
