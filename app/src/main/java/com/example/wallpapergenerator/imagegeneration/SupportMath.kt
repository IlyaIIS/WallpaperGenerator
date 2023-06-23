package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import android.graphics.Point
import com.google.android.material.math.MathUtils
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class SupportMath {
    companion object {
        fun colorLerp(fromColor: Int, toColor: Int, k: Float): Int {
            return Color.rgb(
                MathUtils.lerp(
                    Color.red(fromColor).toFloat(), Color.red(toColor).toFloat(), k
                ).toInt(),
                MathUtils.lerp(
                    Color.green(fromColor).toFloat(), Color.green(toColor).toFloat(), k
                ).toInt(),
                MathUtils.lerp(
                    Color.blue(fromColor).toFloat(), Color.blue(toColor).toFloat(), k
                ).toInt())
        }
        fun getGrayColor(lightness: Int) : Int {
            return Color.rgb(lightness, lightness, lightness)
        }

        fun getRndColor() : Int {
            return Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
        }

        fun rotateAround(point: Point, pivot: Point, angle: Float) : Point {
            val sin = sin(angle)
            val cos = cos(angle)
            val x = (pivot.x+(point.x-pivot.x) * cos - (point.y-pivot.y) * sin).toInt()
            val y = (pivot.y-(point.x-pivot.y) * sin - (point.y-pivot.y) * cos).toInt()
            return Point(x, y)
        }

        fun getPointDistance(point1: Point, point2: Point) : Float {
            return sqrt((point1.x - point2.x).toFloat().pow(2) + (point1.y - point2.y).toFloat().pow(2))
        }
    }
}