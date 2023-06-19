package com.example.wallpapergenerator.parameterholders

import android.graphics.Color
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.imagegeneration.FractalImageGenerator
import com.example.wallpapergenerator.imagegeneration.GenerationType
import com.example.wallpapergenerator.imagegeneration.GradientImageGenerator
import com.example.wallpapergenerator.repository.LocalRepository
import com.google.gson.Gson
import javax.inject.Inject
import kotlin.random.Random

data class InterferenceParameters(
    var isLevelCountRandom: Boolean,
    var levelCount: Int,
    var isTopColorRandom: Boolean,
    var topColor: Int,
    var isBottomColorRandom: Boolean,
    var bottomColor: Int,
)

data class GradientParameters(
    var gradientType: GradientImageGenerator.GradientType,
    var isColorsRandom: Boolean,
    var minColorsCount: Int,
    var maxColorsCount: Int,
    var colorsCount: Int,
    var colors: MutableList<Int>
)

data class FractalParameters(
    var fractalType: FractalImageGenerator.FractalType,
    var coloringType: FractalImageGenerator.FractalColoringType,
    var depth: Int,
    var isOffsetRandom: Boolean,
    var offsetX: Int,
    var offsetY: Int,
    var isZoomRandom: Boolean,
    var zoom: Int,
    var isTopColorRandom: Boolean,
    var topColor: Int,
    var isBottomColorRandom: Boolean,
    var bottomColor: Int,
)

data class ShapeParameters(
    var isBackgroundColorRandom: Boolean,
    var backgroundColor: Int,
    var minShapeCount: Int,
    var maxShapeCount: Int,
    var borderChance: Int,
    var ableCircles: Boolean,
    var ableRectangles: Boolean,
    var ableTriangles: Boolean,
    var ableLines: Boolean,
    var ablePlanes: Boolean,
)

class GenerationParametersHolder @Inject constructor(private val repository: LocalRepository) : ParameterHolder() {
    lateinit var currentGenerationType: GenerationType
    var onParameterChanged: () -> Unit = { }

    var interferenceParameters = InterferenceParameters(
        true, 4,
        true, Color.RED,
        true, Color.BLUE
    )

    var gradientParameters = GradientParameters(
        GradientImageGenerator.GradientType.StrictLine,
        true,
        3,
        7,
        4,
        mutableListOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
    )

    var fractalParameters = FractalParameters(
        FractalImageGenerator.FractalType.JuliaSet,
        FractalImageGenerator.FractalColoringType.MODULE,
        32,
        true,
        50,
        50,
        true,
        0,
        true,
        Color.rgb(0, 200, 255),
        true,
        Color.GRAY
    )

    var shapeParameters = ShapeParameters(
        false,
        Color.WHITE,
        5,
        9,
        50,
        true, true, true, true, true,
    )

    private fun saveParameters() {
        repository.saveSetting(interferenceParameters::class.simpleName!!, Gson().toJson(interferenceParameters))
        repository.saveSetting(gradientParameters::class.simpleName!!, Gson().toJson(gradientParameters))
        repository.saveSetting(fractalParameters::class.simpleName!!, Gson().toJson(fractalParameters))
        repository.saveSetting(shapeParameters::class.simpleName!!, Gson().toJson(shapeParameters))
    }

