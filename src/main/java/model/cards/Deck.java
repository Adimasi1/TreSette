package model.cards;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deck of 40 Tre Sette cards (4 suits x 10 values).
 * 
 * Provides methods to shuffle the deck, draw cards, and check deck state.
 * Throws an exception if drawing from an empty deck.
 */
public class Deck {

	private final List<Card> cards;

	/**
	 * Create a new deck containing 40 Tre Sette cards.
	 * The deck is initialized in a fixed order. It calls {@link #shuffle()} to randomize.
	 */
	public Deck() {
		this.cards = new ArrayList<>(40);
		initializeDeck();
	}

	/**
	 * Draws the top card from the deck and returns it.
	 * This method is not currently used, because it supposed to be
	 * used in a 1v1 mode (not yet implemented).
	 * @return drawn card (never null)
	 */
	public Card drawCard() {
		if (cards.isEmpty()) {
			throw new model.exception.EmptyDeckException("Deck is empty - cannot draw a card.");
		}
		return cards.remove(cards.size() - 1);
	}
	/**
	 * The shuffling method relies on the Collections.shuffle() function
	 * to randomize the order of the cards in the deck.
	 */
	public void shuffle() {
	    Collections.shuffle(cards);
	}

	public int size() { return cards.size(); }

	public boolean isEmpty() { return cards.isEmpty(); }

	/*
	 * Initializes the deck with 40 Tre Sette cards.
	 */
	private void initializeDeck() {
		for (CardSuit suit : CardSuit.values()) {
			for (CardValue value : CardValue.values()) {
				cards.add(new Card(suit, value));
			}
		}
	}
}
