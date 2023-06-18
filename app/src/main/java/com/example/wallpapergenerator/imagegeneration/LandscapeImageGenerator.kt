package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import com.google.android.material.math.MathUtils
import kotlin.random.Random

class LandscapeImageGenerator : ImageGenerator() {
    companion object {
        fun generateImage(xSize: Int, ySize: Int) : IntArray {
            fun drawSky(pixels: IntArray) {
                val topSkyColor = Color.rgb(95, 220, 240)
                val downSkyColor = Color.rgb(15, 170, 200)

                for(y in 0 until ySize)
                    for(x in 0 until xSize) {
                        pixels[x+y*xSize] =
                            SupportMath.colorLerp(topSkyColor, downSkyColor, y / ySize.toFloat())
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
                        pixels[x+y*xSize] = SupportMath.getGrayColor(
                            MathUtils.lerp(
                                110f,
                                180f,
                                y / ySize.toFloat()
                            ).toInt()
                        )
                    }
                    for (y in alt2.toInt() until ySize) {
                        pixels[x+y*xSize] = SupportMath.getGrayColor(
                            MathUtils.lerp(
                                100f,
                                170f,
                                y / ySize.toFloat()
                            ).toInt()
                        )
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
                        pixels[x+y*xSize] = SupportMath.getGrayColor(
                            MathUtils.lerp(
                                120f,
                                140f,
                                y / ySize.toFloat()
                            ).toInt()
                        )
                    }
                    for (y in alt2.toInt() until ySize) {
                        pixels[x+y*xSize] = SupportMath.getGrayColor(
                            MathUtils.lerp(
                                105f,
                                125f,
                                y / ySize.toFloat()
                            ).toInt()
                        )
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
    }
}