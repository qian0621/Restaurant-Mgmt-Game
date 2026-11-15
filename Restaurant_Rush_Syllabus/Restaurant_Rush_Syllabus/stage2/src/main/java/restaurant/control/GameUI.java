package restaurant.control;

import java.util.List;
import restaurant.model.Restaurant;

public interface GameUI {
    void showWelcome(GameConfig config);

    void showHelp();

    void showTurnHeader(int turn, int maxTurns);

    void showEvents(String heading, List<String> events);

    void showBoard(Restaurant restaurant, int turn, int maxTurns, List<String> notices,
                   List<RoleOptions> available);

    ManagerCommand promptStaff(String role, List<String> options);

    void showError(String message);

    void showTurnSummary(Restaurant restaurant, int turn);

    void showResult(GameConfig config, GameResult result);
}
