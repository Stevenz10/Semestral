package com.example.semestral;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;

public class MusicService extends Service {

    private MusicPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player == null) {
            player = new MusicPlayer(this, R.raw.song1);
        }

        Notification notification = new Notification.Builder(this, MainActivity.CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Music is playing...")
                .setSmallIcon(R.drawable.image)
                .build();

        startForeground(1, notification);

        player.play();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.pause();
    }
}
