package com.gmail.rallen.gridstrument.repo

import com.gmail.rallen.gridstrument.GridFinger
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf

class FingerRepo: KoinComponent {
    val fingers = (0..16).map { get<GridFinger> { parametersOf(it) } }
}