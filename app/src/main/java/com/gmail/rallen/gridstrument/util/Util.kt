package com.gmail.rallen.gridstrument.util

import androidx.core.math.MathUtils
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo

fun xyToNote(x: Float, y: Float, gridConfigRepo: GridConfigRepo, baseNotesRepo: BaseNotesRepo): Int {
    val curColumn = Math.floor((x / gridConfigRepo.cellWidth).toDouble()).toInt()
    var curRow = Math.floor((y / gridConfigRepo.cellHeight).toDouble()).toInt()
    if (curRow >= baseNotesRepo.notes.size) {
        curRow = baseNotesRepo.notes.size    - 1
    }
    val baseNote = baseNotesRepo.notes[curRow].toFloat()
    return MathUtils.clamp(baseNote + curColumn, 0f, 127f).toInt()
}