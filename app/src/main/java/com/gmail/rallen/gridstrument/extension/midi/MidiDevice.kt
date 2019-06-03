package com.gmail.rallen.gridstrument.extension.midi

import android.media.midi.MidiDeviceInfo

val MidiDeviceInfo.name: String?
    get() = properties.getString(MidiDeviceInfo.PROPERTY_NAME)