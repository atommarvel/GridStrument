package com.gmail.rallen.gridstrument.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.gmail.rallen.gridstrument.repo.BaseNotesRepo
import com.gmail.rallen.gridstrument.repo.FingerRepo
import com.gmail.rallen.gridstrument.repo.GridConfigRepo
import com.gmail.rallen.gridstrument.util.xyToNote

/**
 * TODO: send pitchBend Range message to the server so they can understand this automatically
 */
class GridGLSurfaceView @JvmOverloads constructor(
    context: Context,
    private val fingerRepo: FingerRepo,
    private val baseNotesRepo: BaseNotesRepo,
    private val gridConfigRepo: GridConfigRepo
) : GLSurfaceView(context) {

    // rendering vars...
    private val renderer: GridGLRenderer = GridGLRenderer()
    private var gridLines: GridLines? = null
    private var noteRects: ArrayList<GridRects>? = null
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0

    private val mNoteColors = arrayOf(// modulo 12 color keys
        floatArrayOf(0.0f, 0.9f, 0.0f, 1.0f), // C
        floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), // C#
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f), // D
        floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), // D#
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f), // E
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f), // F
        floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), // F#
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f), // G
        floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), // G#
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f), // A
        floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), // A#
        floatArrayOf(0.0f, 0.9f, 0.9f, 1.0f)  // B
    )

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    fun onBaseNotesUpdated() {
        val numHorizLines = Math.ceil((displayWidth / gridConfigRepo.cellWidth).toDouble()).toInt() + 1
        val numVertLines = Math.ceil((displayHeight / gridConfigRepo.cellHeight).toDouble()).toInt() + 1
        var k = 0
        for (i in 0..numHorizLines) {
            var j = 0
            while (j <= numVertLines) {
                val note = xyToNote(
                    i * gridConfigRepo.cellWidth + 1,
                    j * gridConfigRepo.cellHeight + 1,
                    gridConfigRepo,
                    baseNotesRepo
                )
                val curColor = mNoteColors[note % 12]
                noteRects!![k].setColor(curColor[0], curColor[1], curColor[2], curColor[3])
                j++
                k++
            }
        }
    }

    private fun resetRenderer() {
        renderer.clearItems()
        for (i in 0..15) {
            renderer.addItem(fingerRepo.fingers[i].glFinger.lightRect)
        }
        for (g in noteRects!!) {
            renderer.addItem(g)
        }
        renderer.addItem(gridLines)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        displayWidth = right - left
        displayHeight = bottom - top
        val numHorizLines = Math.ceil((displayWidth / gridConfigRepo.cellWidth).toDouble()).toInt() + 1
        val numVertLines = Math.ceil((displayHeight / gridConfigRepo.cellHeight).toDouble()).toInt() + 1
        val numVertCells = numVertLines - 1
        Log.d("onLayout", "h=$numHorizLines v=$numVertLines")
        gridLines = GridLines(0.9f, 0.9f, 0.9f, 1.0f)
        noteRects = ArrayList()
        if (baseNotesRepo.notes.size != numVertCells) {
            val resizedBaseNotes = baseNotesRepo.notes.toMutableList()
            val oldSize = resizedBaseNotes.size
            if (oldSize < numVertCells) {
                for (i in oldSize until numVertCells) {
                    resizedBaseNotes.add(baseNotesRepo.notes[oldSize - 1] + 5) // have to pick something
                }
            } else {
                for (i in oldSize - 1 downTo numVertCells) {
                    resizedBaseNotes.removeAt(i)
                }
            }
            baseNotesRepo.updateBaseNotes(resizedBaseNotes)
        }
        //gridLines.reset();
        for (i in 0..numHorizLines) {
            gridLines!!.add(i.toFloat() * gridConfigRepo.cellWidth, 0.0f, 0.0f, i.toFloat() * gridConfigRepo.cellWidth, numHorizLines.toFloat() * gridConfigRepo.cellHeight, 0.0f)
            gridLines!!.add(0.0f, i.toFloat() * gridConfigRepo.cellHeight, 0.0f, numHorizLines.toFloat() * gridConfigRepo.cellWidth, i.toFloat() * gridConfigRepo.cellHeight, 0.0f)
            for (j in 0..numVertLines) {
                val note = xyToNote(
                    i * gridConfigRepo.cellWidth + 1,
                    j * gridConfigRepo.cellHeight + 1,
                    gridConfigRepo,
                    baseNotesRepo
                )
                val curColor = mNoteColors[note % 12]
                val curRect = GridRects(curColor[0], curColor[1], curColor[2], curColor[3])
                curRect.add(-gridConfigRepo.cellWidth / 4f, gridConfigRepo.cellHeight / 6f, 0f, gridConfigRepo.cellWidth / 6f, -gridConfigRepo.cellHeight / 4f, 0f)
                val curMatrix = FloatArray(16)
                Matrix.setIdentityM(curMatrix, 0)
                Matrix.translateM(curMatrix, 0, i * gridConfigRepo.cellWidth + gridConfigRepo.cellWidth / 2, j * gridConfigRepo.cellHeight + gridConfigRepo.cellHeight / 2, 0.0f)
                curRect.setModelMatrix(curMatrix)
                noteRects!!.add(curRect)
            }
        }

        // TODO: finger.onLayout?
        for (i in 0..15) {
            fingerRepo.fingers[i].glFinger.apply {
                lightRect.reset()
                lightRect.add(-gridConfigRepo.cellWidth / 2, gridConfigRepo.cellHeight / 2, 0.0f, gridConfigRepo.cellWidth / 2, -gridConfigRepo.cellHeight / 2, 0.0f)
                Matrix.setIdentityM(lightMatrix, 0)
                Matrix.translateM(lightMatrix, 0, -gridConfigRepo.cellWidth / 2, -gridConfigRepo.cellHeight / 2, 0.0f) // offscreen
                lightRect.setModelMatrix(lightMatrix)
            }
        }

        resetRenderer()
    }
}
