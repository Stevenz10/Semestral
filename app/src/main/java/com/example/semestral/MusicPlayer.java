package com.example.semestral;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicPlayer {
    private MediaPlayer mediaPlayer;

    public MusicPlayer(Context context, int resId) {
        mediaPlayer = MediaPlayer.create(context, resId);
    }

    public void play() {
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    public void pause() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getRemainingTime() {
        return getDuration() - getCurrentPosition();
    }
}
