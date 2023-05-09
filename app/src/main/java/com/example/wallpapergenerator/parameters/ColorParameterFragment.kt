package com.example.wallpapergenerator.parameters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.example.wallpapergenerator.R
import com.google.android.material.textview.MaterialTextView
import java.lang.Integer.max
import java.lang.Integer.min

class ColorParameterFragment(
    private val description: String,
    private var color: Int = Color.WHITE,
    private val onColorChanged: ((color: Int) -> Unit)? = null
) : Fragment() {
    private lateinit var colorBox: GradientDrawable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("OnCreate!!")
        return inflater.inflate(R.layout.fragment_color_parameter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorBox = (view.findViewById<MaterialTextView>(R.id.colorCube)?.background as GradientDrawable)
        colorBox.setColor(color)
        view.findViewById<MaterialTextView>(R.id.descriptionField)?.text = description

        val inputRed = view.findViewById<EditText>(R.id.inputRed)
        inputRed.setText(Color.red(color).toString())
        inputRed.addTextChangedListener {
                text: Editable? ->
            val validNumber = validateNumber(text, inputRed)
            if (validNumber != null)
                changeColor(Color.rgb(validNumber, Color.green(color), Color.blue(color)))
        }

        val inputGreen = view.findViewById<EditText>(R.id.inputGreen)
        inputGreen.setText(Color.green(color).toString())
        inputGreen.addTextChangedListener {
                text: Editable? ->
            val validNumber = validateNumber(text, inputGreen)
            if (validNumber != null)
                changeColor(Color.rgb(Color.red(color), validNumber, Color.blue(color)))
        }

        val inputBlue = view.findViewById<EditText>(R.id.inputBlue)
        inputBlue.setText(Color.blue(color).toString())
        inputBlue.addTextChangedListener {
                text: Editable? ->
            val validNumber = validateNumber(text, inputBlue)
            if (validNumber != null)
                changeColor(Color.rgb(Color.red(color), Color.green(color), validNumber))
        }
    }

    private fun validateNumber(text: Editable?, input: EditText) : Int? {
        if (text != null && text.isNotEmpty()) {
            val number = text.toString().toInt()
            val validNumber = max(0, min(255, number))
            if (validNumber != number)
                input.setText(validNumber.toString())
            return validNumber
        }
        return null
    }

    fun changeColor(newColor: Int) {
        color = newColor
        colorBox.setColor(color)
        onColorChanged?.invoke(color)
    }
}