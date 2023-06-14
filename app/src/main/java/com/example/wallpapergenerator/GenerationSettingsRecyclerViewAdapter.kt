package com.example.wallpapergenerator

import android.content.Context
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.DiffUtil

import com.example.wallpapergenerator.parameters.ColorParameterFragment
import com.google.android.material.textview.MaterialTextView

class GenerationSettingsRecyclerViewAdapter() : androidx.recyclerview.widget.ListAdapter<GenerationParameter,
        GenerationSettingsRecyclerViewAdapter.SettingsViewHolder>(MyDiffUtil())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : SettingsViewHolder {
        return when (viewType) {
            0 -> CheckBoxViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_checkbox_parameter, parent, false))
            1 -> DropdownViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_dropdown_parameter, parent, false))
            2 -> InputDigitViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_input_digit_parameter, parent, false))
            3 -> InputDigitRangeViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_input_digit_range_parameter, parent, false))
            4 -> ColorViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_conteiner_view, parent, false))
            else -> throw NotImplementedError()
        }
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.initialize(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]::class) {
            CheckboxParameter::class -> 0
            DropdownParameter::class -> 1
            InputDigitParameter::class -> 2
            InputDigitRangeParameter::class -> 3
            ColorParameter::class -> 4
            else -> throw NotImplementedError()
        }
    }

    sealed class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun initialize(parameter: GenerationParameter)
    }

    class CheckBoxViewHolder(itemView: View) : SettingsViewHolder(itemView) {
        override fun initialize(parameter: GenerationParameter) {
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
        override fun initialize(parameter: GenerationParameter) {
            if (parameter is ColorParameter) {
                val view = (itemView as FragmentContainerView)
                view.id = R.id.viewFragmentContainer + parameter.text.hashCode()//View.generateViewId()
                val fragmentManager = (itemView.context as AppCompatActivity).supportFragmentManager
                if ((fragmentManager.findFragmentByTag(parameter.text) == null)) {
                    val fragment = ColorParameterFragment(
                        parameter.text,
                        parameter.color,
                        parameter.onColorChanged
                    )
                    fragmentManager.beginTransaction().replace(view.id, fragment, parameter.text).commit()
                }
            }
        }
    }

    class InputDigitViewHolder(itemView: View) : SettingsViewHolder(itemView) {
        override fun initialize(parameter: GenerationParameter) {
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
                val inputField = itemView.findViewById<EditText>(R.id.inputField)
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
                itemView.findViewById<MaterialTextView>(R.id.descriptionField).text = parameter.text
            }
        }
    }

    class InputDigitRangeViewHolder(itemView: View) : SettingsViewHolder(itemView) {
        override fun initialize(parameter: GenerationParameter) {
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

                val inputFieldFrom = itemView.findViewById<EditText>(R.id.inputFieldFrom)
                val inputFieldTo = itemView.findViewById<EditText>(R.id.inputFieldTo)

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
                itemView.findViewById<MaterialTextView>(R.id.descriptionField).text = parameter.text
            }
        }
    }

    class ColorViewHolder(itemView: View) : SettingsViewHolder(itemView) {
        override fun initialize(parameter: GenerationParameter) {
            if (parameter is ColorParameter) {
                val view = (itemView as FragmentContainerView)
                view.id = R.id.viewFragmentContainer + parameter.text.hashCode()//View.generateViewId()
                val fragmentManager = (itemView.context as AppCompatActivity).supportFragmentManager
                if ((fragmentManager.findFragmentByTag(parameter.text) == null)) {
                    val fragment = ColorParameterFragment(
                        parameter.text,
                        parameter.color,
                        parameter.onColorChanged
                    )
                    fragmentManager.beginTransaction().replace(view.id, fragment, parameter.text).commit()
                }
            }
        }
    }

    class MyDiffUtil() : DiffUtil.ItemCallback<GenerationParameter>() {
        override fun areItemsTheSame(oldItem: GenerationParameter, newItem: GenerationParameter): Boolean {
            return oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: GenerationParameter, newItem: GenerationParameter): Boolean {
            return oldItem.text == newItem.text
        }
    }
}


sealed class GenerationParameter(open val text: String)

data class CheckboxParameter(
    override val text: String,
    val isChecked: Boolean,
    val onCheckboxChanged: ((isChecked: Boolean) -> Unit)? = null
) : GenerationParameter(text)

data class DropdownParameter(
    override val text: String,
    val index: Int,
    val options: Array<String>,
    val onOptionSelected: ((optionNum: Int) -> Unit)? = null
) : GenerationParameter(text)

class InputDigitParameter(
    override val text: String,
    val number: Int,
    val minValue: Int,
    val maxValue: Int,
    val onInputEntered: ((number: Int) -> Unit)? = null
) : GenerationParameter(text)

class InputDigitRangeParameter(
    override val text: String,
    val numberFrom: Int,
    val numberTo: Int,
    val minValue: Int,
    val maxValue: Int,
    val onInputEntered: ((numberFrom: Int, numberTo: Int) -> Unit)? = null
) : GenerationParameter(text)

class ColorParameter(
    override val text: String,
    val color: Int,
    val onColorChanged: ((color: Int) -> Unit)? = null
) : GenerationParameter(text)
