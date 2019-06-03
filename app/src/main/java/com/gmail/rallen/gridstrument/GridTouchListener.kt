package com.gmail.rallen.gridstrument

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View

class GridTouchListener(val fingers: List<GridFinger>) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, ev: MotionEvent): Boolean {
        val pointerCount = ev.pointerCount

        for (p in 0 until pointerCount) {
            val pointerId = ev.getPointerId(p)
            fingers[pointerId].current.set(ev.getX(p), view.height - ev.getY(p)) // Y-invert for OpenGL
            fingers[pointerId].pressure = (ev.getPressure(p))
        }

        val historySize = ev.historySize
        if (historySize > 0) {
            Log.e("samp", "UNEXPECTED HISTORY!")
        }
        TouchDebugger.log(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> fingers[ev.getPointerId(ev.actionIndex)].eventDown()
            MotionEvent.ACTION_POINTER_UP -> fingers[ev.getPointerId(ev.actionIndex)].eventUp()
            MotionEvent.ACTION_DOWN -> {
                fingers[ev.getPointerId(0)].eventDown()
                view.requestUnbufferedDispatch(ev) // move events will not be buffered (Android L & later)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> fingers[ev.getPointerId(0)].eventUp()
            MotionEvent.ACTION_MOVE -> for (p in 0 until pointerCount) {
                fingers[ev.getPointerId(p)].eventMove()
            }
            MotionEvent.ACTION_OUTSIDE -> Log.e("onTouch", "Outside? what to do?")
        }
        return true
    }
}