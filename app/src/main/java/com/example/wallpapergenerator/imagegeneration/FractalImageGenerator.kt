package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import android.graphics.Point
import com.example.wallpapergenerator.SupportTools.Companion.pmap
import com.example.wallpapergenerator.parameterholders.FractalParameters
import kotlin.math.sin
import kotlin.random.Random

class FractalImageGenerator : ImageGenerator() {
    enum class FractalType {
        JuliaSet
    }
    enum class FractalColoringType() {
        MODULE,
        SIN,
        LERP
    }

    companion object {
        val fractalTypeNames = arrayOf(
            "Множество Жюлиа",
        )
        val fractalColoringTypeNames = arrayOf(
            "Модуль",
            "Синус",
            "Смешивание"
        )

        fun generateImage(xSize: Int, ySize: Int, parameters: FractalParameters) : IntArray {
            val maxCount = parameters.depth

            fun juliaSetFormula(xx: Float, yy: Float, cx: Float, cy: Float, zoom: Float) : Int {
                val x = xx/zoom+(xSize-(xSize/zoom))/2
                val y = yy/zoom+(ySize-(ySize/zoom))/2
                var z = floatArrayOf((2*y-ySize)/xSize*1.5f, (2*x-xSize)/xSize*1.5f)
                var i = 0
                while ((z[0]*z[0] + z[1]*z[1] < 4) && (i < maxCount)) {
                    z = floatArrayOf(
                        z[0] * z[0] - z[1] * z[1] + cy/ySize,
                        2 * z[0] * z[1] + cx/xSize
                    )
                    i++
                }
                return i
            }

            val pixels = IntArray(xSize*ySize)

            val offset = if (parameters.isOffsetRandom)
                Point(Random.nextInt(xSize)*2-xSize, Random.nextInt(ySize)*2-ySize)
            else
                Point(((parameters.offsetX/100f)*2*xSize).toInt()-xSize, ((parameters.offsetY/100f)*2*ySize).toInt()-ySize)
            val fromColor =
                if (parameters.isBottomColorRandom)
                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                else
                    parameters.bottomColor
            val toColor =
                if (parameters.isTopColorRandom)
                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                else
                    parameters.topColor
            val zoom = 1 + if (parameters.isZoomRandom) Random.nextFloat() else parameters.zoom/100f
            val colKofs = IntArray(3) { Random.nextInt(20) }

            (0 until ySize).toList().pmap { y ->
                for (x in 0 until xSize) {
                    val count = juliaSetFormula(x.toFloat(), y.toFloat(), offset.x.toFloat(), offset.y.toFloat(), zoom)
                    pixels[x + y * xSize] = when (parameters.coloringType) {
                        FractalColoringType.MODULE -> Color.rgb(
                            (count * colKofs[0] % 255),
                            (count * colKofs[1] % 255),
                            (count * colKofs[2] % 255)
                        )
                        FractalColoringType.SIN -> Color.rgb(
                            (sin(count / colKofs[0].toFloat()) * 255).toInt(),
                            (sin(count / colKofs[1].toFloat()) * 255).toInt(),
                            (sin(count / colKofs[2].toFloat()) * 255).toInt()
                        )
                        FractalColoringType.LERP -> SupportMath.colorLerp(
                            fromColor,
                            toColor,
                            count / maxCount.toFloat()
                        )
                        else -> throw NotImplementedError()
                    }
                }
            }

            return pixels
        }
    }
}