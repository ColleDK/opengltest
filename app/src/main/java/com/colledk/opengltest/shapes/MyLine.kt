package com.colledk.opengltest.shapes

import android.opengl.GLES20
import com.colledk.opengltest.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class MyLine(
    startCoords: FloatArray
) {

    private var coords: FloatArray = startCoords

    private var color = floatArrayOf(
        1f,
        1f,
        1f,
        1f,
    )

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main(){" +
                "   gl_Position = uMVPMatrix * vPosition;" +
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
                "void main(){" +
                "   gl_FragColor = vColor;" +
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

    private var colorHandle: Int = 0
    private var matrixHandle: Int = 0
    private var positionHandle: Int = 0

    private val vertexCount: Int = coords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    fun draw(mvpMatrix: FloatArray){
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        matrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

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

        GLES20.glEnableVertexAttribArray(positionHandle)

        // Draw the shape
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    fun changeColor(floatArray: FloatArray){
        color = floatArray
    }

    fun moveObject(floatArray: FloatArray){
        coords = floatArray
    }
}