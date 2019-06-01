package com.gmail.rallen.gridstrument;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.illposed.osc.OSCPortOut;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity
        extends AppCompatActivity
        implements TuningDialogFragment.OnTuningDialogDoneListener
{
    private final static int PREF_REQ_CODE = 99;

    private GridGLSurfaceView mGLView;

    private OSCPortOut mOSCPortOut    = null;

    private ArrayList<Integer> mBaseNotes = new ArrayList<>();
    private MidiRepository midiRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SetupBaseNotes();

        mGLView = new GridGLSurfaceView(this, mBaseNotes);
        setContentView(mGLView);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mGLView.setDPI(dm.xdpi, dm.ydpi);

	    midiRepo = new MidiRepository(this);
        midiRepo.setup();
        mGLView.setMidiRepo( midiRepo );
    }

    private void SetupBaseNotes() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int default_num = Integer.parseInt(getString(R.string.default_num_base_notes));
        int numBaseNotes = SP.getInt("num_base_notes", default_num);
        for(int i = 0; i < numBaseNotes; i++) {
            mBaseNotes.add(SP.getInt("base_note_" + i, 48 + 5 * i));
        }
    }

    public void ResizeBaseNotes(ArrayList<Integer> baseNotes) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = SP.edit();
        editor.putInt("num_base_notes", baseNotes.size());
        mBaseNotes.clear();
        for(int i = 0; i < baseNotes.size(); i++) {
            mBaseNotes.add(i,baseNotes.get(i));
            editor.putInt("base_note_" + i,baseNotes.get(i));
        }
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_notes_off:
            Log.d("select", "NOTES OFF!");
            if (mOSCPortOut != null) {
                for (int c = 0; c < 16; c++) {
                    midiRepo.send( MidiEventKt.getAllNotesOffCC( c ) );
                }
            }
            return true;
        case R.id.action_settings:
            Log.d("select", "preferences...");
            Intent i = new Intent(this, MainPreferenceActivity.class);
            startActivityForResult(i, PREF_REQ_CODE);
            return true;
        case R.id.action_tuning:
            Log.d("select", "tuning...");
            ArrayList<Integer> v = mGLView.getBaseNotes();
            TuningDialogFragment dialog = TuningDialogFragment.newInstance(v);
            dialog.show(getSupportFragmentManager(), "TuningDialogFragment");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void onTuningDialogDone(ArrayList<Integer> values) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = SP.edit();
        for(int i = 0; i < values.size(); i++) {
            mBaseNotes.set(i,values.get(i));
            editor.putInt("base_note_" + i,values.get(i));
        }
        editor.commit();
        mGLView.setBaseNotes(mBaseNotes);
    }
}
