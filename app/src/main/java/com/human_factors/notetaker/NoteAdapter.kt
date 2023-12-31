package com.human_factors.notetaker

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class NoteAdapter(private val context: Context, private val notes: MutableList<Note>) : ArrayAdapter<Note>(context, 0, notes) {
    private var mediaPlayer: MediaPlayer? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val note = getItem(position)!!
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.note_item, parent, false)
        val titleTextView: TextView = listItemView.findViewById(R.id.noteTitle)
        val contentTextView: TextView = listItemView.findViewById(R.id.noteContent)
        val actionButton: Button = listItemView.findViewById(R.id.actionNoteButton)
        val deleteButton: Button = listItemView.findViewById(R.id.deleteNoteButton)

        titleTextView.text = note.title
        contentTextView.text = note.text
        contentTextView.visibility = View.GONE

        actionButton.text = if (note.audioPath.isNullOrEmpty()) "Show" else "Play"
        actionButton.setOnClickListener {
            if (note.audioPath.isNullOrEmpty()) {
                val isContentVisible = contentTextView.visibility == View.VISIBLE
                contentTextView.visibility = if (isContentVisible) View.GONE else View.VISIBLE
                actionButton.text = if (isContentVisible) "Show" else "Hide"
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(note.audioPath!!)
                        prepare()
                        start()
                        actionButton.text = "Stop"
                    }
                    mediaPlayer?.setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                        actionButton.text = "Play"
                    }
                } else {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    actionButton.text = "Play"
                }
            }
        }

        deleteButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            note.audioPath?.let { audioPath -> NoteStorageUtil.deleteAudioFile(audioPath) }

            notes.removeAt(position)
            notifyDataSetChanged()
            NoteStorageUtil.saveNotesToFile(context, notes)
        }

        return listItemView
    }
}
