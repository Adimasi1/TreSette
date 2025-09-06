package model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.cards.Card;
import model.exception.HandFullException;

/**
 * Mutable container for the cards currently held by a player.
 * 
 * The hand holds up to 10 {@link model.cards.Card} instances and provides
 * operations to add and remove cards and to reorder them. The cards are 
 * stored in {@code playerCards} and callers obtain can have just a read-only
 * access through {@link #getAllCards()}.
 */
public class Hand {
	
	private final List<Card> playerCards;
	/** the constructor initialize an Array with a capacity of 10 */
	public Hand() {
		this.playerCards = new ArrayList<>(10);
	}

	/**
	 * Add a card to the hand if capacity permits.
	 * @param card the {@link Card} to add to the hand
	 */
	public void addCard(Card card) {
		if (card == null) throw new IllegalArgumentException("Cannot add null card");
		if (playerCards.size() >= 10) {
			throw new HandFullException("Hand capacity 10 reached");
		}
		playerCards.add(card);
	}

	/**
	 * Remove a specific card instance from the hand.
	 * @param card the {@link Card} to remove
	 * @return {@code true} if the card was present and removed, {@code false} otherwise
	 */
	public boolean removeCard(Card card) {
		return playerCards.remove(card);
	}

	/**
	 * Reorder a card within the hand using indices.
	 * The {@code toIndex} is interpreted as a slot in the interval [0, size].
	 * @param fromIndex source index of the card
	 * @param toIndex destination index in the hand (may be equal to {@code size()})
	 */
	public void moveCard(int fromIndex, int toIndex) {
		int size = playerCards.size();
		if (fromIndex < 0 || fromIndex >= size) {
			throw new IndexOutOfBoundsException("Invalid fromIndex: " + fromIndex);
		}
		if (toIndex < 0 || toIndex > size) {
			throw new IndexOutOfBoundsException("Invalid toIndex: " + toIndex);
		}
		// no change conditions
		if (fromIndex == toIndex || (toIndex == size && fromIndex == size - 1)) return;
		Card cardToMove = playerCards.remove(fromIndex);
		// If moving rightwards, after removal the target slot shifts left by 1
		if (toIndex > fromIndex) toIndex--;
		playerCards.add(toIndex, cardToMove);
	}

	public List<Card> getAllCards() {
		return Collections.unmodifiableList(playerCards);
	}

	public List<String> getAllCardsCode() {
		return playerCards.stream()
						  .map(Card::getCode)
			              .toList();
	}

	public int size() {
		return playerCards.size();
	}

	public boolean isEmpty() {
		return playerCards.isEmpty();
	}

	/**
	 * Remove all cards from the hand.
	 */
	public void clear() {
		playerCards.clear();
	}
}
