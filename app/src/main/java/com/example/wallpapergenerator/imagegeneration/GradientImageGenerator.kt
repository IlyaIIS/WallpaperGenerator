package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import android.graphics.Point
import com.example.wallpapergenerator.SupportTools.Companion.pmap
import com.example.wallpapergenerator.imagegeneration.SupportMath
import com.example.wallpapergenerator.parameterholders.GradientParameters
import com.google.android.material.math.MathUtils
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GradientImageGenerator : ImageGenerator() {

    enum class GradientType {
        StrictLine,
        DuplicateLine,
        SplittedLine,
        Line,
        Circle,
        StrictCircle,
        Points,
        StrictPoints,
    }

    companion object {
        val gradientTypeNames = arrayOf(
            "Четкие линии",
            "Дублирующиеся линии",
            "Разделённые линии",
            "Линии",
            "Круг",
            "Четкий круг",
            "Точки",
            "Четкие точки",
        )

        fun generateImage(xSize: Int, ySize: Int, parameters: GradientParameters) : IntArray {
            val arraySize =
                if (parameters.isColorsRandom)
                    Random.nextInt(parameters.minColorsCount, parameters.maxColorsCount + 1)
                else
                    parameters.colorsCount

            fun getColorArray() : List<Int> {
                return if (parameters.isColorsRandom)
                    IntArray(arraySize) {
                        Color.rgb(
                            Random.nextInt(255),
                            Random.nextInt(255),
                            Random.nextInt(255))
                    }.toList()
                else
                    parameters.colors
            }

            fun drawGradient(pixels: IntArray, altMod: (alt: Float, size: Int) -> Float ) {
                val k = Random.nextFloat()
                val colors = getColorArray()
                (0 until ySize).toList().pmap { y ->
                    for (x in 0 until xSize) {
                        val alt = MathUtils.lerp(
                            x / xSize.toFloat(),
                            y / ySize.toFloat(),
                            k
                        )
                        val subAlt = altMod(alt, colors.size)
                        val i = (alt * (colors.size - 1)).toInt()
                        val fromColor = colors[i]
                        val toColor = colors[i + 1]
                        pixels[x + y * xSize] = SupportMath.colorLerp(fromColor, toColor, subAlt)
                    }
                }
            }
            fun drawStrictLineGradient(pixels: IntArray) {
                drawGradient(pixels) { alt, _ -> alt }
            }
            fun drawDuplicateLineGradient(pixels: IntArray) {
                drawGradient(pixels) { alt, size -> alt % (1f / size) }
            }
            fun drawSplittedLineGradient(pixels: IntArray) {
                drawGradient(pixels) { alt, size -> (alt%(1f/size))*size }
            }
            fun drawLineGradient(pixels: IntArray) {
                drawGradient(pixels) { alt, size ->
                    (sin(((alt*(size-1)%1f) - 0.5f) * PI.toFloat()) + 1) / 2
                }
            }
            fun drawCircleGradient(pixels: IntArray) {
                val pos = Point(Random.nextInt(xSize), Random.nextInt(ySize))
                val colors = getColorArray()
                (0 until ySize).toList().pmap { y ->
                    for(x in 0 until xSize) {
                        val alt = SupportMath.getPointDistance(Point(x, y), pos)/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
                        val i = (alt*(colors.size-1)).toInt()
                        val subAlt = (sin(( (alt*(colors.size-1)%1f) -0.5f)* PI.toFloat()) +1)/2
                        val fromColor = colors[i]
                        val toColor = colors[i+1]
                        pixels[x+y*xSize] = SupportMath.colorLerp(fromColor, toColor, subAlt)
                    }
                }
            }
            fun drawStrictCircleGradient(pixels: IntArray) {
                val levelCount = Random.nextInt(10,100)
                val pos = Point(Random.nextInt(xSize), Random.nextInt(ySize))
                val colors = getColorArray()
                (0 until ySize).toList().pmap { y ->
                    for(x in 0 until xSize) {
                        val alt = (SupportMath.getPointDistance(Point(x, y), pos)/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f)) *levelCount).toInt().toFloat()/levelCount
                        val i = (alt*(colors.size-1)).toInt()
                        val subAlt = (sin(( (alt*(colors.size-1)%1f) -0.5f)* PI.toFloat()) +1)/2
                        val fromColor = colors[i]
                        val toColor = colors[i+1]
                        pixels[x+y*xSize] = SupportMath.colorLerp(fromColor, toColor, subAlt)
                    }
                }
            }
            fun drawStrictPointsGradient(pixels: IntArray) {
                val points = Array(arraySize) {
                    Point(Random.nextInt(xSize), Random.nextInt(ySize))
                }
                val colors = getColorArray()
                (0 until ySize).toList().pmap { y ->
                    for(x in 0 until xSize) {
                        var r = 0f
                        var g = 0f
                        var b = 0f
                        for(i in points.indices) {
                            val alt = SupportMath.getPointDistance(Point(x, y), points[i])/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
                            r += Color.red(colors[i])*alt
                            g += Color.green(colors[i])*alt
                            b += Color.blue(colors[i])*alt
                        }

                        pixels[x+y*xSize] = Color.rgb((r).toInt(), (g).toInt(), (b).toInt())
                    }
                }
            }
            fun drawPointsGradient(pixels: IntArray) {
                val points = Array(arraySize) {
                    Point(Random.nextInt(xSize), Random.nextInt(ySize))
                }
                val colors = getColorArray()
                (0 until ySize).toList().pmap { y ->
                    for(x in 0 until xSize) {
                        var r = 0f
                        var g = 0f
                        var b = 0f
                        val dists = FloatArray(points.size) {0f}
                        var distSum = 0f
                        for(i in points.indices) {
                            dists[i] = SupportMath.getPointDistance(Point(x, y), points[i])/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
                            distSum += dists[i]
                        }
                        for(i in points.indices) {
                            val alt = (sin(( dists[i]/distSum -0.5f)* PI.toFloat()*0.7f) +1)/2
                            r += Color.red(colors[i])*alt
                            g += Color.green(colors[i])*alt
                            b += Color.blue(colors[i])*alt
                        }

                        pixels[x+y*xSize] = Color.rgb((r).toInt(), (g).toInt(), (b).toInt())
                    }
                }
            }

            val pixels = IntArray(xSize*ySize)

            when (parameters.gradientType) {
                GradientType.StrictLine -> drawStrictLineGradient(pixels)
                GradientType.DuplicateLine -> drawDuplicateLineGradient(pixels)
                GradientType.SplittedLine -> drawSplittedLineGradient(pixels)
                GradientType.Line -> drawLineGradient(pixels)
                GradientType.Circle -> drawCircleGradient(pixels)
                GradientType.StrictCircle -> drawStrictCircleGradient(pixels)
                GradientType.StrictPoints -> drawStrictPointsGradient(pixels)
                GradientType.Points -> drawPointsGradient(pixels)
                else -> throw NotImplementedError()
            }

            return pixels
        }
    }
}