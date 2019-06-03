package com.gmail.rallen.gridstrument.event

/**
 * Prevents sending the same CC event twice in a row
 * TODO: refactor to be backed by a map that supports any status byte
 * TODO: use in a larger transform class
 */
class MidiEventDeDuper {
    private var lastPressure = -1
    private var lastModulationX = -1
    private var lastModulationY = -1

    fun isPressureNotDupe(pressure: Int): Boolean =
        (pressure != lastPressure).also {
            lastPressure = pressure
        }

    fun isModXNotDupe(modX: Int): Boolean =
        (modX != lastModulationX).also {
            lastModulationX = modX
        }

    fun isModYNotDupe(modY: Int): Boolean =
        (modY != lastModulationY).also {
            lastModulationY = modY
        }
}