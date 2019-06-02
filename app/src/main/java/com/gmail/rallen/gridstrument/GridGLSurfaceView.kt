package com.gmail.rallen.gridstrument

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent

/**
 * GridGLSurfaceView (really a ViewController)
 * TODO: Extract out logic to separate classes. this class should only own ui
 */
class GridGLSurfaceView @JvmOverloads constructor(context: Context, initialBaseNotes: List<Int> = emptyList()) : GLSurfaceView(context) {

    // configuration options
    private var pitchBendRange = 12    // how far to stretch? 1 grid unit?  12?

    var baseNotes: List<Int> = initialBaseNotes
        set(newValue) {
            assert(newValue.size == field.size)
            field = newValue
            val numHorizLines = Math.ceil((displayWidth / cellWidth).toDouble()).toInt() + 1
            val numVertLines = Math.ceil((displayHeight / cellHeight).toDouble()).toInt() + 1
            var k = 0
            for (i in 0..numHorizLines) {
                var j = 0
                while (j <= numVertLines) {
                    val note = xyToNote(i * cellWidth + 1, j * cellHeight + 1)
                    val curColor = mNoteColors[note % 12]
                    noteRects!![k].setColor(curColor[0], curColor[1], curColor[2], curColor[3])
                    j++
                    k++
                }
            }
        }

    private val modulationYControl = 1     // 1=mod wheel, 2=breath control, etc
    private val minPressureDomain = 0.10f // linear region to map from...
    private val maxPressureDomain = 0.45f
    private val minPressureRange = 1f / 127f  // linear region to map to.
    private val maxPressureRange = 1.0f
    private val fingerSizeInches = 0.6f

    // TODO: stop holding as activity
    // main app
    private var mainActivity: MainActivity? = null

    // rendering vars...
    private val renderer: GridGLRenderer
    private var gridLines: GridLines? = null
    private var noteRects: ArrayList<GridRects>? = null
    private var xdpi: Float = 0.toFloat()
    private var ydpi: Float = 0.toFloat()
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0
    private var cellWidth: Float = 0.toFloat()
    private var cellHeight: Float = 0.toFloat()

    private var midiRepo: MidiRepository? = null
    private val oscCtrl = GridOSCController()

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

    private val fingers = (0..16).map { GridFinger(it) }

    init {
        mainActivity = context as MainActivity
        setEGLContextClientVersion(2)
        renderer = GridGLRenderer()
        setRenderer(renderer)
        ydpi = 200f
        xdpi = ydpi
        cellHeight = 200f
        cellWidth = cellHeight
    }

    fun setDPI(xdpi: Float, ydpi: Float) {
        this.xdpi = xdpi
        this.ydpi = ydpi
    }

    fun setPitchBendRange(n: Int) {
        // TODO send OSC message to the server so they can understand this automatically
        pitchBendRange = n
    }

    fun setMidiRepo(repo: MidiRepository) {
        midiRepo = repo
    }

    fun xyToNote(x: Float, y: Float): Int {
        val curColumn = Math.floor((x / cellWidth).toDouble()).toInt()
        var curRow = Math.floor((y / cellHeight).toDouble()).toInt()
        if (curRow >= baseNotes.size) {
            curRow = baseNotes.size - 1
        }
        val baseNote = baseNotes[curRow].toFloat()
        return clamp(0f, 127f, baseNote + curColumn).toInt()
    }

