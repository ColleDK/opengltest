package com.colledk.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.colledk.opengltest.parser.ObjectParser
import timber.log.Timber

private const val TOUCH_SCALE_FACTOR = 1f

class MySurfaceView(context: Context): GLSurfaceView(context) {

    private val renderer: MyRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    init {

        setEGLContextClientVersion(2)

        renderer = MyRenderer()

        renderer.applicationContext = context.applicationContext

        setRenderer(renderer)

        val data = ObjectParser.parseFile(context.resources.openRawResource(R.raw.cube))

        renderer.data = data

        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x: Float = event.x
        val y: Float = event.y

        when(event.action){
            MotionEvent.ACTION_MOVE -> {
                var dx: Float = x - previousX
                var dy: Float = y - previousY

                if (dx < 0){
                    dx * -1
                }
                if (dy < 0){
                    dy * -1
                }

                renderer.angle += (dy + dx) * TOUCH_SCALE_FACTOR
                requestRender()
            }
        }

        previousX = x
        previousY = y

        return true
    }
}