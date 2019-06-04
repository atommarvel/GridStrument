package com.gmail.rallen.gridstrument.finger

interface FingerBehavior {
    fun eventDown(eventData: EventData)
    fun eventUp(eventData: EventData)
    fun eventMove(eventData: EventData)
    fun close()
}