package view.menu;

/** Holds user selections for new game setup */
public final class NewGameState {
    private String difficulty = "EASY"; // EASY / MEDIUM / HARD
    private int winningScore = 21; // default winning score limite

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }


    public int getWinningScore() { return winningScore; }
    public void setWinningScore(int winningScore) { this.winningScore = winningScore; }

    /** The label for the difficulty setting are in Italian.
     * @return the label for the difficulty setting */
    public String difficultyLabel() {
        if (difficulty == null) return "";
        return switch (difficulty) {
            case "EASY" -> "Facile";
            case "MEDIUM" -> "Medio";
            case "HARD" -> "Difficile";
            default -> difficulty;
        };
    }
}
