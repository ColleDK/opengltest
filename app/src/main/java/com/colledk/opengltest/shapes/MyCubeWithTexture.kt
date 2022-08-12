package com.colledk.opengltest.shapes

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.colledk.opengltest.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

private const val SIDE_SIZE = 6
private const val COORDS_PER_TEXTURE = 2

class MyCubeWithTexture {
    private val textures = IntArray(size = 1)

    private val color = floatArrayOf(
        1f,
        1f,
        1f,
        1.0f,
    )

    private val coords = floatArrayOf(
        -0.5f, 0.5f, 0.5f, // 0 front top left
        -0.5f, -0.5f, 0.5f, // 1 front bottom left
        0.5f, -0.5f, 0.5f, // 2 front bottom right
        0.5f, 0.5f, 0.5f, // 3 front top right
        -0.5f, 0.5f, -0.5f, // 4 back top left
        0.5f, 0.5f, -0.5f, // 5 back top right
        -0.5f, -0.5f, -0.5f, // 6 back bottom left
        0.5f, -0.5f, -0.5f, // 7 back bottom right
    )

    private val drawOrder = shortArrayOf(
        7, 6, 2, 2, 6, 1, // Bottom square
        1, 2, 0, 0, 2, 3, // Front square
        2, 7, 3, 3, 7, 5, // Right square
        7, 6, 5, 5, 6, 4, // Back square
        6, 1, 4, 4, 1, 0, // Left square
        0, 3, 4, 4, 3, 5, // Top square
    )

    private val drawBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())

            asShortBuffer().apply {
                put(drawOrder)

                position(0)
            }
        }

    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )

    private val textureBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(textureCoords.size * 4).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(textureCoords)

                position(0)
            }
        }

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec2 textureCoordIn;" +
                "varying vec2 textureCoordOut;" +
                "void main() {" +
                "   gl_Position = uMVPMatrix * vPosition;" +
                "   textureCoordOut = textureCoordIn;" +
                "}"

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(4 * coords.size).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(coords)

                position(0)
            }
        }

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "uniform sampler2D texture;" +
                "varying vec2 textureCoordOut;" +
                "void main() {" +
                "   gl_FragColor = texture2D(texture, textureCoordOut) * vColor;" +
                "}"



    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)

            GLES20.glAttachShader(it, fragmentShader)

            GLES20.glLinkProgram(it)
        }
    }

    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var matrixHandle: Int = 0
    private var textureCoordHandle: Int = 0
    private var textureHandle: Int = 0

    private val vertexCount: Int = coords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    private fun loadHandles(){
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        matrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        textureCoordHandle = GLES20.glGetAttribLocation(mProgram, "textureCoordIn")

        textureHandle = GLES20.glGetUniformLocation(mProgram, "texture")
    }

    fun draw(mvpMatrix: FloatArray){
        GLES20.glUseProgram(mProgram)

        // Get the handles
        loadHandles()

        // Set the color for drawing
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        // Apply projection matrix
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

        // Prepare coordinate data
        GLES20.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        // Prepare texture data
        GLES20.glVertexAttribPointer(
            textureCoordHandle,
            COORDS_PER_TEXTURE,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_TEXTURE * 4,
            textureBuffer
        )

        // Enable handles
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureCoordHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glUniform1i(textureHandle, 0)

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
    }

    fun loadTexture(index: Int, resource: Int, context: Context){
        val stream = context.resources.openRawResource(resource)
        val bitmap = BitmapFactory.decodeStream(stream)

        // Bind the texture
        GLES20.glGenTextures(1, textures, index)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index])

        // Apply filters
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())

        // Wrapping
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

        // Load the bitmap in
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Cleanup
        bitmap.recycle()
    }
}