package com.gmail.rallen.gridstrument

enum class NoteTrigger(val hex: Int) {
    ON(0x90),
    OFF(0x80)
}

interface MidiEvent {
    /**
     First byte is the status byte plus the channel to target.
     The following bytes after are data bytes representing values related to the set status byte.
    */
    val byteArray: ByteArray
}

// Available values for a midi data byte: 0 - 127
const val AVAIL_VALS = 128

/**
 *  The MIDI protocol specifies that a pitch bend value of 8192 (MSB of 64 and LSB of 0) means no bend.
 *  @param bendValue is converted into two bytes
 */
data class MidiPitchBendEvent(val bendValue: Int, val channel: Int = 0 ) : MidiEvent {

    override val byteArray: ByteArray = byteArrayOf(
        (PITCH_BEND_STATUS + channel).toByte(),
        (bendValue % AVAIL_VALS).toByte(), // Least Significant Byte
        (bendValue / AVAIL_VALS).toByte() // Most Significant Byte
    )

    companion object {
        private const val PITCH_BEND_STATUS = 0xE0
    }
}

data class MidiNoteEvent @JvmOverloads constructor(val pitch: Int, val trigger: NoteTrigger, val channel: Int = 0, val velocity: Int = 64) :
    MidiEvent {

    override val byteArray = byteArrayOf(
            (trigger.hex + channel).toByte(),
            pitch.toByte(),
            velocity.toByte()
        )
}

fun getAllNotesOffCCEvent(channel: Int = 0): MidiCCEvent = MidiCCEvent(123, 0, channel)

/**
 * CC is short for Control Change
 */
data class MidiCCEvent @JvmOverloads constructor(val ctrlNumber: Int, val ctrlValue: Int, val channel: Int = 0) : MidiEvent {

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