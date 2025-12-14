package core.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundPlayer {

    private static Clip clip;
    private static boolean soundEnabled = true;

    public static void playBackgroundMusic(String filePath) {
        if (!soundEnabled) {
            return; // 如果音频已被禁用，直接返回
        }
        
        new Thread(() -> {
            try {
                File audioFile = new File(filePath);

                // 1. Debug: Print the path being searched
                //System.out.println("[Sound] Looking for file at: " + audioFile.getAbsolutePath());

                if (!audioFile.exists()) {
                    System.err.println("[Sound] Warning: Audio file not found. Sound disabled.");
                    soundEnabled = false;
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);

                // Lower volume
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f);

                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            } catch (Exception e) {
                // disable sound on exception
                System.err.println("[Sound] Warning: Audio system not supported in this environment. Sound disabled.");
                soundEnabled = false;
            }
        }).start();
    }

    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}