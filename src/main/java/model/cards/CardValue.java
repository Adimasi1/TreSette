package model.cards;

/**
 * Enum representing the possible values for a card in Tre Sette.
 * Note: getGameValue() returns the value used for determining winning cards,
 * while getPoints() returns the value used for scoring.
 */
public enum CardValue {
    ASSO("Asso", 8, 1.0), DUE("Due", 9, 0.33), TRE("Tre", 10, 0.33), QUATTRO("Quattro", 1, 0.0),
    CINQUE("Cinque", 2, 0.0), SEI("Sei", 3, 0.0), SETTE("Sette", 4, 0.0), FANTE("Fante", 5, 0.33),
    CAVALLO("Cavallo", 6, 0.33), RE("Re", 7, 0.33);

    private final String name;
    private final int gameValue;
    private final double points;
    CardValue(String name, int gameValue, double points) {
        this.name = name;
        this.gameValue = gameValue;
        this.points = points;
    }

    public String getValueName() { return name; }
    public int getGameValue() { return gameValue; }
    public double getPoints() { return points; }
}

