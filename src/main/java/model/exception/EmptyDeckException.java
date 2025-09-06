package model.exception;
/**
 * Exception thrown when an attempt is made to draw a card from an empty deck
 */
public class EmptyDeckException extends IllegalStateException{
    public EmptyDeckException(String message) {
        super(message);
    }
}
