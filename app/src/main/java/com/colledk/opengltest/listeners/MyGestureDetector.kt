package com.colledk.opengltest.listeners

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MyGestureDetector(
    context: Context,
    listener: Listener
){
    data class Listener(
        val onScale: (scaleFactor: Float) -> Unit
    )

    private val gestureScaleDetector = ScaleGestureDetector(
        context,
        MyScaleListener { listener.onScale(it) }
    )

    fun handleTouchEvent(event: MotionEvent): Boolean{
        val scaleEventResult = gestureScaleDetector.onTouchEvent(event)
        return when (scaleEventResult == gestureScaleDetector.isInProgress) {
            true -> true
            else -> gestureScaleDetector.onTouchEvent(event)
        }
    }
}
