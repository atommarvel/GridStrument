package com.gmail.rallen.gridstrument.extensions.midi

import android.media.midi.MidiDeviceInfo

val MidiDeviceInfo.name: String?
    get() = properties.getString(MidiDeviceInfo.PROPERTY_NAME)