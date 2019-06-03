package com.gmail.rallen.gridstrument

class BaseNotesRepository {

    var notes = (0..6).map { 48 + 5 * it }

    operator fun get(index: Int): Int = notes[index]
}