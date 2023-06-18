package com.example.wallpapergenerator.parameterholders

import com.example.wallpapergenerator.adapters.generationsettingsadapter.CheckboxParameter
import com.example.wallpapergenerator.adapters.generationsettingsadapter.DropdownParameter
import com.example.wallpapergenerator.adapters.generationsettingsadapter.SettingsParameter
import com.example.wallpapergenerator.imagegeneration.GenerationType
import com.example.wallpapergenerator.imagegeneration.GenerationTypeNames

class GalleryParametersHolder : ParameterHolder() {
    var allGenerationTypes = true
    var currentGenerationType: GenerationType = 0.toEnum()

    var orderBy: GalleryOrderType = GalleryOrderType.TIME

    var isLikedOnly = false

    var onParameterChanged: () -> Unit = { }

    override fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter> {
        lateinit var params: List<SettingsParameter>
        params = mutableListOf<SettingsParameter>()

        params.add(
            CheckboxParameter(
                "Все типы",
                allGenerationTypes,
            ) { value ->
                if (allGenerationTypes != value) {
                    allGenerationTypes = value
                    updateParameters()
                    onParameterChanged()
                }
            }
        )

        if (!allGenerationTypes) {
            params.add(
                DropdownParameter(
                    "Тип генератора",
                    0,
                    GenerationTypeNames
                ) { optionNum ->
                    currentGenerationType = optionNum.toEnum()
                    onParameterChanged()
                }
            )
        }

        params.add(
            DropdownParameter(
                "Сортировка",
                orderBy.ordinal,
                GalleryOrderTypeNames
            ) {optionNum ->
                orderBy = optionNum.toEnum()
                onParameterChanged()
            }
        )

        params.add(
            CheckboxParameter(
                "Только лайкнутые",
                isLikedOnly,
            ) {value ->
                isLikedOnly = value
                onParameterChanged()
            }
        )

        return params
    }

    enum class GalleryOrderType {
        NONE,
        TIME,
        TIME_ASCENDING,
        LIKE,
        LIKE_ASCENDING
    }

    val GalleryOrderTypeNames = arrayOf(
        "Нет",
        "Время",
        "Время воз.",
        "Лайки",
        "Лайки воз."
    )
}