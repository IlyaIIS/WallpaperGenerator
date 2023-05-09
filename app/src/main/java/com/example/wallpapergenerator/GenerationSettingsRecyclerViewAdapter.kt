package com.example.wallpapergenerator

import android.app.Activity
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil

import com.example.wallpapergenerator.parameters.ColorParameterFragment
import com.google.android.material.textview.MaterialTextView

class GenerationSettingsRecyclerViewAdapter() : androidx.recyclerview.widget.ListAdapter<GenerationParameter,
        GenerationSettingsRecyclerViewAdapter.MyViewHolder>(MyDiffUtil())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolder {
        return when (viewType) {
            0 -> MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_checkbox_parameter, parent, false))
            1 -> MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_dropdown_parameter, parent, false))
            2 -> MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_input_digit_parameter, parent, false))
            3 -> MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_conteiner_view, parent, false))
            4 -> MyViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_dropdown_parameter, parent, false))
            else -> throw NotImplementedError()
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        when(currentList[position]::class) {

            CheckboxParameter::class -> {
                val checkBox = holder.itemView as CheckBox
                val parameter = currentList[position] as CheckboxParameter
                checkBox.setOnCheckedChangeListener { _, isChecked -> parameter.onCheckboxChanged?.invoke(isChecked) }
                checkBox.text = parameter.text
                checkBox.isChecked = parameter.isChecked
            }

            DropdownParameter::class -> {
                val parameter = currentList[position] as DropdownParameter
                holder.itemView.findViewById<MaterialTextView>(R.id.descriptionField).text = parameter.text
                val view = holder.itemView.findViewById<Spinner>(R.id.spinnerDropdown)
                view.setSelection(parameter.index)
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    holder.itemView.context,
                    android.R.layout.simple_spinner_item, parameter.options
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                view.adapter = adapter

                view.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected( parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            parameter.onOptionSelected?.invoke(position)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }

            InputDigitParameter::class -> {
                val view = holder.itemView.findViewById<EditText>(R.id.inputField)
                val parameter = currentList[position] as InputDigitParameter
                view.setOnFocusChangeListener { view, isFocused ->
                    if (isFocused) {
                        val inputMethodManager = holder.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(holder.itemView, InputMethodManager.SHOW_IMPLICIT)
                    } else {
                        parameter.onInputEntered?.invoke((view as EditText).text.toString().toInt())
                        val inputMethodManager = holder.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(holder.itemView.windowToken, 0)
                    }
                }
                view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        parameter.onInputEntered?.invoke((view as EditText).text.toString().toInt())
                        val inputMethodManager = holder.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(holder.itemView.windowToken, 0)
                        return@OnKeyListener true
                    }
                    false
                })

                view.setText(parameter.number.toString())
                holder.itemView.findViewById<MaterialTextView>(R.id.descriptionField).text = parameter.text
            }

            ColorParameter::class -> {
                val view = (holder.itemView as FragmentContainerView)
                val parameter = currentList[position] as ColorParameter
                view.id = R.id.viewFragmentContainer + parameter.text.hashCode()//View.generateViewId()
                val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
                if ((fragmentManager.findFragmentByTag(parameter.text) == null)) {
                    println("IsNull!! in " + view.id.toString())
                    val fragment = ColorParameterFragment(
                        parameter.text,
                        parameter.color,
                        parameter.onColorChanged
                    )
                    fragmentManager.beginTransaction().replace(view.id, fragment, parameter.text).commit()
                }
            }

            else -> throw NotImplementedError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]::class) {
            CheckboxParameter::class -> 0
            DropdownParameter::class -> 1
            InputDigitParameter::class -> 2
            ColorParameter::class -> 3
            else -> throw NotImplementedError()
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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


abstract class GenerationParameter(val text: String)

class CheckboxParameter(
    text: String,
    val isChecked: Boolean,
    val onCheckboxChanged: ((isChecked: Boolean) -> Unit)? = null
) : GenerationParameter(text)

class DropdownParameter(
    text: String,
    val index: Int,
    val options: Array<String>,
    val onOptionSelected: ((optionNum: Int) -> Unit)? = null
) : GenerationParameter(text)

class InputDigitParameter(
    text: String,
    val number: Int,
    val onInputEntered: ((number: Int) -> Unit)? = null
) : GenerationParameter(text)

class ColorParameter(
    text: String,
    val color: Int,
    val onColorChanged: ((color: Int) -> Unit)? = null
) : GenerationParameter(text)