    fun loadParameters() {
        interferenceParameters = Gson().fromJson(repository.readSettingString(interferenceParameters::class.simpleName!!), InterferenceParameters::class.java) ?: interferenceParameters
        gradientParameters = Gson().fromJson(repository.readSettingString(gradientParameters::class.simpleName!!), GradientParameters::class.java) ?: gradientParameters
        fractalParameters = Gson().fromJson(repository.readSettingString(fractalParameters::class.simpleName!!), FractalParameters::class.java) ?: fractalParameters
        shapeParameters = Gson().fromJson(repository.readSettingString(shapeParameters::class.simpleName!!), ShapeParameters::class.java) ?: shapeParameters
    }
    override fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter> {
        lateinit var params: List<SettingsParameter>

        if (currentGenerationType == GenerationType.INTERFERENCE) {
            params = mutableListOf()
            params.add(
                CheckboxParameter(
                    "Случайное количество слоёв",
                    interferenceParameters.isLevelCountRandom
                ) { isChecked ->
                    interferenceParameters.isLevelCountRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!interferenceParameters.isLevelCountRandom)
                params.add(
                    InputDigitParameter(
                        "Количество слоёв",
                        interferenceParameters.levelCount,
                        1, 9999
                    ) { number ->
                        interferenceParameters.levelCount = number
                        saveParameters()
                        onParameterChanged()
                    }
                )

            params.add(
                CheckboxParameter(
                    "Случайный верхний цвет",
                    interferenceParameters.isTopColorRandom
                ) { isChecked ->
                    interferenceParameters.isTopColorRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!interferenceParameters.isTopColorRandom)
                params.add(
                    ColorParameter(
                        "Верхний цвет",
                        interferenceParameters.topColor,
                    ) { color ->
                        interferenceParameters.topColor = color
                        saveParameters()
                        onParameterChanged()
                    }
                )

            params.add(
                CheckboxParameter(
                    "Случайный нижний цвет",
                    interferenceParameters.isBottomColorRandom
                ) { isChecked ->
                    interferenceParameters.isBottomColorRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!interferenceParameters.isBottomColorRandom) {
                params.add(
                    ColorParameter(
                        "Нижний цвет",
                        interferenceParameters.bottomColor,
                    ) { color ->
                        interferenceParameters.bottomColor = color
                        saveParameters()
                        onParameterChanged()
                    }
                )
            }

        } else if (currentGenerationType == GenerationType.SHAPES) {
            params = mutableListOf<SettingsParameter>(
                CheckboxParameter(
                    "Случайный фон",
                    shapeParameters.isBackgroundColorRandom
                ) { isChecked ->
                    shapeParameters.isBackgroundColorRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!shapeParameters.isBackgroundColorRandom) {
                params.add (
                    ColorParameter(
                        "Цвет заднего фона",
                        shapeParameters.backgroundColor
                    ) { color ->
                        shapeParameters.backgroundColor = color
                        saveParameters()
                        onParameterChanged()
                    }
                )
            }
            params.add (
                InputDigitRangeParameter(
                    "Число фигур",
                    shapeParameters.minShapeCount,
                    shapeParameters.maxShapeCount,
                    1, 20
                ) { numberFrom, numberTo ->
                    shapeParameters.minShapeCount = numberFrom
                    shapeParameters.maxShapeCount = numberTo
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                InputDigitParameter(
                    "Шанс появления границы",
                    shapeParameters.borderChance,
                    0, 100
                ) { number ->
                    shapeParameters.borderChance = number
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                CheckboxParameter(
                    "Круги",
                    shapeParameters.ableCircles
                ) { isChecked ->
                    shapeParameters.ableCircles = isChecked
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                CheckboxParameter(
                    "Прямоугольники",
                    shapeParameters.ableRectangles
                ) { isChecked ->
                    shapeParameters.ableRectangles = isChecked
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                CheckboxParameter(
                    "Треугольники",
                    shapeParameters.ableTriangles
                ) { isChecked ->
                    shapeParameters.ableTriangles = isChecked
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                CheckboxParameter(
                    "Линии",
                    shapeParameters.ableLines
                ) { isChecked ->
                    shapeParameters.ableLines = isChecked
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add (
                CheckboxParameter(
                    "Плоскости",
                    shapeParameters.ablePlanes
                ) { isChecked ->
                    shapeParameters.ablePlanes = isChecked
                    saveParameters()
                    onParameterChanged()
                }
            )

        } else if (currentGenerationType == GenerationType.GRADIENTS) {
            params = mutableListOf<SettingsParameter>(
                DropdownParameter(
                    "Тип",
                    gradientParameters.gradientType.ordinal,
                    GradientImageGenerator.gradientTypeNames
                ) { optionNum ->
                    gradientParameters.gradientType = optionNum.toEnum()
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add(
                CheckboxParameter(
                    "Случайные цвета",
                    gradientParameters.isColorsRandom
                ) { isChecked ->
                    gradientParameters.isColorsRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (gradientParameters.isColorsRandom) {
                params.add(
                    InputDigitRangeParameter(
                        "Число цветов",
                        gradientParameters.minColorsCount,
                        gradientParameters.maxColorsCount,
                        2, 9999
                    ) { numberFrom, numberTo ->
                        gradientParameters.minColorsCount = numberFrom
                        gradientParameters.maxColorsCount = numberTo
                        saveParameters()
                        onParameterChanged()
                    }
                )
            } else {
                params.add(
                    InputDigitParameter(
                        "Число цветов",
                        gradientParameters.colorsCount,
                        2, 30
                    ) { number ->
                        gradientParameters.colorsCount = number
                        for(i in gradientParameters.colors.size until number)
                            gradientParameters.colors.add(
                                Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
                        updateParameters()
                        saveParameters()
                        onParameterChanged()
                    }
                )
                for (i in 0 until gradientParameters.colorsCount) {
                    params.add(
                        ColorParameter(
                            "Цвет №$i",
                            gradientParameters.colors[i]
                        ) { color ->
                            gradientParameters.colors[i] = color
                            saveParameters()
                            onParameterChanged()
                        }
                    )
                }
            }

        } else if (currentGenerationType == GenerationType.FRACTALS) {
            params = mutableListOf<SettingsParameter>(
                DropdownParameter(
                    "Фрактал",
                    fractalParameters.fractalType.ordinal,
                    FractalImageGenerator.fractalTypeNames
                ) { optionNum ->
                    fractalParameters.fractalType = optionNum.toEnum()
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add(
                DropdownParameter(
                    "Тип закраски",
                    fractalParameters.coloringType.ordinal,
                    FractalImageGenerator.fractalColoringTypeNames
                ) { optionNum ->
                    fractalParameters.coloringType = optionNum.toEnum()
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add(
                InputDigitParameter(
                    "Макс. глубина",
                    fractalParameters.depth,
                    1, 300
                ) { number ->
                    fractalParameters.depth = number
                    saveParameters()
                    onParameterChanged()
                }
            )
            params.add(
                CheckboxParameter(
                    "Случайный зум",
                    fractalParameters.isZoomRandom
                ) { isChecked ->
                    fractalParameters.isZoomRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!fractalParameters.isZoomRandom) {
                params.add(
                    InputDigitParameter(
                        "Зум",
                        fractalParameters.zoom,
                        0, 100
                    ) { number ->
                        fractalParameters.zoom = number
                        saveParameters()
                        onParameterChanged()
                    }
                )
            }
            params.add(
                CheckboxParameter(
                    "Случайный отступ",
                    fractalParameters.isOffsetRandom
                ) { isChecked ->
                    fractalParameters.isOffsetRandom = isChecked
                    updateParameters()
                    saveParameters()
                    onParameterChanged()
                }
            )
            if (!fractalParameters.isOffsetRandom) {
                params.add(
                    InputDigitParameter(
                        "X отступ",
                        fractalParameters.offsetX,
                        0, 100
                    ) { number ->
                        fractalParameters.offsetX = number
                        saveParameters()
                        onParameterChanged()
                    }
                )
                params.add(
                    InputDigitParameter(
                        "Y отступ",
                        fractalParameters.offsetY,
                        0, 100
                    ) { number ->
                        fractalParameters.offsetY = number
                        saveParameters()
                        onParameterChanged()
                    }
                )
            }
            if (fractalParameters.coloringType == FractalImageGenerator.FractalColoringType.LERP) {
                params.add(
                    CheckboxParameter(
                        "Случайный верхний цвет",
                        fractalParameters.isTopColorRandom
                    ) { isChecked ->
                        fractalParameters.isTopColorRandom = isChecked
                        updateParameters()
                        saveParameters()
                        onParameterChanged()
                    }
                )
                if (!fractalParameters.isTopColorRandom)
                    params.add(
                        ColorParameter(
                            "Верхний цвет",
                            fractalParameters.topColor,
                        ) { color ->
                            fractalParameters.topColor = color
                            saveParameters()
                            onParameterChanged()
                        }
                    )

                params.add(
                    CheckboxParameter(
                        "Случайный нижний цвет",
                        fractalParameters.isBottomColorRandom
                    ) { isChecked ->
                        fractalParameters.isBottomColorRandom = isChecked
                        updateParameters()
                        saveParameters()
                        onParameterChanged()
                    }
                )
                if (!fractalParameters.isBottomColorRandom) {
                    params.add(
                        ColorParameter(
                            "Нижний цвет",
                            fractalParameters.bottomColor,
                        ) { color ->
                            fractalParameters.bottomColor = color
                            saveParameters()
                            onParameterChanged()
                        }
                    )
                }
            }
        } else {
            throw NotImplementedError()
        }

        return params
    }
}