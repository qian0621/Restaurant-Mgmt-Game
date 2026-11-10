package restaurant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import restaurant.control.GameConfig;
import restaurant.control.GameController;
import restaurant.control.GameQuitException;
import restaurant.ui.ConsoleUI;

/** Entry point. Use --echo when piping a script so the transcript shows the typed commands. */
public class Main {
    public static void main(String[] args) {
        boolean echo = false;
        for (String a : args) if (a.equals("--echo")) echo = true;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ConsoleUI ui = new ConsoleUI(in, System.out, echo);
        try {
            new GameController(GameConfig.base(), ui).run();
        } catch (GameQuitException e) {
            System.out.println(e.getMessage() + " Game ended early.");
        }
    }
}
