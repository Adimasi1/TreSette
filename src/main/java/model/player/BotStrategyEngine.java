package model.player;

import java.util.*;

import model.GameDifficultyState;
import model.GameRules;
import model.board.Table;
import model.cards.Card;
import model.cards.CardSuit;
import model.cards.CardValue;
import model.events.SignEvent;
import model.sign.SignType;

/**
 * Bot strategic engine.
 * Same base logic for all difficulties; difficulty only changes randomness.
 */
final class BotStrategyEngine {
    private final Random random = new Random();
    // Difficulty constants: follow the heuristics or play randomly
    private final double actionNoise;   
    private final double signNoise;     
    // Value thresholds
    private static final int STRONG_GAME_VALUE = 8; // consider >=8 a strong card

    private CardSuit plannedBussoPalo; // remember BUSSO suit to possibly lead next time

    public BotStrategyEngine(GameDifficultyState difficulty){
        switch (difficulty) {
            case EASY -> { actionNoise = 0.60; signNoise = 0.60; }
            case MEDIUM -> { actionNoise = 0.35; signNoise = 0.35; }
            case HARD -> { actionNoise = 0.10; signNoise = 0.10; }
            default -> { actionNoise = 0.35; signNoise = 0.35; }
        }
    }

    /**
     * Choose a legal card to play. The same heuristic is used for every difficulty,
     * only the amount of randomness varies.
     */
    public Card chooseCard(Table table, List<Card> hand, Team team) {
        List<Card> legal = legalMoves(hand, table);
        if (legal.size() == 1) return legal.get(0);

        // With the Difficulty probability, ignore the heuristic and pick a random legal card
        if (random.nextDouble() < actionNoise) 
            return legal.get(random.nextInt(legal.size()));

        // 1) NO palo condition
        if (table.getPalo().isEmpty()) {
            // If the bot planned a BUSSO, try to lead that suit with the highest point card
            if (plannedBussoPalo != null) {
                Card chosen = highestPointsOfSuitOrLowest(plannedBussoPalo, legal);
                plannedBussoPalo = null; 
                return chosen;
            }
            // If the bot doesn't have a BUSSO plan, lead with the strongest card:
            // Ace or better
            // Otherwise play the weakest card to safely gather information.
            return findBestLeadCard(legal);
        }

        // 2) Palo condition
        CardSuit palo = table.getPalo().get();
        List<Card> onTable = new ArrayList<>(table.getCardsOnTable().values());
        Card currentWinning = GameRules.getWinningCard(onTable, palo);

        // 2.a) The team mate is winning
        //Determine if a teammate is currently winning the trick.
        boolean isMateWinning = false;
        if (team != null) {
            isMateWinning = table.getCardsOnTable().entrySet().stream()
                .anyMatch(e -> e.getValue().equals(currentWinning) // the card in entry is the currentWinning
                             && team.getMembers().contains(e.getKey())); // the owner is the team mate
        }

        // If a teammate is winning, play the card with the highest points
        if (isMateWinning) {
            boolean hasPaloSuitInHand = hand.stream().anyMatch(c -> c.getSuit() == palo);
            if (hasPaloSuitInHand) {
                // Yes,then follow suit. Play the lowest legal card of that suit to save better ones.
                return minByGameValue(legal);
            } else {
                // No, then discard. This is an opportunity to pass points.
                // Play the card with the highest point value.
                return legal.stream()
                            .max(Comparator.comparingDouble(c -> c.getValue().getPoints()))
                            .orElse(minByGameValue(legal)); // should not happen
            }
        }
        // 2.b) opponent is winning, try to win as cheaply as possible.
        List<Card> winning = legal.stream()
                                    .filter(c -> GameRules.cardBeats(c, currentWinning, palo))
                                    .toList();

        // If we can't win, discard the lowest card. If we can, win with the lowest possible winning card.
        if (winning.isEmpty()) return minByGameValue(legal);
        return minByGameValue(winning);
    }

    /** Decide whether to emit a sign this turn and which. */
    public SignType chooseSign(Table table, List<Card> hand){
        if(random.nextDouble() > 0.50
            || random.nextDouble() < signNoise
            ) return SignType.NONE;
        // Compute the ideal sign deterministically
        SignType ideal = computeIdealSign(hand);
        // Follow the ideal sign
        if (ideal != SignType.BUSSO) plannedBussoPalo = null;
        return ideal;
    }

