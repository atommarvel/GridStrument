package com.gmail.rallen.gridstrument.repo

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.gmail.rallen.gridstrument.event.MidiEvent
import com.gmail.rallen.gridstrument.event.getAllNotesOffCCEvent
import com.gmail.rallen.gridstrument.extension.log
import com.gmail.rallen.gridstrument.extension.midi.name
import com.gmail.rallen.gridstrument.extension.tryLog
import com.gmail.rallen.gridstrument.repo.MidiState.CONNECTED
import com.gmail.rallen.gridstrument.repo.MidiState.DISCONNECTED

enum class MidiState {
    CONNECTED, DISCONNECTED
}

class MidiRepo(private val context: Context) : MidiManager.OnDeviceOpenedListener {

    private lateinit var midiManager: MidiManager
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var midiState = DISCONNECTED
    private var device: MidiDevice? = null
    private var inputPort: MidiInputPort? = null

    /**
     * Can only be called in onCreate or later
     */
    fun onCreate() {
        midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
        midiManager.registerDeviceCallback(deviceCallback, handler)
        connectToFirstAvailableDevice()
    }

    fun turnOffAllNotes() {
        for (channel in 0..15) {
            send(getAllNotesOffCCEvent(channel))
        }
    }

    // TODO: create a transformation helper class that encompasses clamping & deduper
    fun send(midiEvent: MidiEvent) = tryLog {
        inputPort?.send(midiEvent.byteArray, 0, midiEvent.byteArray.size)
    }

    private fun connectToFirstAvailableDevice() = tryLog {
        // Grab the first device that has an input port.
        val deviceInfo = midiManager.devices.firstOrNull { deviceInfo ->
            deviceInfo.inputPortCount > 0
        }
        if (deviceInfo != null) {
            midiManager.openDevice(deviceInfo, this, handler)
        } else {
            log("no devices available to connect to")
        }
    }

    private val deviceCallback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(deviceInfo: MidiDeviceInfo?) {
            if (midiState == DISCONNECTED) {
                connectToFirstAvailableDevice()
            }
        }

        override fun onDeviceRemoved(deviceInfo: MidiDeviceInfo?) = tryLog {
            checkNotNull(deviceInfo)
            if (this@MidiRepo.device?.info == deviceInfo) {
                midiState = DISCONNECTED
                clearDevice()
                log("disconnected from device ${deviceInfo.name}")
                Toast.makeText(context, "Device disconnected: ${deviceInfo.name}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
        }
    }

    override fun onDeviceOpened(device: MidiDevice?) = tryLog {
        checkNotNull(device)
        midiState = CONNECTED
        this.device = device
        log("connected to device ${device.info.name}")
        Toast.makeText(context, "Device connected: ${device.info.name}", Toast.LENGTH_SHORT).show()
        inputPort = device.openInputPort(0)
    }

    private fun clearDevice() {
        device = null
        inputPort = null
    }
}

