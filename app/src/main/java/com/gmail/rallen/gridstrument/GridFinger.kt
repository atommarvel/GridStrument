package com.gmail.rallen.gridstrument

import android.graphics.PointF
import android.opengl.Matrix
import android.util.Log
import androidx.core.math.MathUtils
import com.gmail.rallen.gridstrument.event.Clamper
import com.gmail.rallen.gridstrument.event.MidiNoteEvent
import com.gmail.rallen.gridstrument.event.MidiPitchBendEvent
import com.gmail.rallen.gridstrument.event.NoteTrigger
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo
import com.gmail.rallen.gridstrument.repo.MidiRepo
import com.gmail.rallen.gridstrument.util.xyToNote

class GridFinger(val channel: Int, val midiRepo: MidiRepo, val gridConfigRepo: GridConfigRepo, val baseNotesRepo: BaseNotesRepo, val clamper: Clamper) {
    var touch = PointF()
    var current = PointF()
    var pressure = 0.0f
        // scale (minDomain,maxDomain) value to (minRange,maxRange)
        set(value) {
            field = MathUtils.clamp(
                (value - gridConfigRepo.minPressureDomain) / (gridConfigRepo.maxPressureDomain - gridConfigRepo.minPressureDomain),
                gridConfigRepo.minPressureRange, gridConfigRepo.maxPressureRange
            )
        }
    var lightMatrix = FloatArray(16)
    var lightRect = GridRects(0.5f, 0.5f, 0.5f, 1.0f)
    var active = false
    var note = 0
    val modulationX: Float
        get() = current.x - touch.x
    val modulationY: Float
        get() = current.y - touch.y

    fun eventDown() {
        touch.set(current)
        note = xyToNote(touch.x, touch.y, gridConfigRepo, baseNotesRepo)
        if (active) {
            Log.e("eventDown", String.format("MISSED NOTE OFF on Channel %d", channel))
        }
        active = true
        // light rect
        val x = Math.floor((touch.x / gridConfigRepo.cellWidth).toDouble()).toFloat() * gridConfigRepo.cellWidth + gridConfigRepo.cellWidth / 2
        val y = Math.floor((touch.y / gridConfigRepo.cellHeight).toDouble()).toFloat() * gridConfigRepo.cellHeight + gridConfigRepo.cellHeight / 2
        Matrix.setIdentityM(lightMatrix, 0)
        Matrix.translateM(lightMatrix, 0, x, y, 0.0f)
        lightRect.setModelMatrix(lightMatrix)
        lightRect.setColor(pressure, pressure, pressure, 1.0f)

//        TODO:
//        oscCtrl.sendPressure(channel, 0f)
        midiRepo.send(MidiPitchBendEvent(channel, 0))
//        TODO:
//        oscCtrl.sendModulationY(channel, 0f)
        midiRepo.send(MidiNoteEvent(note, NoteTrigger.ON, channel, clamper.clampPressure(pressure)))
    }

    fun eventUp() {
        if (!active) {
            Log.e("eventUp", String.format("MISSED NOTE ON on Channel %d", channel))
        }
        active = false
        // light rect offscreen
        Matrix.setIdentityM(lightMatrix, 0)
        Matrix.translateM(lightMatrix, 0, -gridConfigRepo.cellWidth / 2, -gridConfigRepo.cellHeight / 2, 0.0f)
        lightRect.setModelMatrix(lightMatrix)

        midiRepo.send(MidiNoteEvent(note, NoteTrigger.OFF, channel, 0))
    }

    fun eventMove() {
        if (!active) {
            if (modulationX + modulationY != 0f) {
                Log.e("eventMove", String.format("MISSING NOTE ON on Channel %d", channel))
            }
        }
        lightRect.setColor(pressure, pressure, pressure, 1.0f)

//        TODO
//        oscCtrl.sendPressure(channel, pressure)
        midiRepo.send(MidiPitchBendEvent(clamper.clampPitchBend(modulationX), channel))
//        TODO
//        oscCtrl.sendModulationY(channel, modulationY)
    }
}