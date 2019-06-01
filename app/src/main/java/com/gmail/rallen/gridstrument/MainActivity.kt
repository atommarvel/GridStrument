package com.gmail.rallen.gridstrument

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

class MainActivity : AppCompatActivity(), TuningDialogFragment.OnTuningDialogDoneListener {

    private var baseNotes = emptyList<Int>()
    private val midiRepo = MidiRepository(this)

    private lateinit var gLView: GridGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBaseNotes()

        gLView = GridGLSurfaceView(this, ArrayList(baseNotes)) // TODO: switch glView to just use a list
        setContentView(gLView)

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        gLView.setDPI(dm.xdpi, dm.ydpi)

        midiRepo.setup()
        gLView.setMidiRepo(midiRepo)
    }

    private fun setupBaseNotes() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val defaultNum = Integer.parseInt(getString(R.string.default_num_base_notes))
        val numBaseNotes = prefs.getInt(KEY_NUM_BASE_NOTES, defaultNum)
        val updatedNotes = mutableListOf<Int>()
        for (i in 0 until numBaseNotes) {
            updatedNotes.add(prefs.getInt("base_note_$i", 48 + 5 * i))
        }
        baseNotes = updatedNotes
    }

    fun resizeBaseNotes(notes: List<Int>) = updateBaseNotes(notes)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_notes_off -> {
            midiRepo.turnOffAllNotes()
            true
        }
        R.id.action_settings -> {
            val i = Intent(this, MainPreferenceActivity::class.java)
            startActivityForResult(i, PREF_REQ_CODE)
            true
        }
        R.id.action_tuning -> {
            val dialog = TuningDialogFragment.newInstance(gLView.baseNotes)
            dialog.show(supportFragmentManager, "TuningDialogFragment")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onTuningDialogDone(values: ArrayList<Int>) {
        updateBaseNotes(values)
        gLView.baseNotes = ArrayList(baseNotes)
    }

    private fun updateBaseNotes(notes: List<Int>) {
        baseNotes = notes
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        prefs.edit().apply {
            notes.forEachIndexed { index, note ->
                putInt("base_note_$index", note)
            }
        }.apply()
    }

    companion object {
        private const val PREF_REQ_CODE = 99
        private const val KEY_NUM_BASE_NOTES = "num_base_notes"
    }
}
