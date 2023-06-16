package com.example.wallpapergenerator.adapters.generationsettingsadapter

sealed class SettingsParameter(open val text: String)

data class CheckboxParameter(
    override val text: String,
    val isChecked: Boolean,
    val onCheckboxChanged: ((isChecked: Boolean) -> Unit)? = null
) : SettingsParameter(text)

data class DropdownParameter(
    override val text: String,
    val index: Int,
    val options: Array<String>,
    val onOptionSelected: ((optionNum: Int) -> Unit)? = null
) : SettingsParameter(text)

class InputDigitParameter(
    override val text: String,
    val number: Int,
    val minValue: Int,
    val maxValue: Int,
    val onInputEntered: ((number: Int) -> Unit)? = null
) : SettingsParameter(text)

class InputDigitRangeParameter(
    override val text: String,
    val numberFrom: Int,
    val numberTo: Int,
    val minValue: Int,
    val maxValue: Int,
    val onInputEntered: ((numberFrom: Int, numberTo: Int) -> Unit)? = null
) : SettingsParameter(text)

class ColorParameter(
    override val text: String,
    val color: Int,
    val onColorChanged: ((color: Int) -> Unit)? = null
) : SettingsParameter(text)