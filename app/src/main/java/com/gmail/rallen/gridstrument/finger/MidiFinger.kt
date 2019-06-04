package com.gmail.rallen.gridstrument.finger

import android.util.Log
import com.gmail.rallen.gridstrument.event.MidiNoteEvent
import com.gmail.rallen.gridstrument.event.MidiPitchBendEvent
import com.gmail.rallen.gridstrument.event.NoteTrigger
import com.gmail.rallen.gridstrument.repo.MidiRepo
import org.koin.core.KoinComponent

class MidiFinger(private val midiRepo: MidiRepo) : KoinComponent, FingerBehavior {

    private var currentChannel = -1
    private var currentNote = -1
    private var isFingerInUse = false

    override fun close() {
        if (isFingerInUse) {
            midiRepo.send(MidiNoteEvent(currentNote, NoteTrigger.OFF, currentChannel, 0))
            midiRepo.send(MidiPitchBendEvent(0, currentChannel))
            //        TODO:
//        oscCtrl.sendModulationY(channel, 0f)
            //        TODO:
//        oscCtrl.sendPressure(channel, 0f)
        }
    }

    override fun eventDown(eventData: EventData) {
        validateEventDown()
        isFingerInUse = true
        currentNote = eventData.note
        currentChannel = eventData.channel

        midiRepo.send(MidiNoteEvent(currentNote, NoteTrigger.ON, eventData.channel, eventData.pressure))
        midiRepo.send(MidiPitchBendEvent(eventData.bend, eventData.channel))
        //        TODO:
//        oscCtrl.sendModulationY(channel, 0f)
        //        TODO:
//        oscCtrl.sendPressure(channel, 0f)
    }

    private fun validateEventDown() {
        if (isFingerInUse) {
            Log.e("eventDown", "Note=$currentNote on Channel=$currentChannel was never given a note off. Turning off.")
            close()
        }
    }

    override fun eventUp(eventData: EventData) {
        validateEventUp(eventData)
        isFingerInUse = false
        midiRepo.send(MidiNoteEvent(eventData.note, NoteTrigger.OFF, eventData.channel))
    }

    private fun validateEventUp(eventData: EventData) {
        if (!isFingerInUse) {
            Log.e("eventUp", "Finger is not set to in use, yet there is an event up.")
        }
        if (eventData.channel != currentChannel || eventData.note != currentNote) {
            Log.e(
                "eventUp",
                "Turning off a note=${eventData.note} on channel=${eventData.channel} that is " +
                    "different from the current note=$currentNote  on current channel=$currentChannel"
            )
        }
    }

    override fun eventMove(eventData: EventData) {
        validateEventMove(eventData)
        midiRepo.send(MidiPitchBendEvent(eventData.modX, eventData.channel))
        //        TODO
//        oscCtrl.sendPressure(channel, pressure)
        //        TODO
//        oscCtrl.sendModulationY(channel, modulationY)
    }

    private fun validateEventMove(eventData: EventData) {
        if (!isFingerInUse && (eventData.modX + eventData.modY != 0)) {
            Log.e("eventMove", "Finger is not set to in use, yet there is an event move.")
        }
        if (eventData.channel != currentChannel) {
            Log.e("eventMove", "Sending modX/Y changes on channel=${eventData.channel} that is different from the current channel=$currentChannel")
        }
    }
}