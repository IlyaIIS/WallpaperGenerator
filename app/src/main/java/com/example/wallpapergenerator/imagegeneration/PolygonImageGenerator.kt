package com.example.wallpapergenerator.imagegeneration

import com.example.wallpapergenerator.SupportTools.Companion.pmap
import com.example.wallpapergenerator.parameterholders.PolygonParameters
import com.google.android.material.math.MathUtils
import kotlin.math.abs
import kotlin.random.Random

class PolygonImageGenerator(val xSize: Int, val ySize: Int) {
    private val size = xSize*ySize

    fun generateImage(parameters: PolygonParameters): IntArray {
        val pixels = IntArray(size)

        generatePolygons(pixels, parameters)

/*        for (y in 0 until ySize) {
            for (x in 0 until xSize) {
                pixels[getI(x, y)] = Color.rgb(
                    (altitudes[getI(x, y)]*255).toInt(),
                    (altitudes[getI(x, y)]*255).toInt(),
                    ((1-altitudes[getI(x, y)])*255).toInt())
            }
        }*/

        return pixels
    }

    private fun generatePolygons(pixels: IntArray, parameters: PolygonParameters) {
        val topColor = if (parameters.isTopColorRandom) SupportMath.getRndColor() else parameters.topColor
        val bottomColor = if (parameters.isBottomColorRandom) SupportMath.getRndColor() else parameters.bottomColor
        val polygonCount = Random.nextInt(parameters.minPolygonCount, parameters.maxPolygonCount)
        val plates = MutableList(polygonCount) { Polygon(
            Random.nextInt(xSize).toFloat(),
            Random.nextInt(ySize).toFloat(),
            if (parameters.coloringType == ColoringType.RANDOM){
                SupportMath.getRndColor()
            }
            else {
                SupportMath.colorLerp(bottomColor, topColor, it.toFloat() / polygonCount)
            }
            )
        }
        (0 until ySize).toList().pmap { y ->
            for (x in 0 until xSize) {
                var minDist = Float.MAX_VALUE
                var nearPlate = Polygon(0f, 0f, 0)
                for (i in plates.indices) {
                    val dist = when (parameters.distancingType) {
                        DistancingType.EUCLIDIAN -> MathUtils.dist(x.toFloat(), y.toFloat(), plates[i].x, plates[i].y)
                        DistancingType.CHEBYSHEV -> abs(x - plates[i].x) + abs(y - plates[i].y)
                        else -> throw NotImplementedError()
                    }
                    if (dist < minDist) {
                        minDist = dist
                        nearPlate = plates[i]
                    }
                }
                pixels[getI(x, y)] = nearPlate.color
            }
        }
    }

    private data class Polygon(val x: Float, val y: Float, val color: Int)

    private fun generateNoise(altitudes: FloatArray) {
        for(i in altitudes.indices) {
            altitudes[i] = Random.nextFloat()
        }
    }

    private fun getI(x: Int, y: Int) : Int {
        return x+y*xSize
    }

    private data class Position(val x: Int, val y: Int, val xSize: Int) {
        val i = x+y*xSize
        val right = i + 1
        val top = i - xSize
        val left = i - 1
        val down = i + xSize
    }

    enum class ColoringType {
        RANDOM,
        RANDOM_GRADIENT,
    }
    enum class DistancingType {
        EUCLIDIAN,
        CHEBYSHEV,
    }
    companion object {
        val ColoringTypeNames = arrayOf(
            "Случайны",
            "Случайный градиент",
        )
        val DistancingTypeNames = arrayOf(
            "Евклидово",
            "Чебушево",
        )
    }
}