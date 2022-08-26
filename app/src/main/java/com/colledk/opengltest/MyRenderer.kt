package com.colledk.opengltest

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.colledk.opengltest.parser.data.ObjectData
import com.colledk.opengltest.shapes.MyCube
import com.colledk.opengltest.shapes.MyCubeWithTexture
import com.colledk.opengltest.shapes.MyCubeWithTextureParsed
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer: GLSurfaceView.Renderer {

    @Volatile
    var zoomVal: Float = 0f

    @Volatile
    var angle: Float = 0f

    @Volatile
    lateinit var applicationContext: Context

    @Volatile
    lateinit var data: ObjectData

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private lateinit var myCube: MyCube
    private lateinit var myCubeWithTexture: MyCubeWithTexture
    private lateinit var myCubeWithTextureParsed: MyCubeWithTextureParsed

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.7f, 0.3f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        myCube = MyCube()
        myCubeWithTexture = MyCubeWithTexture()
        myCubeWithTexture.loadTexture(0, listOf(R.raw.dice1, R.raw.dice6, R.raw.dice3, R.raw.dice4, R.raw.dice2, R.raw.dice5), applicationContext)

        myCubeWithTextureParsed = MyCubeWithTextureParsed(data)
        myCubeWithTextureParsed.loadTexture(0, listOf(R.raw.dice1, R.raw.dice6, R.raw.dice3, R.raw.dice4, R.raw.dice2, R.raw.dice5), applicationContext)
    }

    override fun onDrawFrame(p0: GL10?) {
        val scratch = FloatArray(16)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, -2f, 2f, 5f + zoomVal, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

//        val time = SystemClock.uptimeMillis() % 4000L
//        val angle = 0.090f * time.toInt()

        Matrix.setRotateM(rotationMatrix, 0, angle, 1f, 1f, 1f)

        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw
        myCubeWithTextureParsed.draw(scratch)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        screenHeight = height
        screenWidth = width

        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 100f)
    }

    fun computeTouchCollision(x: Float, y: Float){
        
        Timber.d("Computing touch collisions for {$x, $y} and view port {$screenWidth, $screenHeight}")

    }

}

fun loadShader(type: Int, shaderCode: String): Int {

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    return GLES20.glCreateShader(type).also { shader ->

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val shaderStatus: IntBuffer = ByteBuffer.allocateDirect(1 * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                position(0)
            }
        }
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, shaderStatus)

        when(shaderStatus.get()){
            1 -> { // Everything went okay
                Timber.d("Shader loaded correctly $shaderCode")
            }
            else -> {
                Timber.e("Error loading shader for $shaderCode")
            }
        }
    }
}