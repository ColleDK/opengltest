package com.colledk.opengltest.shapes

import android.opengl.GLES20
import com.colledk.opengltest.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


const val COORDS_PER_VERTEX = 3

class MyCube {

    private val color = listOf(
        floatArrayOf(
            255f,
            255f,
            255f,
            1.0f,
        ),
        floatArrayOf(
            1.0f,
            0.0f,
            1.0f,
            1.0f,
        ),
        floatArrayOf(
            1.0f,
            0.0f,
            0.0f,
            1.0f,
        ),
        floatArrayOf(
            0.0f,
            1.0f,
            0.0f,
            1.0f,
        ),
        floatArrayOf(
            0.0f,
            0.0f,
            1.0f,
            1.0f,
        ),
        floatArrayOf(
            0.3f,
            0.6f,
            0.3f,
            1.0f,
        )
    )

    private val coords = floatArrayOf(
        -0.5f, 0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
    )

    private val drawOrder: List<ShortArray> = listOf(
        shortArrayOf(
            0,1,2,0,2,3
        ),
        shortArrayOf(
            0,4,5,0,5,3
        ),
        shortArrayOf(
            0,1,6,0,6,4
        ),
        shortArrayOf(
            3,2,7,3,7,5
        ),
        shortArrayOf(
            1,2,7,1,7,6
        ),
        shortArrayOf(
            4,6,7,4,7,5
        )
    )

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "   gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "   gl_FragColor = vColor;" +
                "}"

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(coords.size * 4).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(coords)

                position(0)
            }
        }


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

    private val vertexCount: Int = coords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX / 4

    fun draw(mvpMatrix: FloatArray){
        for (i in 0 until 6){
            GLES20.glUseProgram(mProgram)

            positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

            colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

            GLES20.glEnableVertexAttribArray(positionHandle)

            GLES20.glVertexAttribPointer(
                positionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            val buffer = ByteBuffer.allocateDirect(
                drawOrder[i].size * 2
            ).run {
                order(ByteOrder.nativeOrder())

                asShortBuffer().apply {
                    put(drawOrder[i])

                    position(0)
                }
            }

            GLES20.glUniform4fv(colorHandle, 1, color[i], 0)

            matrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder[i].size, GLES20.GL_UNSIGNED_SHORT, buffer)

        }

        GLES20.glDisableVertexAttribArray(positionHandle)

    }

}