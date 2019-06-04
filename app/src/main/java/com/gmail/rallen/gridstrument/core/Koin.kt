package com.gmail.rallen.gridstrument.core

import com.gmail.rallen.gridstrument.GridTouchListener
import com.gmail.rallen.gridstrument.event.Clamper
import com.gmail.rallen.gridstrument.finger.Finger
import com.gmail.rallen.gridstrument.finger.GLFinger
import com.gmail.rallen.gridstrument.finger.MidiFinger
import com.gmail.rallen.gridstrument.gl.GridGLSurfaceView
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.FingerRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo
import com.gmail.rallen.gridstrument.repo.MidiRepo
import org.koin.dsl.module

val appModule = module {
    single { BaseNotesRepo(get()) }
    single { GridConfigRepo() }
    single { MidiRepo(get()) }
    single { FingerRepo() }
    single { Clamper(get()) }

    factory { (pointerId: Int) -> Finger(pointerId, get(), get(), get()) }
    factory { MidiFinger(get()) }
    factory { GLFinger() }
    factory {
        GridGLSurfaceView(get(), get(), get(), get()).also {
            it.setOnTouchListener(GridTouchListener(get()))
        }
    }
}