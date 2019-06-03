package com.gmail.rallen.gridstrument.repo

import android.content.Context
import android.preference.PreferenceManager
import com.gmail.rallen.gridstrument.R

class BaseNotesRepo(val context: Context) {

    private var _notes: List<Int>
    val notes: List<Int>
        get() = _notes

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultNum = Integer.parseInt(context.getString(R.string.default_num_base_notes))
        val numBaseNotes = prefs.getInt(KEY_NUM_BASE_NOTES, defaultNum)
        val updatedNotes = mutableListOf<Int>()
        for (i in 0 until numBaseNotes) {
            updatedNotes.add(prefs.getInt("base_note_$i", 48 + 5 * i))
        }
        _notes = updatedNotes
    }

    fun updateBaseNotes(notes: List<Int>) {
        _notes = notes
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().apply {
            notes.forEachIndexed { index, note ->
                putInt("base_note_$index", note)
            }
        }.apply()
    }

    companion object {
        private const val KEY_NUM_BASE_NOTES = "num_base_notes"
    }
}