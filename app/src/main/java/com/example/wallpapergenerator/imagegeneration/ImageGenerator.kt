package com.example.wallpapergenerator.imagegeneration

sealed class ImageGenerator {

}

enum class GenerationType {
    GRADIENTS,
    SHAPES,
    INTERFERENCE,
    FRACTALS,
    //NOISE,
    POLYGONS,
    //LANDSCAPES
}

val GenerationTypeNames = arrayOf(
    "Градиент",
    "Фигуры",
    "Интерференция",
    "Фракталы",
    //"Шум",
    "Полигоны"
    //"Пейзажи"
)

//todo: пейзажи, сглаженный шум, фракталы, подборка комплементарных цветов