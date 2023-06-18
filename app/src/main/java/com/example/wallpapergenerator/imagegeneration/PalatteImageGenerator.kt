package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import androidx.core.math.MathUtils
import kotlin.math.abs
import kotlin.math.sin

class PalatteImageGenerator : ImageGenerator() {
    companion object {
        fun generateImage(xSize: Int, ySize: Int) : IntArray {
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
    }
}