package model.deal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import model.GameRules;
import model.board.Table;
import model.cards.Card;
import model.cards.CardSuit;
import model.cards.Deck;
import model.player.BotPlayer;
import model.player.Player;
import model.sign.SignManager;
import model.sign.SignType;
import model.events.DealSnapshot;
import model.events.ModelEvents;
import model.events.SignEvent;

/**
 * Abstract base for a single deal (mano) lifecycle.
 * 
 * This class holds the ordered {@code players}, a {@link Deck}, a
 * {@link Table}, the {@link SignManager} and scheduling helpers (timers and
 * {@code BotMoveScheduler}). It implements the deal lifecycle: start,
 * play resolution, trick resolution and end, and emits {@link ModelEvents}
 * for observers.
 */
@SuppressWarnings("deprecation")
public abstract class Deal extends Observable {

    protected final List<Player> players;          
    protected final Deck deck = new Deck();       
    protected final Table table = new Table();     
    private int currentIndex;                      
    private Player lastTrickWinner;                
    private boolean over = false;                  // Deal is Over
    private boolean paused = false;                // Deal paused
    private javax.swing.Timer trickResolutionTimer; // Timer for tock resolution
    private final SignManager signManager;         
    private final int dealIndex;                 
    protected static final int BOT_MOVE_DELAY_MS = 2000;  // Delay for bot moves
    private static final int TRICK_RESOLUTION_DELAY_MS = 1200;  // Delay for trick resolution
    protected final BotMoveScheduler botScheduler = new BotMoveScheduler(this, BOT_MOVE_DELAY_MS, null);

    /** Constructor for the Deal class.
     * @param dealIndex the index of the deal
     * @param players the list of players participating in the deal
     * It also initializes the sign manager with the players list.
     */
    protected Deal(int dealIndex, List<Player> players) {
        this.players = new ArrayList<>(players);
        this.signManager = new SignManager(this.players);
        this.dealIndex = dealIndex;
    }


    // ------------------ Deal Lifecycle ------------------
    /**
     * Starts the deal: resets players, shuffles the deck, deals cards,
     * determines the starting player, sends start events, and schedules the bot if needed
     */
    public void start() {
        resetPlayers();
        deck.shuffle();
        initialDeal();
        currentIndex = determineStartingPlayerIndex();
        setChanged();
        notifyObservers(new ModelEvents.DealStarted(takeGameSnapshot()));
        setChanged();
        notifyObservers(new ModelEvents.TrickStarted(takeGameSnapshot()));
        botScheduler.scheduleIfBotTurn();
    }

    /**
     * Allows a human player to play a card if it is their turn and the move is valid.
    * Validates the move, updates the state, and schedules the bot if needed.
    * @param player the human player attempting the play
    * @param card the card to play
    * @return {@code true} if the play was accepted and executed
     */
    public boolean playHumanCard(Player player, Card card) {
        if (over || paused || player != currentPlayer()) return false;
        if (!player.getHandCards().contains(card)) return false; // Cards not in hand
        if (!GameRules.isValidPlay(player, card, table)) return false; // based on palo
        executePlay(player, card);
        // Check if the game is over or if we need to schedule the bot's turn
        if (!over && table.size() != players.size()) botScheduler.scheduleIfBotTurn();
        return true;
    }
    /** This method is called from {@link BotMoveScheduler} after it's routine to make
     *  the bot to play a card.
     *  @param bot the bot player making the play
     *  @param card the card to play
     */
    public void playCardFromBot(BotPlayer bot, Card card) { executePlay(bot, card); }

    /**
     * Executes the play for the given player and card: removes the card from the player's hand,
     * adds it to the table, sends the card played event, and advances the turn or schedules trick resolution.
     * Used internally by both human and bots.
     * @param player the player making the play
     * @param card the card to play
     */
    private void executePlay(Player player, Card card) {
        player.playCard(card);
        table.addCard(player, card);
        setChanged();
        notifyObservers(new ModelEvents.CardPlayed(player.getId(), card.getCode(), card.toString(), takeGameSnapshot()));
        if (table.size() == players.size()) {
            scheduleTrickResolution();
        } else {
            advanceTurn();
        }
    }

    private void advanceTurn() { 
        currentIndex = (currentIndex + 1) % players.size();
    }

    /**
     * Schedules the resolution of the current trick using a timer, if all players have played.
     * This method is used internally after each play.
     */
    private void scheduleTrickResolution() {
        if (over || paused) return;
        if (trickResolutionTimer != null && trickResolutionTimer.isRunning()) return; // already running
        if (table.size() != players.size()) return; // not all players have played
        // it calls the actionPerformed method of the Timer listener after the delay,
        // invoking onTrickResolutionTimer
        trickResolutionTimer = new javax.swing.Timer(
            TRICK_RESOLUTION_DELAY_MS,
            this::onTrickResolutionTimer
        );
        trickResolutionTimer.setRepeats(false); 
        trickResolutionTimer.start();
    }
    // when the timer is expired, it triggers the resolution of the trick
    private void onTrickResolutionTimer(java.awt.event.ActionEvent event) {
        try {
            resolveTrick();
        // to ensure the timer is stopped after the trick resolution
        } finally {
            // after some checks it has been necessary to stop the timer, because
            // it happens some timers continue running even after the game is over
            if (trickResolutionTimer != null) {
                trickResolutionTimer.stop();
                trickResolutionTimer = null;
            }
        }
    }

