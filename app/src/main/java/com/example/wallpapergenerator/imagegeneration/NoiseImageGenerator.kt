package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import android.graphics.Point
import com.example.wallpapergenerator.SupportTools.Companion.pmap
import com.google.android.material.math.MathUtils
import kotlin.random.Random

class NoiseImageGenerator(val xSize: Int, val ySize: Int) {
    private val size = xSize*ySize

    fun generateImage() : IntArray {
        val pixels = IntArray(size)
        var altitudes = FloatArray(size)

        //generateNoise(altitudes)

        generatePlates(altitudes)

        transferAltitudes(altitudes)

        //altitudes = getSmoothNoise(altitudes, 10)

        for (y in 0 until ySize) {
            for (x in 0 until xSize) {
                pixels[getI(x, y)] = Color.rgb(
                    (altitudes[getI(x, y)]*255).toInt(),
                    (altitudes[getI(x, y)]*255).toInt(),
                    ((1-altitudes[getI(x, y)])*255).toInt())
            }
        }

        return pixels
    }

    private fun transferAltitudes(altitudes: FloatArray) {
        for (i in 0 until 10) {
            (1 until ySize - 1).toList().pmap { y ->
                for (x in 1 until xSize - 1) {
                    if (Random.nextBoolean()) {
                        altitudes[getI(x, y)] = altitudes[getI(x + Random.nextInt(2) - 1, y + Random.nextInt(2) - 1)]
                    }
                }
            }
        }
    }

    private fun generatePlates(altitudes: FloatArray) {
        val plates = MutableList(10) { Triple(Random.nextInt(xSize), Random.nextInt(ySize), Random.nextFloat()-0.5f) }
        (0 until ySize).toList().pmap { y ->
            for (x in 0 until xSize) {
                var minDist = Float.MAX_VALUE
                var nearPlate = Triple(0, 0, 0f)
                for (i in plates.indices) {
                    val dist = MathUtils.dist(x.toFloat(), y.toFloat(), plates[i].first.toFloat(), plates[i].second.toFloat())
                    if (dist < minDist) {
                        minDist = dist
                        nearPlate = plates[i]
                    }
                }
                altitudes[getI(x, y)] += nearPlate.third
            }
        }
    }

    private fun getSmoothNoise(altitudes: FloatArray, number: Int): FloatArray {
        var resultAltitudes = altitudes

        for (i in 0 until number) {
            val newAltitudes = FloatArray(size)
            (1 until ySize - 1).toList().pmap { y ->
                for (x in 1 until xSize-1) {
                    val pos = Position(x, y, xSize)
                    newAltitudes[pos.i] += resultAltitudes[pos.right]
                    newAltitudes[pos.i] += resultAltitudes[pos.top]
                    newAltitudes[pos.i] += resultAltitudes[pos.left]
                    newAltitudes[pos.i] += resultAltitudes[pos.down]
                    newAltitudes[pos.i] += resultAltitudes[pos.i]
                    newAltitudes[pos.i] = newAltitudes[pos.i] / 5
                }
            }
            resultAltitudes = newAltitudes
        }

        return resultAltitudes
    }

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
}