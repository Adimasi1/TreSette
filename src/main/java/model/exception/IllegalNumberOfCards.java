package model.exception;
/**
 * Exception thrown when an invalid number of cards is used in an operation.
 * (Currently not used.)
 */
public class IllegalNumberOfCards extends RuntimeException {
	public IllegalNumberOfCards(String message) {
		super(message);
	}
}
