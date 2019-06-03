package com.gmail.rallen.gridstrument.repo

import android.util.DisplayMetrics
import android.view.WindowManager

class GridConfigRepo {

    var xdpi: Float = 0f
    var ydpi: Float = 0f
    var cellWidth: Float = 0f
    var cellHeight: Float = 0f

    val fingerSizeInches: Float = 0.6f
    val minPressureDomain: Float = 0.10f // linear region to map from...
    val maxPressureDomain: Float = 0.45f
    val minPressureRange: Float = 1f / 127f // linear region to map to.
    val maxPressureRange: Float = 1f
    val modulationYControl: Int = 1 // 1=mod wheel, 2=breath control, etc
    val pitchBendRange: Int = 12

    fun onCreate(windowManager: WindowManager) {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        xdpi = dm.xdpi
        ydpi = dm.ydpi
        cellWidth = xdpi * fingerSizeInches
        cellHeight = ydpi * fingerSizeInches
    }
}