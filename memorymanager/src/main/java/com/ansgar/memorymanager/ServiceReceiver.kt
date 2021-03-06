package com.ansgar.memorymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Choreographer

internal class ServiceReceiver : BroadcastReceiver() {

    private var memoryUtil: MemoryManagerUtil? = null
    private var frameCallback: Choreographer.FrameCallback? = null
    private var isFirstFrame = true

    override fun onReceive(ctx: Context?, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            frameCallback = Choreographer.FrameCallback { nanos ->
                OverlayView.initOverlayView(getString(nanos))
                if (!isFirstFrame) {
                    Choreographer.getInstance().postFrameCallback(frameCallback)
                }
            }

            if (isFirstFrame) {
                Handler(Looper.getMainLooper()).post {
                    Choreographer.getInstance().postFrameCallback(frameCallback)
                }
                isFirstFrame = false
                return
            }
        }
    }

    private fun getString(nanos: Long): String {
        if (memoryUtil == null) memoryUtil = MemoryManagerUtil()
        val stringBuilder = StringBuilder()
        val model = memoryUtil?.getAppMemoryUsage()
        return stringBuilder.append(model?.toString())
                .append(" Mb")
                .append("\n")
                .append(memoryUtil?.getFps(nanos, 20))
                .append(" fps")
                .toString()
    }

}