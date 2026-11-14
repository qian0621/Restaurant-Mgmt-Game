package restaurant;

import java.util.Scanner;
import restaurant.control.GameConfig;
import restaurant.control.GameController;
import restaurant.control.GameQuitException;
import restaurant.ui.ConsoleUI;

/** Entry point. Use --echo when piping a script so the transcript shows the typed commands. */
public class Main {
    public static void main(String[] args) {
        boolean echo = false;
        for (String a : args) {
            if (a.equals("--echo")) {
                echo = true;
            }
        }
        Scanner in = new Scanner(System.in);
        ConsoleUI ui = new ConsoleUI(in, echo);
        try {
            new GameController(GameConfig.base(), ui).run();
        } catch (GameQuitException e) {
            System.out.println(e.getMessage() + " Game ended early.");
        }
    }
}
