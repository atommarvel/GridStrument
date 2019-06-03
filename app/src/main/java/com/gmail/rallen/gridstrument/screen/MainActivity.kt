package com.gmail.rallen.gridstrument.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.gmail.rallen.gridstrument.R
import com.gmail.rallen.gridstrument.extension.tryLog
import com.gmail.rallen.gridstrument.gl.GridGLSurfaceView
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo
import com.gmail.rallen.gridstrument.repo.MidiRepo
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), TuningDialogFragment.OnTuningDialogDoneListener {

    private val midiRepo: MidiRepo by inject()
    private val baseNotesRepo: BaseNotesRepo by inject()
    private val gridConfigRepo: GridConfigRepo by inject()
    private val glView: GridGLSurfaceView by inject()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        midiRepo.onCreate()
        gridConfigRepo.onCreate(windowManager)
        setContentView(glView)
    }

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
            val dialog = TuningDialogFragment.newInstance()
            dialog.show(supportFragmentManager, "TuningDialogFragment")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onTuningDialogDone(values: List<Int>?) = tryLog {
        checkNotNull(values)
        baseNotesRepo.updateBaseNotes(values)
        glView.onBaseNotesUpdated()
    }

    companion object {
        private const val PREF_REQ_CODE = 99
    }
}
