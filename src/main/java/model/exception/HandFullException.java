package model.exception;
/**
 * Exception thrown when an attempt is made to add a card to a hand that is already full.
 */
public class HandFullException extends RuntimeException {
	public HandFullException(String message) {
		super(message);
	}
}
