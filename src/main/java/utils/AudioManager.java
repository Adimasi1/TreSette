package utils;

import javax.sound.sampled.*;

/**
 * Simple utility to play short game audio clips from the classpath.
 * Each play method opens a fresh Clip so sounds can overlap.
 */
public class AudioManager {
    
    /** Play the "click" UI sound. */
    public static void playClick() {
        try {
            // Fresh clip each call so sounds can overlap
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/click.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Auto close clip when finished to free memory
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors
        }
    }
    
    /** Play the winner sound. */
    public static void playWinner() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/winner.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Auto close clip when finished
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the intro music/sound. */
    public static void playIntro() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/intro.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Auto close clip when finished
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the game over sound. */
    public static void playGameOver() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/game_over.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Auto close clip when finished
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the swapping sound effect. */
    public static void playSwapping() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/swapping.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the shuffle sound effect. */
    public static void playShuffle() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/shuffle.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the card playing sound effect. */
    public static void playPlayingCard() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/playing_card.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the knock (busso) sound effect. */
    public static void playKnock() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/knock.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
    
    /** Play the flying (volo) sound effect. */
    public static void playFlying() {
        try {
            java.net.URL soundUrl = AudioManager.class.getResource("/audio/flying.wav");
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Ignore audio errors silently
        }
    }
}