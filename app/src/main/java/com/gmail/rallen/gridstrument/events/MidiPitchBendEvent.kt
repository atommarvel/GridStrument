package com.gmail.rallen.gridstrument.events

import com.gmail.rallen.gridstrument.core.AVAIL_VALS

/**
 *  The MIDI protocol specifies that a pitch bend value of 8192 (MSB of 64 and LSB of 0) means no bend.
 *  @param bendValue is converted into two bytes
 */
data class MidiPitchBendEvent(val bendValue: Int, val channel: Int = 0) : MidiEvent {

    override val byteArray: ByteArray = byteArrayOf(
        (PITCH_BEND_STATUS + channel).toByte(),
        (bendValue % AVAIL_VALS).toByte(), // Least Significant Byte
        (bendValue / AVAIL_VALS).toByte() // Most Significant Byte
    )

    companion object {
        private const val PITCH_BEND_STATUS = 0xE0
    }
}