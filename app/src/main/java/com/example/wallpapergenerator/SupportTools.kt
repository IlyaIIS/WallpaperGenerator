package com.example.wallpapergenerator

import java.util.concurrent.Executors

class SupportTools {
    companion object {
        inline fun <reified T : Enum<T>> Int.toEnum(): T {
            return enumValues<T>().first { it.ordinal == this }
        }

        inline fun <reified T : Enum<T>> T.toInt(): Int {
            return this.ordinal
        }

        fun <T, R> Iterable<T>.pmap(numThreads: Int = Runtime.getRuntime().availableProcessors(), transform: (T) -> R): List<R> {
            val executor = Executors.newFixedThreadPool(numThreads)
            val result = mutableListOf<R>()
            val tasks = mutableListOf<Runnable>()
            for (item in this) {
                val task = kotlinx.coroutines.Runnable {
                    val res = transform(item)
                    synchronized(result) { result.add(res) }
                }
                tasks.add(task)
            }
            tasks.forEach { task -> executor.execute(task) }
            executor.shutdown()
            while (!executor.isTerminated);
            return result
        }
    }
}