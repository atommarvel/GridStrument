package com.gmail.rallen.gridstrument.finger

data class EventData(
    val channel: Int = 0,
    val pressure: Int = 0,
    val note: Int = 0,
    val bend: Int = 0,
    val modX: Int = 0,
    val modY: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f
)