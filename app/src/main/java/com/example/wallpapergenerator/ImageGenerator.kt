package com.example.wallpapergenerator

import android.graphics.Color
import android.graphics.Point
import androidx.core.math.MathUtils
import com.google.android.material.math.MathUtils.lerp
import com.example.wallpapergenerator.ImageGenerator.Companion.SupMath.Companion.colorLerp
import com.example.wallpapergenerator.ImageGenerator.Companion.SupMath.Companion.getGrayColor
import com.example.wallpapergenerator.ImageGenerator.Companion.SupMath.Companion.getPointDistance
import com.example.wallpapergenerator.ImageGenerator.Companion.SupMath.Companion.rotateAround
import com.fasterxml.jackson.annotation.JsonFormat
import com.google.gson.annotations.SerializedName
import kotlin.math.*
import kotlin.random.Random

class ImageGenerator {
    companion object {
        fun generatePalette(xSize: Int, ySize: Int) : IntArray {
            val pixels = IntArray(xSize*ySize)
            for (y in 0 until ySize) {
                for (x in 0 until xSize) {
                    var xx = x+ sin(y/20f) *100 //эффект волны
                    var alt = xx/xSize.toFloat() //(0;1)
                    alt *= 6 //(0;6)

                    var r = 0
                    var g = 0
                    var b = 0
                    val count = 1
                    if (y/(ySize/count) == 0) {
                        r = (MathUtils.clamp(abs(alt - 3) - 1, 0f, 1f) * 255).toInt()
                        g = (MathUtils.clamp(-abs(alt - 2) + 2, 0f, 1f) * 255).toInt()
                        b = (MathUtils.clamp(-abs(alt - 4) + 2, 0f, 1f) * 255).toInt()
                    } /*else if (y/(ySize/count) == 1){
                    r = (clamp(abs(alt - 3) - 1, 0f, 1f) * 255).toInt()
                    g = (clamp(-abs(alt - 3) + 2, 0f, 1f) * 255).toInt()
                    b = (clamp(-abs(alt - 3) + 2, 0f, 1f) * 255).toInt()
                } else if (y/(ySize/count) == 2){
                    r = (clamp(abs(alt - 3) - 1, 0f, 1f) * 255).toInt()
                    g = (clamp(-abs(alt - 4) + 2, 0f, 1f) * 255).toInt()
                    b = (clamp(-abs(alt - 3) + 2, 0f, 1f) * 255).toInt()
                } else if (y/(ySize/count) == 3){
                    r = (clamp(abs(alt - 3) - 1, 0f, 1f) * 255).toInt()
                    g = (clamp(-abs(alt - 3) + 2, 0f, 1f) * 255).toInt()
                    b = (clamp(-abs(alt - 2) + 2, 0f, 1f) * 255).toInt()
                } else if (y/(ySize/count) == 4){
                    r = (clamp(abs(alt - 3) - 1, 0f, 1f) * 255).toInt()
                    g = (clamp(-abs(alt - 4) + 2, 0f, 1f) * 255).toInt()
                    b = (clamp(-abs(alt - 2) + 2, 0f, 1f) * 255).toInt()
                }*/

                    pixels[x+y*xSize] = Color.rgb(r, g, b)//Color.argb(255, alt.toInt(), alt.toInt(), alt.toInt())
                }
            }
            return pixels;
        }

        fun generateGradient(xSize: Int, ySize: Int, parameters: GenerationActivity.GradientParameters) : IntArray {
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
                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        val alt = lerp(
                            x / xSize.toFloat(),
                            y / ySize.toFloat(),
                            k
                        )
                        val subAlt = altMod(alt, colors.size)
                        val i = (alt*(colors.size-1)).toInt()
                        val fromColor = colors[i]
                        val toColor = colors[i+1]
                        pixels[x+y*xSize] = colorLerp(fromColor, toColor, subAlt)
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
                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        val alt = getPointDistance(Point(x, y), pos)/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
                        val i = (alt*(colors.size-1)).toInt()
                        val subAlt = (sin(( (alt*(colors.size-1)%1f) -0.5f)* PI.toFloat()) +1)/2
                        val fromColor = colors[i]
                        val toColor = colors[i+1]
                        pixels[x+y*xSize] = colorLerp(fromColor, toColor, subAlt)
                    }
            }
            fun drawStrictCircleGradient(pixels: IntArray) {
                val levelCount = Random.nextInt(10,100)
                val pos = Point(Random.nextInt(xSize), Random.nextInt(ySize))
                val colors = getColorArray()
                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        val alt = (getPointDistance(Point(x, y), pos)/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f)) *levelCount).toInt().toFloat()/levelCount
                        val i = (alt*(colors.size-1)).toInt()
                        val subAlt = (sin(( (alt*(colors.size-1)%1f) -0.5f)* PI.toFloat()) +1)/2
                        val fromColor = colors[i]
                        val toColor = colors[i+1]
                        pixels[x+y*xSize] = colorLerp(fromColor, toColor, subAlt)
                    }
            }
            fun drawStrictPointsGradient(pixels: IntArray) {
                val points = Array(arraySize) {
                    Point(Random.nextInt(xSize), Random.nextInt(ySize))
                }
                val colors = getColorArray()
                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        var r = 0f
                        var g = 0f
                        var b = 0f
                        for(i in points.indices) {
                            val alt = getPointDistance(Point(x, y), points[i])/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
                            r += Color.red(colors[i])*alt
                            g += Color.green(colors[i])*alt
                            b += Color.blue(colors[i])*alt
                        }

                        pixels[x+y*xSize] = Color.rgb((r).toInt(), (g).toInt(), (b).toInt())
                    }
            }
            fun drawPointsGradient(pixels: IntArray) {
                val points = Array(arraySize) {
                    Point(Random.nextInt(xSize), Random.nextInt(ySize))
                }
                val colors = getColorArray()
                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        var r = 0f
                        var g = 0f
                        var b = 0f
                        val dists = FloatArray(points.size) {0f}
                        var distSum = 0f
                        for(i in points.indices) {
                            dists[i] = getPointDistance(Point(x, y), points[i])/ sqrt(xSize.toFloat().pow(2) + ySize.toFloat().pow(2f))
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

        fun generateFractal(xSize: Int, ySize: Int, parameters: GenerationActivity.FractalParameters) : IntArray {
            val maxCount = parameters.depth

            fun formula(xx: Float, yy: Float, cx: Float, cy: Float, zoom: Float) : Int {
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

            for(y in 0 until ySize)
                for(x in 0 until xSize) {
                    val count = formula(x.toFloat(), y.toFloat(), offset.x.toFloat(), offset.y.toFloat(), zoom)
                    pixels[x+y*xSize] = when (parameters.coloringType) {
                        FractalColoringType.MODULE -> Color.rgb(
                            (count*colKofs[0]%255),
                            (count*colKofs[1]%255),
                            (count*colKofs[2]%255)
                        )
                        FractalColoringType.SIN -> Color.rgb(
                            (sin(count/colKofs[0].toFloat())*255).toInt(),
                            (sin(count/colKofs[1].toFloat())*255).toInt(),
                            (sin(count/colKofs[2].toFloat())*255).toInt()
                        )
                        FractalColoringType.LERP -> colorLerp(fromColor, toColor, count/maxCount.toFloat())
                        else -> throw NotImplementedError()
                    }
                }

            return pixels
        }

        enum class FractalType {
            JuliaSet
        }

        val fractalTypeNames = arrayOf(
            "Множество Жюлиа",
        )

        enum class FractalColoringType() {
            MODULE,
            SIN,
            LERP
        }

        val fractalColoringTypeNames = arrayOf(
            "Модуль",
            "Синус",
            "Смешивание"
        )

        fun generateSinNoise(xSize: Int, ySize: Int, parameters: GenerationActivity.NoiseParameters) : IntArray {
            val seed1 = Array(20) { Random.nextFloat()};

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

            val levelCount = if (parameters.isLevelCountRandom) Random.nextInt(3,10).toFloat() else parameters.levelCount

            val pixels = IntArray(xSize*ySize)
            for (y in 0 until ySize) {
                for (x in 0 until xSize) {
                    var alt = 0f;
                    /*for (i in seed1.indices){
                        alt += (sin(x/4f * seed1[i] / (1-seed1[i]) + PI.toFloat()*2 / seed2[i])) *
                                (sin(y/4f * seed1[seed1.size-i-1] / (1-seed1[seed1.size-i-1]) + PI.toFloat()*2 / seed2[i]));
                    }*/
                    for (i in seed1.indices)
                        alt += (sin(x/10f*seed1[i])) *
                                (sin(y/10f*seed1[seed1.size-i-1]));

                    alt = (alt/seed1.size+1)/2; //(-1;1)->(0;1)
                    alt = min(1f, max(0f, (alt-0.4f)/3*10)) //(0.4;0.7)->(0;1)
                    alt = ((alt*levelCount.toFloat()).toInt()/levelCount.toFloat())

                    pixels[x+y*xSize] = colorLerp(fromColor, toColor, alt)//Color.argb(255, alt.toInt(), alt.toInt(), alt.toInt())
                }
            }
            return pixels;
        }

        fun generateLandscape(xSize: Int, ySize: Int) : IntArray {
            fun drawSky(pixels: IntArray) {
                val topSkyColor = Color.rgb(95, 220, 240)
                val downSkyColor = Color.rgb(15, 170, 200)

                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        pixels[x+y*xSize] = colorLerp(topSkyColor, downSkyColor, y/ySize.toFloat())
                    }
            }

            fun drawMountains2(pixels: IntArray) {
                val landAlt = ySize/6f*3.5f+ Random.nextInt(-ySize/10, ySize/10)
                var alt = landAlt
                var alt2 = landAlt*1.15;
                var altDir = (Random.nextFloat()*2-1)/20
                for (x in 0 until xSize) {
                    altDir += (Random.nextFloat()*2-1)/2
                    altDir += (landAlt-alt)/10000
                    altDir *= 0.97f
                    alt += altDir
                    alt2 += altDir/2;
                    for (y in alt.toInt() until ySize) {
                        pixels[x+y*xSize] = getGrayColor(
                            lerp(
                                110f,
                                180f,
                                y / ySize.toFloat()
                            ).toInt())
                    }
                    for (y in alt2.toInt() until ySize) {
                        pixels[x+y*xSize] = getGrayColor(
                            lerp(
                                100f,
                                170f,
                                y / ySize.toFloat()
                            ).toInt())
                    }
                }
            }

            fun drawMountains(pixels: IntArray) {
                val landAlt = ySize/6f*4+ Random.nextInt(-ySize/10, ySize/10)
                var alt = landAlt
                var alt2 = landAlt*1.15;
                var altDir = (Random.nextFloat()*2-1)/20
                for (x in 0 until xSize) {
                    altDir += (Random.nextFloat()*2-1)/2
                    altDir += (landAlt-alt)/10000
                    altDir *= 0.97f
                    alt += altDir
                    alt2 += altDir/2;
                    for (y in alt.toInt() until ySize) {
                        pixels[x+y*xSize] = getGrayColor(
                            lerp(
                                120f,
                                140f,
                                y / ySize.toFloat()
                            ).toInt())
                    }
                    for (y in alt2.toInt() until ySize) {
                        pixels[x+y*xSize] = getGrayColor(
                            lerp(
                                105f,
                                125f,
                                y / ySize.toFloat()
                            ).toInt())
                    }
                }
            }

            fun drawLand(pixels: IntArray) {
                val landAlt = ySize/4f*3
                var alt = landAlt+ Random.nextInt(-ySize/20, ySize/20)
                var altDir = (Random.nextFloat()*2-1)/20
                for (x in 0 until xSize) {
                    altDir += (Random.nextFloat()*2-1)/20
                    altDir += (landAlt-alt)/100000
                    altDir *= 0.98f
                    alt += altDir
                    for (y in alt.toInt() until ySize) {
                        pixels[x+y*xSize] = Color.rgb(80, 250, 130)
                    }
                }
            }

            val hsv = FloatArray(3) {0f}
            Color.RGBToHSV(10,101,10, hsv)

            val pixels = IntArray(xSize*ySize)

            drawSky(pixels)
            drawMountains2(pixels)
            drawMountains(pixels)
            drawLand(pixels)

            return pixels
        }

        fun generateShapes(xSize: Int, ySize: Int, parameters: GenerationActivity.ShapeParameters) : IntArray {
            val angles = Array(3) { Random.nextFloat()* PI.toFloat() }
            val backgroundColor =
                if (parameters.isBackgroundColorRandom)
                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                else
                    parameters.backgroundColor

            fun getRndShape(xBound: Int, yBound: Int, ableShapes: List<ShapeType>): IShape {
                val pos = Point(Random.nextInt(xBound), Random.nextInt(yBound))
                val angle = angles[Random.nextInt(angles.size)]
                val thickness =
                    if (Random.nextInt(100) + 1 <= parameters.borderChance)
                        Random.nextInt(3, 10).toFloat()
                    else
                        0f
                val color = Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                if (ableShapes.size > 0)
                    return when (ableShapes[Random.nextInt(ableShapes.size)]) {
                        ShapeType.RECTANGLE -> Rectangle(pos,
                            Random.nextInt(300, 1000).toFloat(),
                            Random.nextInt(300, 1000).toFloat(),
                            angle,
                            thickness,
                            color)
                        ShapeType.CIRCLE -> Circle(pos,
                            Random.nextInt(100, 500).toFloat(),
                            thickness,
                            color)
                        ShapeType.TRIANGLE -> Triangle(pos,
                            Random.nextInt(100, 500).toFloat(),
                            angle,
                            thickness,
                            color)
                        ShapeType.LINE -> Line(pos,
                            angle,
                            Random.nextInt(10, 50).toFloat(),
                            if (Random.nextInt(3) == 0) Random.nextInt(2, 5).toFloat() else 0f,
                            color)
                        ShapeType.PLANE -> Plane(pos,
                            angle,
                            Random.nextInt(-1,2),
                            thickness,
                            color)
                        else ->
                            throw NotImplementedError()
                    }
                else
                    return Line(pos, angle, 0f, 0f, Color.TRANSPARENT)
            }

            fun drawShapes(pixels: IntArray, shapes: Array<IShape>) {
                if (parameters.backgroundColor != Color.WHITE)
                    for(y in 0 until  ySize)
                        for(x in 0 until xSize)
                            pixels[x + y*xSize] = backgroundColor
                for(y in 0 until  ySize)
                    for(x in 0 until xSize)
                        for(shape in shapes) {
                            if (shape.contains(Point(x, y))) {
                                pixels[x + y*xSize] += shape.color
                            } else if (shape.thickness != 0f && abs(shape.getDistanceToBorder(Point(x, y))) <= shape.thickness) {
                                pixels[x + y*xSize] = Color.WHITE;
                            }
                        }
            }

            fun getAbleShapes() : List<ShapeType> {
                val ableShapes = mutableListOf<ShapeType>()
                if (parameters.ableCircles)
                    ableShapes.add(ShapeType.CIRCLE)
                if (parameters.ableRectangles)
                    ableShapes.add(ShapeType.RECTANGLE)
                if (parameters.ableTriangles)
                    ableShapes.add(ShapeType.TRIANGLE)
                if (parameters.ableLines)
                    ableShapes.add(ShapeType.LINE)
                if (parameters.ablePlanes)
                    ableShapes.add(ShapeType.PLANE)

                return ableShapes
            }

            val pixels = IntArray(xSize*ySize)
            val ableShapes = getAbleShapes()

            val shapes = Array(Random.nextInt(parameters.minShapeCount, parameters.maxShapeCount+1)) {
                getRndShape(xSize, ySize, ableShapes)
            }
            drawShapes(pixels, shapes)

            return pixels
        }

        interface IShape {
            var pos: Point
            var angle: Float
            var thickness: Float
            var color: Int
            fun contains(point: Point) : Boolean
            fun getDistanceToBorder(point: Point) : Float
        }

        enum class ShapeType {
            TRIANGLE,
            RECTANGLE,
            CIRCLE,
            LINE,
            PLANE
        }
        val ShapeTypeNames = arrayOf(
            "Треугольник",
            "Прямоугольник",
            "Круг",
            "Линия",
            "Плоскость"
        )
        class Rectangle(
            override var pos: Point,
            var width: Float,
            var height: Float,
            override var angle: Float,
            override var thickness: Float,
            override var color: Int
        ) : IShape {

            override fun contains(point: Point) : Boolean {
                return getDistanceToBorder(point) <= 0
            }
            //границы продолжаются после фигуры
            fun getDistanceToBorder2(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return min(
                    min(abs(rotatedPoint.x - (pos.x - width/2)), abs(rotatedPoint.x - (pos.x + width/2))),
                    min(abs(rotatedPoint.y - (pos.y - height/2)), abs(rotatedPoint.y - (pos.y + height/2)))
                )
            }
            override fun getDistanceToBorder(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return max(abs(rotatedPoint.x - pos.x) - width/2, abs(rotatedPoint.y - pos.y) - height/2)
            }
        }
        class Circle(
            override var pos: Point,
            var radius: Float,
            override var thickness: Float,
            override var color: Int
        ) : IShape {

            override var angle = 0f
            override fun contains(point: Point) : Boolean {
                return getDistanceToBorder(point) <= 0
            }

            override fun getDistanceToBorder(point: Point): Float {
                return getPointDistance(point, pos) - radius
            }
        }
        class Triangle(
            override var pos: Point,
            var size: Float,
            override var angle: Float,
            override var thickness: Float,
            override var color: Int
        ) : IShape {

            override fun contains(point: Point): Boolean {
                return getDistanceToBorder(point) <= 0
            }
            //без дна
            fun getDistanceToBorder2(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return rotatedPoint.y - (pos.y + size*(abs(pos.x - rotatedPoint.x) /(size/2)) - size/2)
            }

            override fun getDistanceToBorder(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return max((rotatedPoint.y - (pos.y+size/2))*2,
                    (pos.y - size/2 + size*(abs(pos.x - rotatedPoint.x) /(size/2))) - rotatedPoint.y)
            }
        }
        class Line(
            override var pos: Point,
            override var angle: Float,
            var size: Float,
            override var thickness: Float,
            override var color: Int
        ) : IShape {

            override fun contains(point: Point): Boolean {
                return getDistanceToBorder(point) <= 0
            }

            override fun getDistanceToBorder(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return abs(rotatedPoint.y - pos.y) - size/2
            }
        }
        class Plane(
            override var pos: Point,
            override var angle: Float,
            var side: Int,
            override var thickness: Float,
            override var color: Int
        ) : IShape {

            override fun contains(point: Point): Boolean {
                return getDistanceToBorder(point) <= 0
            }

            override fun getDistanceToBorder(point: Point): Float {
                val rotatedPoint = rotateAround(point, pos, -angle)
                return (rotatedPoint.y-pos.y) * side.toFloat()
            }
        }

        class SupMath {
            companion object {
                fun colorLerp(fromColor: Int, toColor: Int, k: Float): Int {
                    return Color.rgb(
                        lerp(
                            Color.red(fromColor).toFloat(), Color.red(toColor).toFloat(), k
                        ).toInt(),
                        lerp(
                            Color.green(fromColor).toFloat(), Color.green(toColor).toFloat(), k
                        ).toInt(),
                        lerp(
                            Color.blue(fromColor).toFloat(), Color.blue(toColor).toFloat(), k
                        ).toInt())
                }
                fun getGrayColor(lightness: Int): Int {
                    return Color.rgb(lightness, lightness, lightness)
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
    }
}

enum class GenerationType {
    Gradients,
    Shapes,
    Noise,
    Fractals,
    Landscapes
}

val GenerationTypeNames = arrayOf(
    "Градиент",
    "Фигуры",
    "Шум",
    "Фракталы",
    "Пейзажи"
)

/*
Идеи:
Градиенты:
    Линейный
    Радиальный
    Дискретный
Фигуры:
    Черный фон, фигуры rgb с эффектом наложения
    Вывернутые фигуры
    Фигуры без границ
Синусоидный шум:
    Выбор цвета откуда до куда
Линии в разные стороны

комплиментарные цвета
Фрактал
*/