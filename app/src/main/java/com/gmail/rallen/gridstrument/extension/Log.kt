package com.gmail.rallen.gridstrument.extension

import android.util.Log

fun Any.log(msg: String) {
    Log.d(this::class.java.simpleName, msg)
}