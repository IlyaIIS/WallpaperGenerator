package com.example.wallpapergenerator

class SupportTools {
    companion object {
        inline fun <reified T : Enum<T>> Int.toEnum(): T {
            return enumValues<T>().first { it.ordinal == this }
        }

        inline fun <reified T : Enum<T>> T.toInt(): Int {
            return this.ordinal
        }
    }
}