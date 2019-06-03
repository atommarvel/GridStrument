package com.gmail.rallen.gridstrument.event

interface MidiEvent {
    /**
    First byte is the status byte plus the channel to target.
    The following bytes after are data bytes representing values related to the set status byte.
     */
    val byteArray: ByteArray
}