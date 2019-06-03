package com.gmail.rallen.gridstrument

import androidx.core.math.MathUtils

// TODO: revisit calculations
class Clamper(private val gridConfigRepo: GridConfigRepository) {

    fun clampPressure(pressure: Float) =
        MathUtils.clamp(Math.floor((pressure * 127 + 0.5f).toDouble()).toFloat(), 0f, 127f).toInt()

    /**
     * @param deltaX number of horizontal pixels the finger has slid from its origin point of inital contact
     */
    fun clampPitchBend(deltaX: Float) =
        MathUtils.clamp(0x2000 + 0x2000 * (deltaX / (gridConfigRepo.pitchBendRange * gridConfigRepo.cellWidth)),0f, 1f * 0x3fff ).toInt()

    /**
     * @param deltaY number of vertical pixels the finger has slid from its origin point of inital contact
     */
    fun clampModY(deltaY: Float) =
        MathUtils.clamp(127f * (Math.abs(deltaY) / gridConfigRepo.cellHeight),0f, 127f).toInt()

    fun clampVelocity(velocity: Float) =
        MathUtils.clamp(Math.floor((velocity * 127 + 0.5f).toDouble()).toFloat(), 0f, 127f).toInt()
}