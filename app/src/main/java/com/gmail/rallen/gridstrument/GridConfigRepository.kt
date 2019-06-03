package com.gmail.rallen.gridstrument

class GridConfigRepository {

    var xdpi: Float = 200f
    var ydpi: Float = 200f
    val fingerSizeInches: Float = 0.6f
    val cellWidth: Float = xdpi * fingerSizeInches
    val cellHeight: Float = ydpi * fingerSizeInches
    val minPressureDomain: Float = 0.10f // linear region to map from...
    val maxPressureDomain: Float = 0.45f
    val minPressureRange: Float = 1f / 127f // linear region to map to.
    val maxPressureRange: Float = 1f
    val modulationYControl: Int = 1 // 1=mod wheel, 2=breath control, etc
    val pitchBendRange: Int = 12

}