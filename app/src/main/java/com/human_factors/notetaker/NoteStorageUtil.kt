package com.human_factors.notetaker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object NoteStorageUtil {
    private const val NOTES_FILE = "notes.json"

    fun saveNotesToFile(context: Context, notes: List<Note>) {
        val gson = Gson()
        val jsonNotes = gson.toJson(notes)
        context.openFileOutput(NOTES_FILE, Context.MODE_PRIVATE).use { output ->
            output.write(jsonNotes.toByteArray())
        }
    }

    fun loadNotesFromFile(context: Context): MutableList<Note> {
        val notesFile = File(context.filesDir, NOTES_FILE)
        if (!notesFile.exists()) {
            return mutableListOf()
        }

        return context.openFileInput(NOTES_FILE).use { input ->
            val size = input.available()
            val bytes = ByteArray(size)
            input.read(bytes)
            val json = String(bytes)

            val gson = Gson()
            val type = object : TypeToken<MutableList<Note>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun deleteAudioFile(audioPath: String) {
        val file = File(audioPath)
        if (file.exists()) {
            file.delete()
        }
    }
}