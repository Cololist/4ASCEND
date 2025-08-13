package server;

import javax.sound.sampled.*;

public class BGM {
    private Clip clip;

    public void play(String path){
        try{
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(path));
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
