package util;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Utility to play background music safely.
 * Runs in a separate thread to avoid blocking the game loop.
 * * Fails silently if audio file is missing or errors occur, ensuring game stability.
 * * @author Serena N.
 * @version 1.0
 */
public class SoundPlayer {

    private static Clip clip;

    public static void playBackgroundMusic(String filePath) {
        new Thread(() -> {
            try {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    // Fail silently so the game doesn't crash on the professor's computer
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);

                // Optional: Lower volume by 10 decibels so it's background, not foreground
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f);

                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();

            } catch (Exception e) {
                // Ignore audio errors (Game must continue even if sound fails)
            }
        }).start();
    }

    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}