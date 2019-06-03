package com.gmail.rallen.gridstrument.event

enum class NoteTrigger(val hex: Int) {
    ON(0x90),
    OFF(0x80)
}

data class MidiNoteEvent @JvmOverloads constructor(val pitch: Int, val trigger: NoteTrigger, val channel: Int = 0, val velocity: Int = 64) :
    MidiEvent {

    override val byteArray = byteArrayOf(
        (trigger.hex + channel).toByte(),
        pitch.toByte(),
        velocity.toByte()
    )
}