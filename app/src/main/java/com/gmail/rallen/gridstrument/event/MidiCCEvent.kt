package com.gmail.rallen.gridstrument.event

fun getAllNotesOffCCEvent(channel: Int = 0): MidiCCEvent =
    MidiCCEvent(123, 0, channel)

/**
 * CC is short for Control Change
 */
data class MidiCCEvent @JvmOverloads constructor(val ctrlNumber: Int, val ctrlValue: Int, val channel: Int = 0) :
    MidiEvent {

    override val byteArray = byteArrayOf(
        (ccByte + channel).toByte(),
        ctrlNumber.toByte(),
        ctrlValue.toByte()
    )

    companion object {
        // Status bits indicating that this is a Control Change message.
        private const val ccByte = 0xB0
    }
}