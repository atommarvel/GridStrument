package com.gmail.rallen.gridstrument

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.gmail.rallen.gridstrument.extensions.tryLog
import java.util.ArrayList

/**
 * Found a template for this at:
 * http://www.i-programmer.info/programming/android/7647-android-adventures-a-numberpicker-dialogfragment-project.html
 */
class TuningDialogFragment : DialogFragment() {
    private var values: ArrayList<Int> = ArrayList()
    private var numPickers: Array<NumberPicker> = arrayOf()
    private var mListener: OnTuningDialogDoneListener? = null

    private val numberPickerFormatter by lazy {
        NumberPicker.Formatter { value ->
            val note = value % 12
            val noteString = notes[note]
            val octaveString = " " + (value / 12 - 1)
            noteString + octaveString
        }
    }

    interface OnTuningDialogDoneListener {
        fun onTuningDialogDone(values: ArrayList<Int>?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        values = savedInstanceState?.getIntegerArrayList(KEY_CURR_VALUES) ?: arguments?.getIntegerArrayList(KEY_BASE_NOTES) ?: ArrayList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntegerArrayList(KEY_CURR_VALUES, values)
    }

    private fun getValuesFromPickers() {
        values = ArrayList(numPickers.map { it.value })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val linLayoutH = LinearLayout(activity)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linLayoutH.layoutParams = params

        numPickers = values.map { value ->
            val numberPicker = NumberPicker(context).apply {
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                maxValue = 127
                minValue = 0
                this.value = value
                setFormatter(numberPickerFormatter)
                // TODO: debug
                // https://code.google.com/p/android/issues/detail?id=35482
                // This causes horrible exceptions in the ADB logs, but is otherwise harmless, I guess.
                // Without this, the selected values are not properly displayed.
                tryLog {
                    val method = javaClass.getDeclaredMethod("changeValueByOne", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(this, true)
                }
            }
            linLayoutH.addView(numberPicker)
            numberPicker
        }.toTypedArray()

        val linLayoutV = LinearLayout(context)
        linLayoutV.orientation = LinearLayout.VERTICAL
        linLayoutV.addView(linLayoutH)

        val okButton = Button(context)
        okButton.setOnClickListener {
            getValuesFromPickers()
            mListener?.onTuningDialogDone(values)
            dismiss()
        }
        params.gravity = Gravity.CENTER_HORIZONTAL
        okButton.layoutParams = params
        okButton.text = "Done"
        linLayoutV.addView(okButton)

        return linLayoutV
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = activity as OnTuningDialogDoneListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnTuningDialogDoneListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    companion object {
        private const val KEY_BASE_NOTES = "base_notes"
        private const val KEY_CURR_VALUES = "curr_values"
        private val notes = arrayOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")

        fun newInstance(initValues: ArrayList<Int>): TuningDialogFragment = TuningDialogFragment().apply {
            arguments = bundleOf(KEY_BASE_NOTES to initValues)
        }
    }
}
