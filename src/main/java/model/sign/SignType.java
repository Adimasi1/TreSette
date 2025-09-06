package model.sign;

/**
 * Placeholder SignType enum 
 * 
 * In Tressette, "Busso" means asking your partner to play their strongest card
 * in the current suit. "Volo" signals that you have no more cards of that suit
 * and are discarding. "Liscio" means you are playing a low card in the suit, 
 * showing you have nothing important to play. 
 * These signs help partners coordinate their strategy.
 */
public enum SignType {
    NONE,
    BUSSO,
    VOLO,
    LISCIO
}
