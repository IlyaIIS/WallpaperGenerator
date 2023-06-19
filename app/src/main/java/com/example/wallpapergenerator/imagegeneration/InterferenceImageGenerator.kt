package com.example.wallpapergenerator.imagegeneration

import android.graphics.Color
import com.example.wallpapergenerator.SupportTools.Companion.pmap
import com.example.wallpapergenerator.parameterholders.InterferenceParameters
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class InterferenceImageGenerator : ImageGenerator() {
    companion object {
        fun generateImage(xSize: Int, ySize: Int, parameters: InterferenceParameters) : IntArray {
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
            (0 until ySize).toList().pmap { y ->
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

                    pixels[x+y*xSize] = SupportMath.colorLerp(
                        fromColor,
                        toColor,
                        alt
                    )//Color.argb(255, alt.toInt(), alt.toInt(), alt.toInt())
                }
            }
            return pixels;
        }
    }
}