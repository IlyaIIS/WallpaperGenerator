package com.example.wallpapergenerator.imagegeneration

sealed class ImageGenerator {

}

enum class GenerationType {
    GRADIENTS,
    SHAPES,
    INTERFERENCE,
    FRACTALS,
    //LANDSCAPES
}

val GenerationTypeNames = arrayOf(
    "Градиент",
    "Фигуры",
    "Интерференция",
    "Фракталы",
    //"Пейзажи"
)

//todo: пейзажи, сглаженный шум, фракталы, подборка комплементарных цветов