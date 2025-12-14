package core.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundPlayer {

    private static Clip clip;

    public static void playBackgroundMusic(String filePath) {
        new Thread(() -> {
            try {
                File audioFile = new File(filePath);

                // 1. Debug: Print the path being searched
                //System.out.println("[Sound] Looking for file at: " + audioFile.getAbsolutePath());

                if (!audioFile.exists()) {
                    System.err.println("[Sound] Error: File not found!");
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
                // 2. Debug: Print the actual error
                System.err.println("[Sound] Critical Error:");
                e.printStackTrace();
            }
        }).start();
    }

    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}