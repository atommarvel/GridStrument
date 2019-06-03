package com.gmail.rallen.gridstrument.extension

import android.util.Log

inline fun Any.tryLog(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        Log.e(this::class.java.simpleName, e.message ?: "Exception message missing")
    }
}