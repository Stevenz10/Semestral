package com.example.semestral;

// Importaciones necesarias para la actividad principal
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Clase MainActivity que implementa SensorEventListener
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Declaración de constantes para las acciones de la aplicación
    public static final String CHANNEL_ID = "MusicServiceChannel";
    public static final String ACTION_PLAY = "com.example.semestral.ACTION_PLAY";
    public static final String ACTION_NEXT = "com.example.semestral.ACTION_NEXT";
    public static final String ACTION_PREV = "com.example.semestral.ACTION_PREV";
    public static final String ACTION_ADD = "com.example.semestral.ACTION_ADD";

    // Declaración de constantes y variables para la administración de permisos y sensores
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isPlaying = false;
    private Intent musicServiceIntent;
    private static final int PICK_SONG_REQUEST = 1;
    private boolean wasPlaying = false; // Añade este campo para llevar un registro del estado de la reproducción

    // Método onCreate que se ejecuta cuando se crea la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.proximity);
        actionBar.setTitle("MyProximity");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        // Verificación de permisos para la lectura del almacenamiento externo
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, se solicita
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Asociación de la vista de la imagen y los botones con las variables correspondientes
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageResource(R.drawable.image);

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Este método se llama cuando se hace clic en el botón de reproducción.
                playMusic();
            }
        });

        // Asociación de los botones de pausa, siguiente, anterior y añadir con sus respectivos métodos
        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Este método se llama cuando se hace clic en el botón de pausa.
                pauseMusic();
            }
        });

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Este método se llama cuando se hace clic en el botón siguiente.
                nextTrack();
            }
        });

        Button prevButton = findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Este método se llama cuando se hace clic en el botón anterior.
                prevTrack();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Este método se llama cuando se hace clic en el botón añadir.
                openFilePicker();
            }
        });

        // Aquí se obtiene el servicio del sensor y se inicializa el sensor de proximidad.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            finish();  // Si no se encuentra el sensor de proximidad, se termina la actividad.
        }

        // Inicializa el Intent para el servicio de música.
        musicServiceIntent = new Intent(this, MusicService.class);

        // Crea el canal de notificación.
        createNotificationChannel();
    }

    // Método para reproducir música
    private void playMusic() {
        musicServiceIntent.setAction(ACTION_PLAY);
        startService(musicServiceIntent);
        MusicService service = MusicService.getInstance();
        if (service != null) {
            service.getPlayer().play();
        }
        Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
        isPlaying = true;
    }

    // Método para pausar la música
    private void pauseMusic() {
        MusicService service = MusicService.getInstance();
        if (service != null) {
            service.getPlayer().pause();
        }
        Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
        isPlaying = false;
    }

    // Método para avanzar a la siguiente pista
    private void nextTrack() {
        musicServiceIntent.setAction(ACTION_NEXT);
        startService(musicServiceIntent);
    }

    // Método para retroceder a la pista anterior
    private void prevTrack() {
        musicServiceIntent.setAction(ACTION_PREV);
        startService(musicServiceIntent);
    }

    // Método para añadir música
    private void addMusic(Uri songUri) {
        moveFile(songUri);
        musicServiceIntent.setAction(ACTION_ADD);
        musicServiceIntent.putExtra("uri", songUri.toString());
        startService(musicServiceIntent);
    }

    // Método para abrir el selector de archivos
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_SONG_REQUEST);
    }

    // Método que se ejecuta cuando se devuelve un resultado de una actividad que se inició con startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_SONG_REQUEST && resultCode == RESULT_OK) {
            Uri songUri = data.getData();
            File musicDir = new File(getExternalFilesDir(null), "/myMusicFolder/");

            moveFile(songUri);
            Toast.makeText(this, "File moved to " + musicDir.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            addMusic(Uri.fromFile(new File(musicDir, new File(songUri.getPath()).getName())));
        }
    }

    // Método para mover un archivo
    private void moveFile(Uri sourceUri) {
        String fileName = getRealFileName(sourceUri);
        if (fileName == null) {
            fileName = sourceUri.getLastPathSegment();
        }

        File musicDir = getExternalFilesDir("myMusicFolder");
        File destinationFile = new File(musicDir, fileName);

        try {
            try (InputStream in = getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
            }

            Log.d("MainActivity", "Moved file to " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to move file", e);
        }
    }

    // Método para obtener el nombre real de un archivo
    @SuppressLint("Range")
    private String getRealFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // Método que se ejecuta cuando se reanuda la actividad
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(musicServiceIntent);
    }


    // Método que se ejecuta cuando cambia el sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                playMusic();
            } else {
                pauseMusic();
            }
        }
    }

    // Método que se ejecuta cuando cambia la precisión del sensor
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No necesitamos esto para la aplicacion pero es necesario establecerlo.
    }

    // Método para crear el canal de notificación para el servicio de música
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MusicServiceChannel";
            String description = "Channel for Music Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
