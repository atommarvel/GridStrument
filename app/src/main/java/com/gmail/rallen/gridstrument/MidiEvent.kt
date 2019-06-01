package com.gmail.rallen.gridstrument

enum class NoteTrigger(val hex: Int) {
    ON(0x90),
    OFF(0x80)
}

interface MidiEvent {
    val byteArray: ByteArray
    val byteCount: Int
}

data class MidiNoteEvent @JvmOverloads constructor(val pitch: Int, val trigger: NoteTrigger, private val _channel: Int = 1, val velocity: Int = 64) :
    MidiEvent {

    val channel = _channel - 1

    override val byteArray: ByteArray
        get() = byteArrayOf(
            (trigger.hex + channel).toByte(), // note on/off + channel to target
            pitch.toByte(),
            velocity.toByte()
        )
    override val byteCount: Int = bytes

    companion object {
        private const val bytes = 3
    }
}

fun getAllNotesOffCC(channel: Int = 1): MidiCCEvent = MidiCCEvent(123, 0, channel)

/**
 * CC is short for Control Change
 */
data class MidiCCEvent @JvmOverloads constructor(val ctrlNumber: Int, val ctrlValue: Int, private val _channel: Int = 1) : MidiEvent {

    val channel = _channel - 1

    override val byteArray: ByteArray
        get() = byteArrayOf(
            (ccByte + channel).toByte(),
            ctrlNumber.toByte(),
            ctrlValue.toByte()
        )

    override val byteCount: Int = bytes

    companion object {
        // left half of the status byte indicating that this is a Control Change message
        private const val ccByte = 0xB0
        private const val bytes = 3
    }
}