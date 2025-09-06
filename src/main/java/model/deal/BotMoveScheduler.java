package model.deal;

import javax.swing.Timer;

import java.util.List;

import model.cards.Card;
import model.player.BotPlayer;
import model.player.Player;
import model.player.Team;
import model.sign.SignType;

/**
 * Schedules delayed bot moves for a {@link Deal}.
 * 
 * This is a helper that triggers a delayed bot action using a {@link javax.swing.Timer} so the
 * UI has a short pause between plays. 
 *
 * Responsibilities:
 * - Detect whether the current player is a bot.
 * - After a short delay, ask the bot for a card and invoke the parent
 *   {@link Deal#playCardFromBot} to execute the play.
 * - Optionally ask the bot to emit a sign before playing
 * - Avoid re-scheduling while a timer is already pending.
 */

public class BotMoveScheduler {
    private final Deal deal;        
    private final int delayMs; 
    private final List<Team> teams;    
    private Timer timer;           

    /**
     * Create a scheduler bound to a parent {@link Deal} and using the given delay.
     *
     * @param deal   the deal that will receive the bot play
     * @param delayMs delay in milliseconds before the bot action is executed
     * @param teams  for 2v2
     */
    public BotMoveScheduler(Deal deal, int delayMs, List<Team> teams) {
        this.deal = deal;
        this.delayMs = delayMs;
        this.teams = teams;
    }

    /**
     * Schedule a bot move if the current player is a bot.
     * 
     * If a timer is already pending, this method does nothing. If no bot is
     * currently active, the scheduler cancels any pending timer.
     * Otherwise, it schedules a new timer for the bot's turn and overrides
     * the actionPerformed method to handle the bot's play (card and sign)
     */
    public void scheduleIfBotTurn() {
        if (deal.isOver() || deal.isPaused()) return;
        BotPlayer bot = currentBot();
        if (bot == null) { cancel(); return; }
        if (timer != null && timer.isRunning()) return; 

        timer = new Timer(delayMs, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    BotPlayer current = currentBot();
                    if (current == null || deal.isOver() || deal.isPaused()) return;
                    maybeSendSign(current);
                    // Find the team for the current bot
                    Team botTeam = null;
                    if (teams != null) {
                        botTeam = teams.stream()
                                       .filter(team -> team.getMembers().contains(current))
                                       .findFirst()
                                       .orElse(null);
                    }
                    Card choice = current.decideCard(deal.table, botTeam);
                    deal.playCardFromBot(current, choice);
                } catch (Exception ignored) {
                    // The drawback is that any issues are silently ignored
                } finally {
                    // clear reference so new schedules are allowed
                    timer = null;
                    // chain if trick not complete and still bot turn
                    boolean dealRunning = !deal.isOver() && !deal.isPaused();
                    boolean trickComplete = deal.table.size() == deal.players.size();
                    if (dealRunning && !trickComplete) {
                        scheduleIfBotTurn();
                    }
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Cancel any pending bot move timer. */
    public void cancel() { 
        if (timer != null) { 
            timer.stop(); 
            timer = null; 
        } 
    }
    //---- Private helpers ----
    private BotPlayer currentBot() {
        Player player = deal.getCurrentPlayer();
        if (player instanceof BotPlayer bot) return bot;
        return null;
    }

    private void maybeSendSign(BotPlayer bot) {
        if (deal.canPlayerMakeSign(bot)) {
            SignType type = bot.decideSign(deal.table);
            if (type != null && type != SignType.NONE) {
                deal.handlePlayerSign(bot, type);
            }
        }
    }
}
