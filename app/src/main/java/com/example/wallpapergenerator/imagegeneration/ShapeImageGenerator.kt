package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import android.graphics.Point
import com.example.wallpapergenerator.parameterholders.ShapeParameters
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ShapeImageGenerator : ImageGenerator() {
    enum class ShapeType {
        TRIANGLE,
        RECTANGLE,
        CIRCLE,
        LINE,
        PLANE
    }

    companion object {
        val ShapeTypeNames = arrayOf(
            "Треугольник",
            "Прямоугольник",
            "Круг",
            "Линия",
            "Плоскость"
        )

        fun generateImage(xSize: Int, ySize: Int, parameters: ShapeParameters) : IntArray {
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
    }

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
        fun getDistanceToBorderLimitless(point: Point): Float {
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
            return min(
                min(abs(rotatedPoint.x - (pos.x - width/2)), abs(rotatedPoint.x - (pos.x + width/2))),
                min(abs(rotatedPoint.y - (pos.y - height/2)), abs(rotatedPoint.y - (pos.y + height/2)))
            )
        }
        override fun getDistanceToBorder(point: Point): Float {
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
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
            return SupportMath.getPointDistance(point, pos) - radius
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
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
            return rotatedPoint.y - (pos.y + size*(abs(pos.x - rotatedPoint.x) /(size/2)) - size/2)
        }

        override fun getDistanceToBorder(point: Point): Float {
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
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
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
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
            val rotatedPoint = SupportMath.rotateAround(point, pos, -angle)
            return (rotatedPoint.y-pos.y) * side.toFloat()
        }
    }
}