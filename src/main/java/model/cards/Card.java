package model.cards;

import java.util.Objects;

/**
 * Represents a single playing card in the Tre Sette game
 * 
 * Each card has a suit and a value, and is immutable after creation
 * This class provides equality and hash code implementations based on suit and value
 */
public class Card {
	
	private final CardSuit suit;
	private final CardValue value;
	
	/**
	 * Create a new card with the given suit and value
	 * The created card is immutable.
	 * @param suit the card suit
	 * @param value the card value
	 */
	public Card(CardSuit suit, CardValue value) {
		this.suit = suit;
		this.value = value;
	}

	/* This override is necessary because Card is used as a key in maps,
	 * and the equality check depends on the suit and value
	 */
	@Override
	public boolean equals(Object obj) {
		// identity check
		if (this == obj) return true;
		// null check
		if (obj == null || getClass() != obj.getClass()) return false;
		// cast
		Card card = (Card) obj;
		// field equality checks
		if(suit != card.suit) return false;
		if(value != card.value) return false;
		return true;
	}

	public CardSuit getSuit() { return suit; }
	public CardValue getValue() { return value; }

	@Override
	/**
	 * This representation is made for the user, e.g. "Asso di Denari"
	 */
	public String toString() {
		return value.getValueName() + " di " + suit.getSuitName();
	}

	/**
	 * Stable code for this card.
	 * Format: "VALUE-SUIT" using enum names (e.g. "SETTE_DENARI").
	 * @return String card code
	 */
	public String getCode() {
		return value.name() + "_" + suit.name();
	}

	/**
	 * This override is necessary because Card is used as a key in maps
	 * and the equality check depends on the suit and value
	 */
	@Override
	public int hashCode() {
		// this code was in the previous version return suit.hashCode() + value.hashCode();
		return Objects.hash(suit, value);
	}
}

