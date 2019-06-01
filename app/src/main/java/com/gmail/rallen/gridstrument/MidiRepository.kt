package com.gmail.rallen.gridstrument

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.gmail.rallen.gridstrument.MidiState.CONNECTED
import com.gmail.rallen.gridstrument.MidiState.DISCONNECTED
import com.gmail.rallen.gridstrument.extensions.log
import com.gmail.rallen.gridstrument.extensions.midi.name
import com.gmail.rallen.gridstrument.extensions.tryLog

class MidiRepository(private val context: Context) : MidiManager.OnDeviceOpenedListener {

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var midiState = DISCONNECTED
    private var device: MidiDevice? = null
    private var inputPort: MidiInputPort? = null

    fun setup() {
        midiManager.registerDeviceCallback(deviceCallback, handler)
        connectToFirstAvailableDevice()
    }

    fun noteOn(channel: Int, note: Int, velocity: Int) {
        val buffer = ByteArray(32)
        var numBytes = 0
        buffer[numBytes++] = (0x90 + (0)).toByte() // note on
        buffer[numBytes++] = note.toByte()
        buffer[numBytes++] = 64.toByte() // max velocity
        val offset = 0
        // post is non-blocking
        inputPort?.send(buffer, offset, numBytes)
    }

    fun noteOff(channel: Int, note: Int, velocity: Int) {
        val buffer = ByteArray(32)
        var numBytes = 0
        buffer[numBytes++] = (0x80 + (0)).toByte() // note off
        buffer[numBytes++] = note.toByte()
        buffer[numBytes++] = 64.toByte() // max velocity?
        val offset = 0
        // post is non-blocking
        inputPort?.send(buffer, offset, numBytes)
    }

    private fun connectToFirstAvailableDevice() = tryLog {
        val deviceInfo = midiManager.devices.firstOrNull { deviceInfo ->
            deviceInfo.inputPortCount > 0
        }

        if (deviceInfo == null) {
            log("no devices available to connect to")
            return
        } else {
            midiManager.openDevice(deviceInfo, this, handler)
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
            if (this@MidiRepository.device?.info == deviceInfo) {
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

enum class MidiState {
    CONNECTED, DISCONNECTED
}
