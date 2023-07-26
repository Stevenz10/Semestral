package com.example.semestral;

// Importaciones necesarias para el reproductor de música
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Clase MusicPlayer
public class MusicPlayer {
    // Declaración de variables para el reproductor de música
    private List<MediaPlayer> mediaPlayers;
    private List<File> musicFiles; // Añade esta línea para llevar un registro de los archivos de música
    private int currentTrack = 0;
    private Context context;

    // Constructor de la clase MusicPlayer
    public MusicPlayer(Context context) {
        this.context = context;
        mediaPlayers = new ArrayList<>();
        musicFiles = new ArrayList<>(); // Inicializa la lista de archivos de música
        addSongs();
    }

    // Método para añadir canciones al reproductor
    public void addSongs() {
        mediaPlayers.clear();
        musicFiles.clear(); // Asegúrate de limpiar la lista de archivos de música también

        Resources res = context.getResources();
        int song1 = res.getIdentifier("song1", "raw", context.getPackageName());
        MediaPlayer mediaPlayerRaw = MediaPlayer.create(context, song1);
        mediaPlayers.add(mediaPlayerRaw);

        // Añadiendo canciones de la carpeta externa
        File musicDir = new File(context.getExternalFilesDir(null), "/myMusicFolder/");
        boolean success = true;

        if (!musicDir.exists()) {
            success = musicDir.mkdir();
        }


        File[] files = musicDir.listFiles();
        if (files != null) {
            Log.d("MusicPlayer", "Found " + files.length + " files in myMusicFolder");
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file));
                    mediaPlayers.add(mediaPlayer);
                    musicFiles.add(file); // Añade el archivo a la lista de archivos de música
                }
            }
        } else {
            Log.d("MusicPlayer", "Could not list files in myMusicFolder");
        }
    }

    // Método para reproducir la música
    public void play() {
        if(!mediaPlayers.isEmpty() && !mediaPlayers.get(currentTrack).isPlaying()){
            mediaPlayers.get(currentTrack).start();
        }
    }

    // Método para pausar la música
    public void pause() {
        if(!mediaPlayers.isEmpty() && mediaPlayers.get(currentTrack).isPlaying()){
            mediaPlayers.get(currentTrack).pause();
        }
    }

    // Método para avanzar a la siguiente pista
    public void nextTrack() {
        if (!mediaPlayers.isEmpty() && mediaPlayers.size() > currentTrack + 1) {
            mediaPlayers.get(currentTrack).pause();
            currentTrack++;
            mediaPlayers.get(currentTrack).start();
        } else {
            Toast.makeText(context, "No more songs", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para retroceder a la pista anterior
    public void prevTrack() {
        if (!mediaPlayers.isEmpty() && currentTrack - 1 >= 0) {
            mediaPlayers.get(currentTrack).pause();
            currentTrack--;
            mediaPlayers.get(currentTrack).start();
        }
    }

    // Método para añadir una pista
    public void addTrack(Uri songUri) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, songUri);
        mediaPlayers.add(mediaPlayer);
        Toast.makeText(context, "Song added", Toast.LENGTH_SHORT).show();
    }

    // Método para liberar los recursos del reproductor
    public void release() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }
    }

    // Método para recargar las canciones
    public void reloadSongs() {
        mediaPlayers.clear();
        addSongs();
    }

    // Método para obtener la duración de la canción
    public int getDuration() {
        return mediaPlayers.get(currentTrack).getDuration();
    }

    // Método para obtener la posición actual de la canción
    public int getCurrentPosition() {
        return mediaPlayers.get(currentTrack).getCurrentPosition();
    }

    // Método para obtener el tiempo restante de la canción
    public int getRemainingTime() {
        return getDuration() - getCurrentPosition();
    }

    // Añade este método para obtener el nombre del archivo de la canción actual
    public String getCurrentSongName() {
        if (!musicFiles.isEmpty() && musicFiles.size() > currentTrack) {
            return musicFiles.get(currentTrack).getName();
        } else {
            return "Unknown";
        }
    }
}
