package com.ansgar.memorymanager

import java.io.BufferedReader
import java.io.InputStreamReader

internal class MemoryManagerUtil {

    private val fpsMap = ArrayList<Long>()
    private var lastNanoTime = 0L
    private var fps = 0
    private var frames = 0
    private var unprocessedSeconds: Double = 0.0
    private var secondsPerTick: Double = 1 / 60.0
    private var tickCount = 0

    fun getAppMemoryUsage(): AppMemoryModel {
        val runtime = Runtime.getRuntime()
        val useMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        val maxHeapSizeMb = runtime.maxMemory() / 1048576L
        val remainingHeapSizeMb = maxHeapSizeMb - useMemoryMb

        return AppMemoryModel(useMemoryMb, remainingHeapSizeMb, maxHeapSizeMb)
    }

    fun getCpuAppUsage(): String {
            var cpu = 0f
            val pid = android.os.Process.myPid()

            return try {
                val process = Runtime.getRuntime().exec("top -n 1 -d 1")
                val br = BufferedReader(InputStreamReader(process.inputStream))
                var i = 0
                var cpuColIndex = -1

                while (br.readLine() != null) {
                    val line = br.readLine().split(" ")
                    val notNullLine = line.filter { !it.isEmpty() }
                    notNullLine.forEachIndexed { index, item ->
                        if (cpuColIndex > -1) {
                            if (isNumeric(item) || isCpuValue(item)) {
                                cpu += notNullLine[cpuColIndex].replace("%", "")
                                        .replace(",", "")
                                        .toFloat()
                            }
                        } else {
                            if (isCpuValue(item)) {
                                cpu += item.replace("%", "").toFloat()
                            }
                        }

                        if (isColCpuValue(item)) {
                            cpuColIndex = index
                        }

                    }
                    i++
                    if (i == 10) break
                }
                "Total CPU: $cpu%"
            } catch (e: InterruptedException) {
                e.printStackTrace()
                "Not available"
            }
    }

    fun getFps(nanoTime: Long, maxHeapSize: Int): Int {
        val passedTime = nanoTime - lastNanoTime
        lastNanoTime = nanoTime
        unprocessedSeconds += passedTime / 1000000000.0

        while (unprocessedSeconds > secondsPerTick) {
            unprocessedSeconds -= secondsPerTick
            tickCount++

            if (tickCount % 60 == 0) {
                fps = frames
                lastNanoTime += 1000
                frames = 0
                tickCount = 0
            }
        }
        frames++

        return fps
    }

    private fun cleanFpsHeap(maxHeapSize: Int) {
        if (fpsMap.size > maxHeapSize) {
            fpsMap.removeAt(0)
        }
    }

    private fun isColCpuValue(value: String): Boolean {
        val lowerVal = value.toLowerCase()
        return lowerVal.contains("cpu%") || lowerVal.contains("0%sys")
    }

    private fun isCpuValue(value: String): Boolean = value.matches("-?\\d+(\\.\\d+)?%".toRegex())

    private fun isNumeric(value: String): Boolean = value.matches("-?\\d+(\\.\\d+)".toRegex())

}