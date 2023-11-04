package com.human_factors.notetaker

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var stopTimer: Button
    private lateinit var countdownTimerText: TextView
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var currentAudioPath: String = ""
    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countdownTimerText = findViewById(R.id.countdown_timer_text)

        stopTimer = findViewById(R.id.stop_timer)

        val countdownTimer = object : CountDownTimer(120000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                countdownTimerText.text = getString(R.string.seconds_remaining, secondsRemaining.toString())
            }

            override fun onFinish() {
                stopRecording()
                stopTimer.text = getString(R.string.start)
            }
        }

        countdownTimer.start()
        startRecordingActual()
        stopTimer.setOnClickListener {
            if (isRecording) {
                countdownTimer.cancel()
                stopRecording()
                stopTimer.text = getString(R.string.start)
            } else {
                countdownTimer.start()
                startRecording()
                stopTimer.text = getString(R.string.stop)
            }
        }

    }

    private fun startRecordingActual() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        } else {
            startRecording()
        }
    }


    private fun startRecording() {
        isRecording = true
        currentAudioPath =
            "${externalCacheDir?.absolutePath}/audio_note_${System.currentTimeMillis()}.3gp"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(currentAudioPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (isRecording) {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }
}