    private fun resetRenderer() {
        renderer.clearItems()
        for (i in 0..15) {
            renderer.addItem(fingers[i].lightRect)
        }
        for (g in noteRects!!) {
            renderer.addItem(g)
        }
        renderer.addItem(gridLines)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        cellWidth = fingerSizeInches * xdpi
        cellHeight = fingerSizeInches * ydpi
        displayWidth = right - left
        displayHeight = bottom - top
        val numHorizLines = Math.ceil((displayWidth / cellWidth).toDouble()).toInt() + 1
        val numVertLines = Math.ceil((displayHeight / cellHeight).toDouble()).toInt() + 1
        val numVertCells = numVertLines - 1
        Log.d("onLayout", "h=$numHorizLines v=$numVertLines")
        gridLines = GridLines(0.9f, 0.9f, 0.9f, 1.0f)
        noteRects = ArrayList()
        if (baseNotes.size != numVertCells) {
            val resizedBaseNotes = baseNotes.toMutableList()
            val oldSize = resizedBaseNotes.size
            if (oldSize < numVertCells) {
                for (i in oldSize until numVertCells) {
                    resizedBaseNotes.add(baseNotes[oldSize - 1] + 5) // have to pick something
                }
            } else {
                for (i in oldSize - 1 downTo numVertCells) {
                    resizedBaseNotes.removeAt(i)
                }
            }
            mainActivity!!.resizeBaseNotes(baseNotes)
        }
        //gridLines.reset();
        for (i in 0..numHorizLines) {
            gridLines!!.add(i.toFloat() * cellWidth, 0.0f, 0.0f, i.toFloat() * cellWidth, numHorizLines.toFloat() * cellHeight, 0.0f)
            gridLines!!.add(0.0f, i.toFloat() * cellHeight, 0.0f, numHorizLines.toFloat() * cellWidth, i.toFloat() * cellHeight, 0.0f)
            for (j in 0..numVertLines) {
                val note = xyToNote(i * cellWidth + 1, j * cellHeight + 1)
                val curColor = mNoteColors[note % 12]
                val curRect = GridRects(curColor[0], curColor[1], curColor[2], curColor[3])
                curRect.add(-cellWidth / 4f, cellHeight / 6f, 0f, cellWidth / 6f, -cellHeight / 4f, 0f)
                val curMatrix = FloatArray(16)
                Matrix.setIdentityM(curMatrix, 0)
                Matrix.translateM(curMatrix, 0, i * cellWidth + cellWidth / 2, j * cellHeight + cellHeight / 2, 0.0f)
                curRect.setModelMatrix(curMatrix)
                noteRects!!.add(curRect)
            }
        }

        for (i in 0..15) {
            fingers[i].lightRect.reset()
            fingers[i].lightRect.add(-cellWidth / 2, cellHeight / 2, 0.0f, cellWidth / 2, -cellHeight / 2, 0.0f)
            Matrix.setIdentityM(fingers[i].lightMatrix, 0)
            Matrix.translateM(fingers[i].lightMatrix, 0, -cellWidth / 2, -cellHeight / 2, 0.0f) // offscreen
            fingers[i].lightRect.setModelMatrix(fingers[i].lightMatrix)
        }

        resetRenderer()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val pointerCount = ev.pointerCount
        //final long eventTime = ev.getEventTime();

        for (p in 0 until pointerCount) {
            val pointerId = ev.getPointerId(p)
            fingers[pointerId].current.set(ev.getX(p), this.height - ev.getY(p)) // Y-invert for OpenGL
            fingers[pointerId].pressure = (ev.getPressure(p))
        }

        val historySize = ev.historySize
        if (historySize > 0) {
            Log.e("samp", "UNEXPECTED HISTORY!")
        }
        TouchDebugger.log(ev)
        when (ev.actionMasked) {
            // ignoring ACTION_HOVER_*, ACTION_SCROLL
            MotionEvent.ACTION_POINTER_DOWN -> fingers[ev.getPointerId(ev.actionIndex)].eventDown()
            MotionEvent.ACTION_POINTER_UP -> fingers[ev.getPointerId(ev.actionIndex)].eventUp()
            MotionEvent.ACTION_DOWN -> {
                fingers[ev.getPointerId(0)].eventDown()
                this.requestUnbufferedDispatch(ev) // move events will not be buffered (Android L & later)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> fingers[ev.getPointerId(0)].eventUp()
            MotionEvent.ACTION_MOVE -> for (p in 0 until pointerCount) {
                fingers[ev.getPointerId(p)].eventMove()
            }
            MotionEvent.ACTION_OUTSIDE -> Log.e("onTouch", "Outside? what to do?")
        }
        return true
    }

    // ======================================================================
    private inner class GridOSCController {
        private var lastPressure = -1
        private var lastModulationX = -1
        private var lastModulationY = -1

        fun sendPressure(channel: Int, p: Float) {
            val pi = clamp(0f, 127f, Math.floor((p * 127 + 0.5f).toDouble()).toFloat()).toInt()
            if (pi != lastPressure) {
                Log.d("sendPressure", String.format("/vkb_midi/%d/channelpressure=%d", channel, pi))
                lastPressure = pi
            }
        }

        fun sendModulationX(channel: Int, mx: Float) {
            val mxi = clamp(0f, 1f * 0x3fff, 0x2000 + 0x2000 * (mx / (pitchBendRange * cellWidth))).toInt()
            if (mxi != lastModulationX) {
                Log.d("sendModulationX", String.format("/vkb_midi/%d/pitch=%d", channel, mxi))
                lastModulationX = mxi
                val midiEvent = MidiPitchBendEvent(mxi)
                midiRepo?.send(midiEvent)
            }
        }

        fun sendModulationY(channel: Int, my: Float) {
            val myi = clamp(0f, 127f, 127f * (Math.abs(my) / cellHeight)).toInt()
            if (myi != lastModulationY) {
                Log.d("sendModulationY", String.format("/vkb_midi/%d/cc/%d=%d", channel, modulationYControl, myi))
                lastModulationY = myi
            }
        }

        fun sendNoteOn(channel: Int, note: Int, velocity: Float) {
            val vi = clamp(0f, 127f, Math.floor((velocity * 127 + 0.5f).toDouble()).toFloat()).toInt()
            Log.d("sendNoteOn", String.format("/vkb_midi/%d/note/%d=%d", channel, note, vi))
            val midiEvent = MidiNoteEvent(note, NoteTrigger.ON)
            midiRepo?.send(midiEvent)
        }

        fun sendNoteOff(channel: Int, note: Int, velocity: Float) {
            val vi = clamp(0f, 127f, Math.floor((velocity * 127 + 0.5f).toDouble()).toFloat()).toInt()
            Log.d("sendNoteOff", String.format("/vkb_midi/%d/note/%d=%d", channel, note, vi))
            val midiEvent = MidiNoteEvent(note, NoteTrigger.OFF)
            midiRepo?.send(midiEvent)
        }
    }

    // ======================================================================
    private inner class GridFinger internal constructor(c: Int) {
        var channel = 0
        var touch = PointF()
        var current = PointF()
        var pressure = 0.0f
            // scale (minDomain,maxDomain) value to (minRange,maxRange)
            set(value) {
                field = clamp(
                    minPressureRange, maxPressureRange,
                    (value - minPressureDomain) / (maxPressureDomain - minPressureDomain)
                )
            }
        var lightMatrix = FloatArray(16)
        var lightRect = GridRects(0.5f, 0.5f, 0.5f, 1.0f)
        var active = false
        var note = 0
        val modulationX: Float
            get() = current.x - touch.x
        val modulationY: Float
            get() = current.y - touch.y

        init {
            channel = c
        }

        fun eventDown() {
            touch.set(current)
            note = xyToNote(touch.x, touch.y)
            if (active) {
                Log.e("eventDown", String.format("MISSED NOTE OFF on Channel %d", channel))
            }
            active = true
            // light rect
            val x = Math.floor((touch.x / cellWidth).toDouble()).toFloat() * cellWidth + cellWidth / 2
            val y = Math.floor((touch.y / cellHeight).toDouble()).toFloat() * cellHeight + cellHeight / 2
            Matrix.setIdentityM(lightMatrix, 0)
            Matrix.translateM(lightMatrix, 0, x, y, 0.0f)
            lightRect.setModelMatrix(lightMatrix)
            lightRect.setColor(pressure, pressure, pressure, 1.0f)

            oscCtrl.sendPressure(channel, 0f)
            oscCtrl.sendModulationX(channel, 0f)
            oscCtrl.sendModulationY(channel, 0f)
            oscCtrl.sendNoteOn(channel, note, pressure)
        }

        fun eventUp() {
            if (!active) {
                Log.e("eventUp", String.format("MISSED NOTE ON on Channel %d", channel))
            }
            active = false
            // light rect offscreen
            Matrix.setIdentityM(lightMatrix, 0)
            Matrix.translateM(lightMatrix, 0, -cellWidth / 2, -cellHeight / 2, 0.0f)
            lightRect.setModelMatrix(lightMatrix)

            oscCtrl.sendNoteOff(channel, note, 0.0f)
        }

        fun eventMove() {
            if (!active) {
                if (modulationX + modulationY != 0f) {
                    Log.e("eventMove", String.format("MISSING NOTE ON on Channel %d", channel))
                }
            }
            lightRect.setColor(pressure, pressure, pressure, 1.0f)

            oscCtrl.sendPressure(channel, pressure)
            oscCtrl.sendModulationX(channel, modulationX)
            oscCtrl.sendModulationY(channel, modulationY)
        }
    }

    companion object {
        internal fun clamp(min: Float, max: Float, x: Float): Float {
            return if (x > max) max else if (x < min) min else x
        }
    }
}
