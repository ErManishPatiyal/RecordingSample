package com.human_factors.notetaker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.app.NotificationCompat


class RecordService : Service() {
    private lateinit var mediaRecorder: MediaRecorder
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "record"
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startRecording(intent)
        return START_STICKY // This makes the service restart if it's killed by the system
    }

    override fun onDestroy() {
        // Stop and release resources when the service is stopped
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        // Create a notification channel (required for Android Oreo and higher)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Audio Recording Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording audio")
            .setContentText("Audio recording is in progress")
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .build()
    }

    private fun startRecording(intent: Intent) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(intent.getStringExtra("audio"))
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            release()
        }
    }
}
