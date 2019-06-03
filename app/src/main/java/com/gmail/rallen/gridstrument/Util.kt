package com.gmail.rallen.gridstrument

import androidx.core.math.MathUtils

fun xyToNote(x: Float, y: Float, gridConfigRepo: GridConfigRepository, baseNotesRepo: BaseNotesRepository): Int {
    val curColumn = Math.floor((x / gridConfigRepo.cellWidth).toDouble()).toInt()
    var curRow = Math.floor((y / gridConfigRepo.cellHeight).toDouble()).toInt()
    if (curRow >= baseNotesRepo.notes.size) {
        curRow = baseNotesRepo.notes.size    - 1
    }
    val baseNote = baseNotesRepo[curRow].toFloat()
    return MathUtils.clamp(baseNote + curColumn, 0f, 127f).toInt()
}