    /**
     * Resolves the current trick: determines the winner, assigns won cards, updates state,
     * emits events, and either ends the deal or starts the next trick.
     * Called by the timer after all players have played their cards for the trick.
     */
    private void resolveTrick() {
        CardSuit palo = table.getPalo().orElseThrow(() ->
                        new IllegalStateException("Palo not found"));
        Player trickWinner = GameRules.getTrickWinner(table.getCardsOnTable(), palo);
        if (trickWinner == null) throw new IllegalStateException("Trick winner null");
        List<Card> trickCards = table.clearTableAndReturnCards();
        trickWinner.addWonCards(trickCards);
        lastTrickWinner = trickWinner;
        currentIndex = players.indexOf(trickWinner);
        signManager.onTrickEnded(); // ensure sign manager turn off the sign accessibility
        setChanged();
        notifyObservers(new ModelEvents.TrickEnded(takeGameSnapshot()));
        if (isDealFinished()) { // check if deal is finished according to variant rules
            endDeal();
        } else {
            setChanged();
            notifyObservers(new ModelEvents.TrickStarted(takeGameSnapshot()));
            botScheduler.scheduleIfBotTurn();
        }
    }

    private void endDeal() { 
        over = true;
        setChanged();
        notifyObservers(new ModelEvents.DealEnded(takeGameSnapshot())); 
    }
    private Player currentPlayer() { return players.get(currentIndex); }

    // Pauses or resumes the deal.
    public void setPaused(boolean paused) {
        boolean wasPaused = this.paused;
        this.paused = paused;
        if (paused) {
            botScheduler.cancel();
            if (trickResolutionTimer != null) {
                trickResolutionTimer.stop();
                trickResolutionTimer = null;
            }
        } else { // resume
            if (!over && table.size() == players.size()) {
                scheduleTrickResolution(); // all players have played, then schedule trick resolution
            } else if (wasPaused) {
                botScheduler.scheduleIfBotTurn(); // resume bot turn if was paused
            }
        }
    }

    // ------------------ Signs and Interaction ------------------
    public boolean canPlayerMakeSign(Player p) { 
        return signManager.canPlayerMakeSign(p, table, currentPlayer()); 
    }

    /**
     * Handles a player's attempt to send a sign: delegates to {@link SignManager}, delivers the
     * resulting sign event to bot players and sends the model event.
     * If the sign is not allowed a {@link SignEvent} with {@link SignType#NONE} may be returned.
    * @param player the player emitting the sign
    * @param type desired sign type (BUSSO / VOLO / LISCIO / NONE)
    * @return the concrete {@link SignEvent} produced
     */
    public SignEvent handlePlayerSign(Player player, SignType type) {
        SignEvent event = signManager.sendSign(player, type, table, currentPlayer());
        setChanged();
        notifyObservers(new ModelEvents.Sign(event, takeGameSnapshot()));
        return event;
    }

    // ------------------ Snapshot ------------------
    /** This methods prepare a Snapshot of the current game state
     *  using {@link DealSnapshot}.
     */
    private DealSnapshot takeGameSnapshot() {
        // each player's hand size and won cards count
        Map<String,Integer> handSizes = new LinkedHashMap<>();
        Map<String,Integer> wonCounts = new LinkedHashMap<>();
        for (Player p : players) {
            handSizes.put(p.getId(), p.getHandCards().size());
            wonCounts.put(p.getId(), p.getWonCards().size());
        }
        // cards on table
        List<String> tableCards = new ArrayList<>();
        for (Map.Entry<Player,Card> entry : table.getCardsOnTable().entrySet()) {
            tableCards.add(entry.getValue().getCode());
        }

        String lastWinnerId;
        if (lastTrickWinner == null) lastWinnerId = null;
        else lastWinnerId = lastTrickWinner.getId();

        boolean canSign = (!over && !paused && canPlayerMakeSign(currentPlayer()));

        // human player hand
        List<String> humanHand;
        if (players.isEmpty()) {
            humanHand = List.of();
        } else {
            humanHand = players.get(0).getHandCards().stream()
                        .map(Card::getCode)
                        .toList();
        }

    return new DealSnapshot(dealIndex,
        currentPlayer().getId(),
        handSizes,
        wonCounts,
        tableCards,
        lastWinnerId,
        canSign,
        paused,
        humanHand);
    }
    // ------------------ getters ------------------
    public boolean isOver() { return over; }
    public boolean isPaused() { return paused; }
    public Player getCurrentPlayer() { return currentPlayer(); }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public Player getLastTrickWinner() { return lastTrickWinner; }
    public int getDealIndex() { return dealIndex; }

    // ------------------ Utility methods ------------------
    private void resetPlayers() {
        for (Player p : players) p.resetForNewGame();
    }
    private void initialDeal() {
        for (int i = 0; i < GameRules.CARDS_PER_PLAYER; i++) {
            for (Player p : players) {
                p.addCard(deck.drawCard());
            }
        }
    }
    /** Determine which player index starts the deal.*/
    protected abstract int determineStartingPlayerIndex();
    /** End of deal condition */
    protected abstract boolean isDealFinished();

}
