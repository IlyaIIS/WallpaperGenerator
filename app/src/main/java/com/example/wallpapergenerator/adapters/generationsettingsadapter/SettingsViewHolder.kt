package com.example.wallpapergenerator.adapters.generationsettingsadapter

import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

sealed class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun initialize(parameter: SettingsParameter)
}

class CheckBoxViewHolder(itemView: View) : SettingsViewHolder(itemView) {
    override fun initialize(parameter: SettingsParameter) {
        if (parameter is CheckboxParameter) {
            val checkBox = itemView as CheckBox
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                parameter.onCheckboxChanged?.invoke(isChecked)
            }
            checkBox.text = parameter.text
            checkBox.isChecked = parameter.isChecked
        }
    }
}

class DropdownViewHolder(itemView: View) : SettingsViewHolder(itemView) {
    override fun initialize(parameter: SettingsParameter) {
        if (parameter is DropdownParameter) {
            itemView.findViewById<MaterialTextView>(com.example.wallpapergenerator.R.id.descriptionField).text = parameter.text
            val view = itemView.findViewById<Spinner>(com.example.wallpapergenerator.R.id.spinnerDropdown)
            view.setSelection(parameter.index)
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                itemView.context,
                R.layout.simple_spinner_item, parameter.options
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.adapter = adapter

            view.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        parameter.onOptionSelected?.invoke(position)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
        }
    }
}

class InputDigitViewHolder(itemView: View) : SettingsViewHolder(itemView) {
    override fun initialize(parameter: SettingsParameter) {
        if (parameter is InputDigitParameter) {
            fun applyChanges(inputField: EditText, parameter: InputDigitParameter) {
                val value = inputField.text.toString().toInt()
                val validateValue = value.coerceIn(parameter.minValue, parameter.maxValue)
                if (value != validateValue)
                    inputField.setText(validateValue.toString())
                parameter.onInputEntered?.invoke(validateValue)
                val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(itemView.windowToken, 0)
            }
            val inputField = itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputField)
            inputField.setOnFocusChangeListener { _, isFocused ->
                if (isFocused) {
                    val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(itemView, InputMethodManager.SHOW_IMPLICIT)
                } else {
                    applyChanges(inputField, parameter)
                }
            }
            inputField.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    applyChanges(inputField, parameter)
                    return@OnKeyListener true
                }
                false
            })

            inputField.setText(parameter.number.toString())
            itemView.findViewById<MaterialTextView>(com.example.wallpapergenerator.R.id.descriptionField).text = parameter.text
        }
    }
}

class InputDigitRangeViewHolder(itemView: View) : SettingsViewHolder(itemView) {
    override fun initialize(parameter: SettingsParameter) {
        if (parameter is InputDigitRangeParameter) {
            fun applyChanges(inputFieldFrom: EditText, inputFieldTo: EditText, parameter: InputDigitRangeParameter) {
                val valueFrom = inputFieldFrom.text.toString().toInt()
                val valueTo = inputFieldTo.text.toString().toInt()
                val validateValueFrom = valueFrom.coerceIn(parameter.minValue, parameter.maxValue)
                val validateValueTo = valueTo.coerceIn(validateValueFrom, parameter.maxValue)
                if (valueFrom != validateValueFrom)
                    inputFieldFrom.setText(validateValueFrom.toString())
                if (valueTo != validateValueTo)
                    inputFieldTo.setText(validateValueTo.toString())
                parameter.onInputEntered?.invoke(validateValueFrom, validateValueTo)
                val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(itemView.windowToken, 0)
            }

            val inputFieldFrom = itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputFieldFrom)
            val inputFieldTo = itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputFieldTo)

            fun setHandleEvents(inputField: EditText) {
                inputField.setOnFocusChangeListener { _, isFocused ->
                    if (isFocused) {
                        val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(itemView, InputMethodManager.SHOW_IMPLICIT)
                    } else {
                        applyChanges(inputFieldFrom, inputFieldTo, parameter)
                    }
                }
                inputField.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        applyChanges(inputFieldFrom, inputFieldTo, parameter)
                        return@OnKeyListener true
                    }
                    false
                })
            }

            setHandleEvents(inputFieldFrom)
            setHandleEvents(inputFieldTo)

            inputFieldFrom.setText(parameter.numberFrom.toString())
            inputFieldTo.setText(parameter.numberTo.toString())
            itemView.findViewById<MaterialTextView>(com.example.wallpapergenerator.R.id.descriptionField).text = parameter.text
        }
    }
}

class ColorViewHolder(itemView: View) : SettingsViewHolder(itemView) {
    private lateinit var colorBox: GradientDrawable
    private lateinit var description: String
    private var color: Int = Color.WHITE
    private var onColorChanged: ((color: Int) -> Unit)? = null

    override fun initialize(parameter: SettingsParameter) {
        if (parameter is ColorParameter) {
            description = parameter.text
            color = parameter.color
            onColorChanged = parameter.onColorChanged

            colorBox = (itemView.findViewById<MaterialTextView>(com.example.wallpapergenerator.R.id.colorCube)?.background as GradientDrawable)
            colorBox.setColor(color)
            itemView.findViewById<MaterialTextView>(com.example.wallpapergenerator.R.id.descriptionField)?.text = description

            itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputRed).run {
                setText(Color.red(color).toString())
                addTextChangedListener {
                        text: Editable? ->
                    val validNumber = validateNumber(text, this)
                    if (validNumber != null)
                        changeColor(Color.rgb(validNumber, Color.green(color), Color.blue(color)))
                }
            }


            itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputGreen).run {
                setText(Color.green(color).toString())
                addTextChangedListener {
                        text: Editable? ->
                    val validNumber = validateNumber(text, this)
                    if (validNumber != null)
                        changeColor(Color.rgb(Color.red(color), validNumber, Color.blue(color)))
                }
            }


            itemView.findViewById<EditText>(com.example.wallpapergenerator.R.id.inputBlue).run {
                setText(Color.blue(color).toString())
                addTextChangedListener {
                        text: Editable? ->
                    val validNumber = validateNumber(text, this)
                    if (validNumber != null)
                        changeColor(Color.rgb(Color.red(color), Color.green(color), validNumber))
                }
            }


/*                view.id = R.id.viewFragmentContainer + parameter.text.hashCode() использование фрагмента
                val fragmentManager = (itemView.context as AppCompatActivity).supportFragmentManager
                if ((fragmentManager.findFragmentByTag(parameter.text) == null)) {
                    val fragment = ColorParameterFragment(
                        parameter.text,
                        parameter.color,
                        parameter.onColorChanged
                    )
                    fragmentManager.beginTransaction().replace(view.id, fragment, parameter.text).commit()
                }*/
        }
    }

    private fun validateNumber(text: Editable?, input: EditText) : Int? {
        if (text != null && text.isNotEmpty()) {
            val number = text.toString().toInt()
            val validNumber = Integer.max(0, Integer.min(255, number))
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