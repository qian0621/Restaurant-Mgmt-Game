package restaurant.model;

/**
 * Thrown when game code tries to do something the rules never allow, for example seating a customer
 * who is not waiting. It signals a programming mistake, not a wrong choice by the manager (wrong
 * choices are reported with InvalidChoiceException), so it is an unchecked exception.
 */
public class GameRuleException extends RuntimeException {
    public GameRuleException(String message) {
        super(message);
    }
}
