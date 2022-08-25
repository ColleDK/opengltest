package com.colledk.opengltest.listeners

import android.view.ScaleGestureDetector

class MyScaleListener(val onScale: (scaleFactor: Float) -> Unit): ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private var mScaleFactor = 1f

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        mScaleFactor *= detector.scaleFactor

        // Constrain the scale to not be too large or small
        mScaleFactor = mScaleFactor.coerceIn(0.1f, 2.0f)

        onScale(mScaleFactor)
        return super.onScale(detector)
    }
}