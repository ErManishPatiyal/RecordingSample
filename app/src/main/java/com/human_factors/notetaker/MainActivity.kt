package com.human_factors.notetaker

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var recordButton: Button? = null
    private var notesList: ListView? = null
    private var noteAdapter: NoteAdapter? = null
    private val notes = mutableListOf<Note>()
    private var isRecording = false
    private var currentAudioPath: String = ""
    private var titleInput: EditText? = null
    private var contentInput: EditText? = null
    private var addButton: Button? = null
    private var buttonsLayout: LinearLayout? = null
    private lateinit var saveTextNoteButton: Button
    private lateinit var stopRecordingButton: Button
    private lateinit var serviceIntent: Intent
    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        addButton = findViewById(R.id.addButton)
        buttonsLayout = findViewById(R.id.buttonsLayout)
        recordButton = findViewById(R.id.recordButton)
        notesList = findViewById(R.id.notesList)
        saveTextNoteButton = findViewById(R.id.saveTextNoteButton)
        stopRecordingButton = findViewById(R.id.stopRecordingButton)

        noteAdapter = NoteAdapter(this, notes)
        notesList?.adapter = noteAdapter

        notes.addAll(NoteStorageUtil.loadNotesFromFile(this))
        noteAdapter?.notifyDataSetChanged()

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        addButton?.setOnClickListener {
            val title = titleInput?.text.toString()
            if (title.isNotEmpty()) {
                titleInput?.isEnabled = false
                addButton?.visibility = View.GONE
                buttonsLayout?.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Please enter a title for the note.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val textNoteButton: Button = findViewById(R.id.textNoteButton)
        textNoteButton.setOnClickListener {
            buttonsLayout?.visibility = View.GONE
            contentInput?.visibility = View.VISIBLE
            saveTextNoteButton.visibility = View.VISIBLE
        }

        recordButton?.setOnClickListener {
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
                buttonsLayout?.visibility = View.GONE
                startRecording()
            }
        }

        saveTextNoteButton.setOnClickListener {
            val textContent = contentInput?.text.toString()
            if (textContent.isNotEmpty()) {
                saveNote(titleInput?.text.toString(), textContent, null)
                contentInput?.visibility = View.GONE
                saveTextNoteButton.visibility = View.GONE
                titleInput?.text?.clear()
                contentInput?.text?.clear()
                titleInput?.isEnabled = true
                buttonsLayout?.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Please enter some text for the note.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        stopRecordingButton.setOnClickListener {
            stopRecording()
            saveNote(titleInput?.text.toString(), null, currentAudioPath)
            stopRecordingButton.visibility = View.GONE
            titleInput?.text?.clear()
            titleInput?.isEnabled = true
            buttonsLayout?.visibility = View.VISIBLE
        }
    }

    private fun saveNote(title: String, text: String?, audioPath: String?) {
        if (title.isNotEmpty()) {
            val note = Note(title, text ?: "", audioPath ?: "")
            notes.add(note)
            noteAdapter?.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Please enter a title for the note.", Toast.LENGTH_SHORT).show()
        }
        NoteStorageUtil.saveNotesToFile(this, notes)
    }

    private fun startRecording() {
        isRecording = true
        currentAudioPath =
            "${externalCacheDir?.absolutePath}/audio_note_${System.currentTimeMillis()}.3gp"
        val isMyServiceRunning = isServiceRunning(RecordService::class.java)
        if (!isMyServiceRunning) {
            serviceIntent = Intent(this, RecordService::class.java)
            serviceIntent.putExtra("audio", currentAudioPath)
            startService(serviceIntent)
        }
        stopRecordingButton.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        if (isRecording) {
            val isMyServiceRunning = isServiceRunning( RecordService::class.java)
            if (isMyServiceRunning) {
                stopService(serviceIntent)
            }
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
    }


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)

        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                return true // The service is running
            }
        }

        return false // The service is not running
    }
}
