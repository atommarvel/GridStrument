package com.gmail.rallen.gridstrument

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.gmail.rallen.gridstrument.repo.FingerRepo
import com.gmail.rallen.gridstrument.util.TouchDebugger

class GridTouchListener(private val fingerRepo: FingerRepo) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, ev: MotionEvent): Boolean {
        val pointerCount = ev.pointerCount
        for (pointerIndex in 0 until pointerCount) {
            val pointerId = ev.getPointerId(pointerIndex)
            fingerRepo.fingers[pointerId].update(view, ev, pointerIndex)
        }

        val historySize = ev.historySize
        if (historySize > 0) {
            Log.e("samp", "UNEXPECTED HISTORY!")
        }
        TouchDebugger.log(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> fingerRepo.fingers[ev.getPointerId(ev.actionIndex)].eventDown()
            MotionEvent.ACTION_POINTER_UP -> fingerRepo.fingers[ev.getPointerId(ev.actionIndex)].eventUp()
            MotionEvent.ACTION_DOWN -> {
                fingerRepo.fingers[ev.getPointerId(0)].eventDown()
                view.requestUnbufferedDispatch(ev) // move events will not be buffered (Android L & later)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> fingerRepo.fingers[ev.getPointerId(0)].eventUp()
            MotionEvent.ACTION_MOVE -> for (p in 0 until pointerCount) {
                fingerRepo.fingers[ev.getPointerId(p)].eventMove()
            }
            MotionEvent.ACTION_OUTSIDE -> Log.e("onTouch", "Outside? what to do?")
        }
        return true
    }
}