    /** For future learning/adaptation (currently unused). */
    public void observeSign(SignEvent event){ /* no-op for now */ }

    // ------------------- Helpers -------------------
    private SignType computeIdealSign(List<Card> hand){
        // Try to announce control in a suit if we have it
        Optional<CardSuit> maybeSuit = selectBussoSuit(hand);
        if (maybeSuit.isPresent()){
            plannedBussoPalo = maybeSuit.get();
            return SignType.BUSSO;
        }

        // Otherwise choose between VOLO / LISCIO based on hand shape
        int strong = 0, pointCards = 0;
        for (Card c : hand) {
            if (isStrong(c)) strong++;
            if (c.getValue().getPoints() > 0) pointCards++;
        }
        if (pointCards >= 3) return SignType.LISCIO; //  The team has strong cards, play smooth
        if (strong == 0 && pointCards <= 2) return SignType.VOLO; // no strong cards: fluy
        return SignType.NONE;
    }

    private List<Card> legalMoves(List<Card> handCards, Table table){
        if (table.getPalo().isEmpty()) return handCards;
        CardSuit leading = table.getPalo().get();
        List<Card> match = new ArrayList<>();
        handCards.stream()
                 .filter(c -> c.getSuit() == leading)
                 .forEach(match::add);
        if(match.isEmpty()) return handCards;
        return match;
    }

    private boolean isStrong(Card c){
        return c.getValue().getGameValue() >= STRONG_GAME_VALUE;
    }
    private boolean isTopStrong(Card c){
        return c.getValue().getGameValue() >= STRONG_GAME_VALUE + 1;
    }

    private Card minByGameValue(List<Card> cards){
        return Collections.min(cards, Comparator.comparingInt(c -> c.getValue().getGameValue()));
    }

    private Card highestPointsOfSuitOrLowest(CardSuit suit, List<Card> legal){
        Card best = legal.stream()
                        .filter(c -> c.getSuit() == suit)
                        .max(Comparator.comparingDouble(c -> c.getValue().getPoints()))
                        .orElseGet(() -> minByGameValue(legal));
        return best;
    }

    private Optional<CardSuit> selectBussoSuit(List<Card> hand){
        Map<CardSuit,List<Card>> bySuit = new EnumMap<>(CardSuit.class);
        for(Card c : hand){
            bySuit.computeIfAbsent(c.getSuit(), k -> new ArrayList<>()).add(c);
        }

        CardSuit bestSuit = null;
        int bestStrongNumber = -1, bestTop = -1, bestSize = -1;
        for(Map.Entry<CardSuit,List<Card>> entry : bySuit.entrySet()){
            List<Card> list = entry.getValue();
            int strongNumber = 0;
            int top = 0;
            for (Card c : list){
                if (isStrong(c)) {
                    strongNumber++; 
                    if (isTopStrong(c)) top++; 
                }
            }
            boolean qualifies = (strongNumber >= 2); // hard-coded minimum of 2 strong cards bot rule
            if(!qualifies){
                // the bot has a top card and more than 1 for the suit
                if(top == 1 && list.size() >= 2) qualifies = true;
            }
            if(!qualifies) continue;

            int size = list.size();
            boolean better = false;
            if (strongNumber > bestStrongNumber) better = true;
            else if (strongNumber == bestStrongNumber){
                if (top > bestTop) better = true;  
                else if (top == bestTop && size > bestSize) better = true;
            }
            if (better){ 
                bestSuit = entry.getKey(); 
                bestStrongNumber = strongNumber; 
                bestTop = top; 
                bestSize = size; 
            }
        }
        return Optional.ofNullable(bestSuit);
    }

    private Card findBestLeadCard(List<Card> legal) {
        // Find the card with the highest game value
        Card strongestCard = Collections.max(legal, 
                                Comparator.comparingInt(c -> c.getValue().getGameValue()));

        // If the strongest card is an Ace or better (3 or 2), play it.
        if (strongestCard.getValue().getGameValue() >= CardValue.ASSO.getGameValue()) {
            return strongestCard;
        }

        // Otherwise, play the weakest card (lowest game value), following a conservative approach
        return minByGameValue(legal);
    }
}
