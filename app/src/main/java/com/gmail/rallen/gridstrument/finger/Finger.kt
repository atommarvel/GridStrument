package com.gmail.rallen.gridstrument.finger

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.core.math.MathUtils
import com.gmail.rallen.gridstrument.event.Clamper
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo
import com.gmail.rallen.gridstrument.util.xyToNote
import org.koin.core.KoinComponent
import org.koin.core.inject

class Finger(private val pointerId: Int, private val gridConfigRepo: GridConfigRepo, private val baseNotesRepo: BaseNotesRepo, private val clamper: Clamper) :
    KoinComponent {

    // TODO: zone finger that can direct what channel/behavior the event should be?
    private val midiFinger: MidiFinger by inject()
    val glFinger: GLFinger by inject()

    var initialPosition = PointF()
    var currentPosition = PointF()
    var pressure = -1f

    val modX: Float get() = currentPosition.x - initialPosition.x
    val modY: Float get() = currentPosition.y - initialPosition.y

    fun update(view: View, ev: MotionEvent, index: Int) {
        currentPosition.set(
            ev.getX(index),
            view.height - ev.getY(index) // Y-invert for OpenGL
        )
        pressure = MathUtils.clamp(
            (ev.getPressure(index) - gridConfigRepo.minPressureDomain) / (gridConfigRepo.maxPressureDomain - gridConfigRepo.minPressureDomain),
            gridConfigRepo.minPressureRange, gridConfigRepo.maxPressureRange
        )
    }

    fun eventDown() {
        initialPosition.set(currentPosition)
        val clampPressure = clamper.clampPressure(pressure)
        // Midi
        val midiData = EventData(
            pressure = clampPressure,
            note = getNote(),
            bend = 0
        )
        midiFinger.eventDown(midiData)

        // GL
        val x = Math.floor((currentPosition.x / gridConfigRepo.cellWidth).toDouble()).toFloat() * gridConfigRepo.cellWidth + gridConfigRepo.cellWidth / 2
        val y = Math.floor((currentPosition.y / gridConfigRepo.cellHeight).toDouble()).toFloat() * gridConfigRepo.cellHeight + gridConfigRepo.cellHeight / 2
        val glData = EventData(
            x = x,
            y = y,
            pressure = clampPressure
        )
        glFinger.eventDown(glData)
    }

    fun eventUp() {
        val clampPressure = clamper.clampPressure(pressure)
        // Midi
        val midiData = EventData(
            pressure = clampPressure,
            note = getNote(),
            bend = 0
        )
        midiFinger.eventUp(midiData)

        // GL
        val x = -gridConfigRepo.cellWidth / 2
        val y = -gridConfigRepo.cellHeight / 2
        val glData = EventData(
            x = x,
            y = y
        )
        glFinger.eventUp(glData)
    }

    fun eventMove() {
        // Midi
        val midiData = EventData(
            modX = clamper.clampPitchBend(modX)
        )
        midiFinger.eventMove(midiData)

        // GL
        val clampPressure = clamper.clampPressure(pressure)
        val glData = EventData(
            pressure = clampPressure
        )
        glFinger.eventMove(glData)
    }

    private fun getNote(): Int = xyToNote(initialPosition.x, initialPosition.y, gridConfigRepo, baseNotesRepo)
}