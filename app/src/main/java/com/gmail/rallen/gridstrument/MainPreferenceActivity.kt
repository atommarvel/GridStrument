package com.gmail.rallen.gridstrument

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment

// TODO: Upgrade to AndroidX Preference Library
class MainPreferenceActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, MyPreferenceFragment()).commit()
    }

    class MyPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            // try to validate server ip and server port
            findPreference("pitch_bend_range").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val value = (newValue as? String)?.toIntOrNull() ?: -9999
                value in 1..24
            }
        }
    }
}