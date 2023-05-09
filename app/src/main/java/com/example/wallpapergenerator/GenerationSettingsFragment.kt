package com.example.wallpapergenerator

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.ImageGenerator.Companion.fractalColoringTypeNames
import com.example.wallpapergenerator.ImageGenerator.Companion.fractalTypeNames
import com.example.wallpapergenerator.ImageGenerator.Companion.gradientTypeNames
import com.example.wallpapergenerator.databinding.FragmentGenerationParametersBinding
import com.example.wallpapergenerator.ImageGenerator.Companion.FractalColoringType
import java.lang.Integer.max
import java.lang.Integer.min


class GenerationSettingsFragment : Fragment() {

    private lateinit var binding: FragmentGenerationParametersBinding
    private lateinit var parametersHolder: GenerationActivity.ParametersHolder
    private lateinit var adapter: GenerationSettingsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGenerationParametersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parametersHolder = ViewModelProvider(requireActivity())[GenerationActivity.ParametersHolder::class.java]

        adapter = GenerationSettingsRecyclerViewAdapter()
        binding.paramsList.adapter = adapter

        updateParameters()
    }

    fun updateParameters() {
        lateinit var params: List<GenerationParameter>

        if (parametersHolder.currentGenerationType == GenerationType.Noise) {
            params = mutableListOf<GenerationParameter>()
            params.add(
                CheckboxParameter(
                    "Случайное количество слоёв",
                    parametersHolder.noiseParameters.isLevelCountRandom
                ) { isChecked ->
                    parametersHolder.noiseParameters.isLevelCountRandom = isChecked
                    updateParameters()
                }
            )
            if (!parametersHolder.noiseParameters.isLevelCountRandom)
                params.add(
                    InputDigitParameter(
                        "Количество слоёв", parametersHolder.noiseParameters.levelCount
                    ) { number -> parametersHolder.noiseParameters.levelCount = number }
                )

            params.add(
                CheckboxParameter(
                    "Случайный верхний цвет",
                    parametersHolder.noiseParameters.isTopColorRandom
                ) { isChecked ->
                    parametersHolder.noiseParameters.isTopColorRandom = isChecked
                    updateParameters()
                }
            )
            if (!parametersHolder.noiseParameters.isTopColorRandom)
                params.add(
                    ColorParameter(
                        "Верхний цвет",
                        parametersHolder.noiseParameters.topColor,
                    ) { color -> parametersHolder.noiseParameters.topColor = color }
                )

            params.add(
                CheckboxParameter(
                    "Случайный нижний цвет",
                    parametersHolder.noiseParameters.isBottomColorRandom
                ) { isChecked ->
                    parametersHolder.noiseParameters.isBottomColorRandom = isChecked
                    updateParameters()
                }
            )
            if (!parametersHolder.noiseParameters.isBottomColorRandom) {
                params.add(
                    ColorParameter(
                        "Нижний цвет",
                        parametersHolder.noiseParameters.bottomColor,
                    ) { color -> parametersHolder.noiseParameters.bottomColor = color }
                )
            }

        } else if (parametersHolder.currentGenerationType == GenerationType.Shapes) {
            params = mutableListOf<GenerationParameter>()

        } else if (parametersHolder.currentGenerationType == GenerationType.Gradients) {
            params = mutableListOf<GenerationParameter>(
                DropdownParameter(
                    "Тип",
                    parametersHolder.gradientParameters.gradientType.ordinal,
                    gradientTypeNames
                ) { optionNum -> parametersHolder.gradientParameters.gradientType = optionNum.toEnum() }
            )
            params.add(
                CheckboxParameter(
                    "Случайные цвета",
                    parametersHolder.gradientParameters.isColorsRandom
                ) {
                    isChecked -> parametersHolder.gradientParameters.isColorsRandom = isChecked
                    updateParameters()
                }
            )
            if (parametersHolder.gradientParameters.isColorsRandom) {
                params.add(
                    InputDigitParameter(
                        "Не меньше чем",
                        parametersHolder.gradientParameters.minColorsCount
                    ) { number -> parametersHolder.gradientParameters.minColorsCount = number }
                )
                params.add(
                    InputDigitParameter(
                        "Не больше чем",
                        parametersHolder.gradientParameters.maxColorsCount
                    ) { number -> parametersHolder.gradientParameters.maxColorsCount = number }
                )
            } else {
                params.add(
                    InputDigitParameter(
                        "Число цветов",
                        parametersHolder.gradientParameters.minColorsCount
                    ) {
                        number -> parametersHolder.gradientParameters.colorsCount = number
                        updateParameters()
                    }
                )
                for (i in 0 until parametersHolder.gradientParameters.minColorsCount) {
                    params.add(
                        ColorParameter(
                            "Цвет №$i",
                            parametersHolder.gradientParameters.colors[i]
                        ) { color -> parametersHolder.gradientParameters.colors[i] = color }
                    )
                }
            }

        } else if (parametersHolder.currentGenerationType == GenerationType.Fractals) {
            params = mutableListOf<GenerationParameter>(
                DropdownParameter(
                    "Фрактал",
                    parametersHolder.fractalParameters.fractalType.ordinal,
                    fractalTypeNames
                ) { optionNum -> parametersHolder.fractalParameters.fractalType = optionNum.toEnum() }
            )
            params.add(
                DropdownParameter(
                    "Тип закраски",
                    parametersHolder.fractalParameters.coloringType.ordinal,
                    fractalColoringTypeNames
                ) {
                    optionNum -> parametersHolder.fractalParameters.coloringType = optionNum.toEnum()
                    updateParameters()
                }
            )
            params.add(
                InputDigitParameter(
                    "Макс. глубина",
                    parametersHolder.fractalParameters.depth
                ) { number -> parametersHolder.fractalParameters.depth = number }
            )
            params.add(
                CheckboxParameter(
                    "Случайный зум",
                    parametersHolder.fractalParameters.isZoomRandom
                ) { isChecked ->
                    parametersHolder.fractalParameters.isZoomRandom = isChecked
                    updateParameters()
                }
            )
            if (!parametersHolder.fractalParameters.isZoomRandom) {
                params.add(
                    InputDigitParameter(
                        "Зум",
                        parametersHolder.fractalParameters.zoom
                    ) { number -> parametersHolder.fractalParameters.zoom = min(100, max(0, number)) }
                )
            }
            params.add(
                CheckboxParameter(
                    "Случайный отступ",
                    parametersHolder.fractalParameters.isOffsetRandom
                ) { isChecked ->
                    parametersHolder.fractalParameters.isOffsetRandom = isChecked
                    updateParameters()
                }
            )
            if (!parametersHolder.fractalParameters.isOffsetRandom) {
                params.add(
                    InputDigitParameter(
                        "X отступ",
                        parametersHolder.fractalParameters.offsetX
                    ) { number -> parametersHolder.fractalParameters.offsetX = min(100, max(0, number)) }
                )
                params.add(
                    InputDigitParameter(
                        "Y отступ",
                        parametersHolder.fractalParameters.offsetY
                    ) { number -> parametersHolder.fractalParameters.offsetY = min(100, max(0, number)) }
                )
            }
            if (parametersHolder.fractalParameters.coloringType == FractalColoringType.Lerp) {
                params.add(
                    CheckboxParameter(
                        "Случайный верхний цвет",
                        parametersHolder.fractalParameters.isTopColorRandom
                    ) { isChecked ->
                        parametersHolder.fractalParameters.isTopColorRandom = isChecked
                        updateParameters()
                    }
                )
                if (!parametersHolder.fractalParameters.isTopColorRandom)
                    params.add(
                        ColorParameter(
                            "Верхний цвет",
                            parametersHolder.fractalParameters.topColor,
                        ) { color -> parametersHolder.fractalParameters.topColor = color }
                    )

                params.add(
                    CheckboxParameter(
                        "Случайный нижний цвет",
                        parametersHolder.fractalParameters.isBottomColorRandom
                    ) { isChecked ->
                        parametersHolder.fractalParameters.isBottomColorRandom = isChecked
                        updateParameters()
                    }
                )
                if (!parametersHolder.fractalParameters.isBottomColorRandom) {
                    params.add(
                        ColorParameter(
                            "Нижний цвет",
                            parametersHolder.fractalParameters.bottomColor,
                        ) { color -> parametersHolder.fractalParameters.bottomColor = color }
                    )
                }
            }
        } else {
            throw NotImplementedError()
        }

        adapter.submitList(params)
    }

    inline fun <reified T : Enum<T>> Int.toEnum(): T {
        return enumValues<T>().first { it.ordinal == this }
    }
}