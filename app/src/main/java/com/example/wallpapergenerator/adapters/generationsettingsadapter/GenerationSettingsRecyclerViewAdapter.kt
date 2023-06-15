package com.example.wallpapergenerator

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.DiffUtil
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*

import com.google.android.material.textview.MaterialTextView

class GenerationSettingsRecyclerViewAdapter() : androidx.recyclerview.widget.ListAdapter<GenerationParameter,
        SettingsViewHolder>(MyDiffUtil())  {

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
                .inflate(R.layout.fragment_color_parameter, parent, false))
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

    class MyDiffUtil() : DiffUtil.ItemCallback<GenerationParameter>() {
        override fun areItemsTheSame(oldItem: GenerationParameter, newItem: GenerationParameter): Boolean {
            return oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: GenerationParameter, newItem: GenerationParameter): Boolean {
            return oldItem.text == newItem.text
        }
    }
}