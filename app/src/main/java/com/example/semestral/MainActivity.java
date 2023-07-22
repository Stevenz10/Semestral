package com.example.semestral;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final String CHANNEL_ID = "MusicServiceChannel";

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private MusicPlayer player;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    private TextView tvTime;
    private ProgressBar progressBar;

    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            int remainingTime = player.getRemainingTime();
            int totalTime = player.getDuration();
            int currentTime = totalTime - remainingTime;
            int minutes = currentTime / 1000 / 60;
            int seconds = (currentTime / 1000) % 60;
            int totalMinutes = totalTime / 1000 / 60;
            int totalSeconds = (totalTime / 1000) % 60;

            tvTime.setText(String.format("%02d:%02d / %02d:%02d", minutes, seconds, totalMinutes, totalSeconds));
            progressBar.setProgress(currentTime * 100 / totalTime);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tv_time);
        progressBar = findViewById(R.id.progress_bar);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        player = new MusicPlayer(this, R.raw.song1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        if(proximitySensor == null) {
            Toast.makeText(this, "Proximity sensor not available.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                if (isPlaying) {
                    player.pause();
                    handler.removeCallbacks(updateTimeRunnable);
                    stopMusicService();
                } else {
                    player.play();
                    handler.post(updateTimeRunnable);
                    startMusicService();
                }
                isPlaying = !isPlaying;
            }
        }
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    private void stopMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        player.pause();
        isPlaying = false;
        handler.removeCallbacks(updateTimeRunnable);
        stopMusicService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
