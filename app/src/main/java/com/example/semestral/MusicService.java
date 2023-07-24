package com.example.semestral;

// Importaciones necesarias para el servicio de música
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.net.Uri;
import android.widget.Toast;

// Clase MusicService
public class MusicService extends Service {

    // Declaración de variables para el servicio de música
    private MusicPlayer player;
    private static MusicService instance = null;

    // Método onBind
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Método onStartCommand
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player == null) {
            player = new MusicPlayer(this);
        }
        instance = this;

        String action = intent.getAction();

        // Comprobación de la acción y ejecución de la acción correspondiente
        if (MainActivity.ACTION_PLAY.equals(action)) {
            player.play();
        } else if (MainActivity.ACTION_NEXT.equals(action)) {
            player.nextTrack();
        } else if (MainActivity.ACTION_PREV.equals(action)) {
            player.prevTrack();
        } else if (MainActivity.ACTION_ADD.equals(action)) {
            Uri songUri = Uri.parse(intent.getStringExtra("uri"));
            player.addTrack(songUri);
            player.reloadSongs();  // Actualiza el reproductor con las nuevas canciones
        }

        // Creación de la notificación para el servicio de música
        Notification notification = new Notification.Builder(this, MainActivity.CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Now Playing: " + player.getCurrentSongName())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();

        // Inicia el servicio de música en primer plano
        startForeground(1, notification);

        return START_STICKY;
    }

    // Método onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
        Toast.makeText(this, "Music Service Stopped", Toast.LENGTH_SHORT).show();
    }

    // Método para obtener la instancia del servicio de música
    public static MusicService getInstance() {
        return instance;
    }

    // Método para obtener el reproductor de música
    public MusicPlayer getPlayer() {
        return player;
    }
}
