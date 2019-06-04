package com.gmail.rallen.gridstrument.finger

import android.opengl.Matrix
import com.gmail.rallen.gridstrument.gl.GridRects
import org.koin.core.KoinComponent

class GLFinger: KoinComponent, FingerBehavior {

    var lightMatrix = FloatArray(16)
    var lightRect = GridRects(0.5f, 0.5f, 0.5f, 1.0f)

    override fun eventDown(eventData: EventData) {
        Matrix.setIdentityM(lightMatrix, 0)
        Matrix.translateM(lightMatrix, 0, eventData.x, eventData.y, 0.0f)
        lightRect.setModelMatrix(lightMatrix)
        val floatPressure = eventData.pressure.toFloat()
        lightRect.setColor(floatPressure, floatPressure, floatPressure, 1.0f)
    }

    override fun eventUp(eventData: EventData) {
        // light rect offscreen
        Matrix.setIdentityM(lightMatrix, 0)
        Matrix.translateM(lightMatrix, 0, eventData.x, eventData.y, 0.0f)
        lightRect.setModelMatrix(lightMatrix)
    }

    override fun eventMove(eventData: EventData) {
        val floatPressure = eventData.pressure.toFloat()
        lightRect.setColor(floatPressure, floatPressure, floatPressure, 1.0f)
    }

    override fun close() {
        // Do Nothing.
    }
}