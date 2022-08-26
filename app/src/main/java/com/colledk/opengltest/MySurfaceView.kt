package com.colledk.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.colledk.opengltest.listeners.MyGestureDetector
import com.colledk.opengltest.parser.ObjectParser
import timber.log.Timber

private const val TOUCH_SCALE_FACTOR = 1f

class MySurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var gestureHandler: MyGestureDetector

    init {

        setEGLContextClientVersion(2)

        renderer = MyRenderer()

        renderer.applicationContext = context.applicationContext

        setRenderer(renderer)

        val data = ObjectParser.parseFile(context.resources.openRawResource(R.raw.cube))

        renderer.data = data

        renderMode = RENDERMODE_WHEN_DIRTY

        gestureHandler = MyGestureDetector(
            context = context.applicationContext,
            listener = MyGestureDetector.Listener(
                onScale = {
                    renderer.zoomVal = it
                    requestRender()
                },
                onPan = {
                    renderer.angle = it
                    requestRender()
                }
            )
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                renderer.computeTouchCollision(
                    x = x,
                    y = y
                )
            }
        }

        return gestureHandler.handleTouchEvent(event)
    }
}