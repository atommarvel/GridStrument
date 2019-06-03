package com.gmail.rallen.gridstrument.util

import android.util.Log
import android.view.MotionEvent
import com.gmail.rallen.gridstrument.extension.tryLog

object TouchDebugger {

    private const val loggingEnabled = false // TODO: pref

    fun log(ev: MotionEvent) = tryLog {
        if (!loggingEnabled) return

        val pointerActionIndex = toPointerActionIndex(ev)
        val unhandledAction = isUnhandledAction(ev.actionMasked)
        val action = toAction(ev.actionMasked)

        if (unhandledAction) {
            Log.e("samp", "UNHANDLED $action")
        }
        val historySize = ev.historySize
        if (historySize > 0) {
            Log.e("samp", "UNEXPECTED HISTORY!")

        }
        val pointerCount = ev.pointerCount
        Log.d("samp", "act $action $pointerActionIndex $pointerCount $historySize")
        logHistoricalEvents(ev, historySize, pointerCount)

        val eventTime = ev.eventTime
        for (p in 0 until pointerCount) {
            val pointerId = ev.getPointerId(p)
            val pointerX = ev.getX(p)
            val pointerY = ev.getY(p)
            Log.d("samp", "$eventTime: ptr ($p) $pointerId ($pointerX,$pointerY)")
        }
    }

    private fun logHistoricalEvents(ev: MotionEvent, historySize: Int, pointerCount: Int) {
        for (h in 0 until historySize) {
            val historicalTime = ev.getHistoricalEventTime(h)
            for (p in 0 until pointerCount) {
                val pointerId = ev.getPointerId(p)
                val pointerX = ev.getHistoricalX(p, h)
                val pointerY = ev.getHistoricalY(p, h)
                Log.d("samp", "$historicalTime: hst ($h,$p) $pointerId ($pointerX,$pointerY)")
            }
        }
    }


    private fun isUnhandledAction(actionMasked: Int): Boolean = when (actionMasked) {
        MotionEvent.ACTION_CANCEL,
        MotionEvent.ACTION_DOWN,
        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_MOVE -> false
        else -> true
    }

    private fun toAction(actionMasked: Int): String = when (actionMasked) {
        MotionEvent.ACTION_CANCEL -> "CANCEL"
        MotionEvent.ACTION_DOWN -> "DOWN"
        MotionEvent.ACTION_HOVER_ENTER -> "HOVER_ENTER"
        MotionEvent.ACTION_HOVER_EXIT -> "HOVER_EXIT"
        MotionEvent.ACTION_HOVER_MOVE -> "HOVER_MOVE"
        MotionEvent.ACTION_MOVE -> "MOVE"
        MotionEvent.ACTION_OUTSIDE -> "OUTSIDE"
        MotionEvent.ACTION_POINTER_DOWN -> "POINTER_DOWN"
        MotionEvent.ACTION_POINTER_UP -> "POINTER_UP"
        MotionEvent.ACTION_SCROLL -> "SCROLL"
        MotionEvent.ACTION_UP -> "UP"
        else -> "?"
    }

    private fun toPointerActionIndex(ev: MotionEvent): Int = when(ev.actionMasked) {
        MotionEvent.ACTION_POINTER_DOWN,
        MotionEvent.ACTION_POINTER_UP -> ev.actionIndex
        else -> -1
    }
}