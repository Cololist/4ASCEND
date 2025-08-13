package client;

import javax.sound.sampled.*;

public class Sound {
    public static void play(String path){
        try{
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Sound.class.getResource(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
