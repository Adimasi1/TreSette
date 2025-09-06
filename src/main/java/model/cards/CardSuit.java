package model.cards;

/**
 * Enum representing the four suits in a Tre Sette deck: Denari, Bastoni, Coppe, Spade.
 */
public enum CardSuit {
    DENARI("Denari"), BASTONI("Bastoni"),
	 COPPE("Coppe"), SPADE("Spade");

    private final String name;

    CardSuit(String name) { 
        this.name = name;
    }

	public String getSuitName() { return name; }
}
