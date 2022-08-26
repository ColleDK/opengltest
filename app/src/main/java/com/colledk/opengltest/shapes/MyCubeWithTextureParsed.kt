package com.colledk.opengltest.shapes

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.colledk.opengltest.loadShader
import com.colledk.opengltest.parser.data.ObjectData
import timber.log.Timber
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.sqrt

private const val SIDE_SIZE = 6
private const val COORDS_PER_TEXTURE = 3

class MyCubeWithTextureParsed(
    private val objectData: ObjectData
) {
    private val textures = IntArray(size = SIDE_SIZE)

    private val color = floatArrayOf(
        1f,
        1f,
        1f,
        1.0f,
    )

    val coords: () -> FloatArray = {
        val array = mutableListOf<Float>()

        objectData.vertices.forEach { vertex ->
            array.add(vertex.x)
            array.add(vertex.y)
            array.add(vertex.z)
        }

        array.toFloatArray()
    }

    val drawOrder: () -> ShortArray = {
        val array = mutableListOf<Short>()

        objectData.faces.forEach { face ->
            array.add(face.coord1.toShort())
            array.add(face.coord2.toShort())
            array.add(face.coord3.toShort())
        }

        array.toShortArray()
    }

    private val drawBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder().size * 2).run {
            order(ByteOrder.nativeOrder())

            asShortBuffer().apply {
                put(drawOrder())

                position(0)
            }
        }

    private val textureCoords = floatArrayOf(
        0.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f,
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
                "attribute vec3 textureCoordIn;" +
                "varying vec3 textureCoordOut;" +
                "void main() {" +
                "   gl_Position = uMVPMatrix * vPosition;" +
                "   textureCoordOut = vPosition.xyz;" +
                "}"

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(4 * coords().size).run {
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(coords())

                position(0)
            }
        }

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "uniform samplerCube texture;" +
                "varying vec3 textureCoordOut;" +
                "void main() {" +
                "   gl_FragColor = textureCube(texture, textureCoordOut);" +
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

    private val vertexCount: Int = coords().size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    private fun loadHandles(){
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        if (positionHandle == -1 ){
            Timber.e("Could not get attribute location for vPosition")
        }

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        if (colorHandle == -1 ){
            Timber.e("Could not get attribute location for vColor")
        }

        matrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        if (matrixHandle == -1 ){
            Timber.e("Could not get attribute location for uMVPMatrix")
        }

        textureCoordHandle = GLES20.glGetAttribLocation(mProgram, "textureCoordIn")
        if (textureCoordHandle == -1 ){
            Timber.e("Could not get attribute location for textureCoordIn")
        }

        textureHandle = GLES20.glGetUniformLocation(mProgram, "texture")
        if (textureHandle == -1 ){
            Timber.e("Could not get attribute location for texture")
        }
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textures[0])
        GLES20.glUniform1i(textureHandle, 0)

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder().size, GLES20.GL_UNSIGNED_SHORT, drawBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
    }

    fun loadTexture(index: Int, resources: List<Int>, context: Context){
        // Bind the texture
        GLES20.glGenTextures(1, textures, index)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textures[index])

        // Apply filters
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())

        // Wrapping
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

        for (i in 0 until 6){
            val stream = if (i >= resources.size) {
                context.resources.openRawResource(resources[0])
            } else context.resources.openRawResource(resources[i])
            val bitmap = BitmapFactory.decodeStream(stream)

            // Load the bitmap in
            GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0)

            // Cleanup
            bitmap.recycle()
        }
    }

    fun rayPick(touchedX: Float, touchedY: Float, screenWidth: Float, screenHeight: Float, viewMatrix: FloatArray, projectionMatrix: FloatArray): FloatArray{
        // Step 1 normalize the device coordinate to 3D space
        val normalizedCoordinates = floatArrayOf(
            (2 * touchedX) / screenWidth - 1, // X
            1 - (2 * touchedY) / screenHeight, // Y
            1f // Z
        )

        Timber.d("Normalized coordinates ${normalizedCoordinates.joinToString()}")

        // Step 2 Create a 4D homogenous clip coordinate
        val rayClip = floatArrayOf(
            normalizedCoordinates[0], // X
            normalizedCoordinates[1], // Y
            normalizedCoordinates[2].times(-1), // Z (should point towards - which is negative Z direction
            1f // w
        )

        Timber.d("Ray clip ${rayClip.joinToString()}")

        // Step 3 Transform camera coordinates to 4D
        // Create the inverse projection matrix
        val inversedProjectionMatrix = FloatArray(16)
        Matrix.invertM(inversedProjectionMatrix, 0, projectionMatrix, 0)

        Timber.d("Inversed projection matrix ${inversedProjectionMatrix.joinToString()}")


        // Find the 4D coordinates of the camera
        val rayCamera = FloatArray(4)
        Matrix.multiplyMV(rayCamera, 0, inversedProjectionMatrix, 0, rayClip, 0)

        // We only needed x,y coordinates so we can specify the z,w coordinates
        rayCamera[2] = -1f
        rayCamera[3] = 0f

        // Step 4 Get the 4D world coordinates
        val inversedViewMatrix = FloatArray(16)
        Matrix.invertM(inversedViewMatrix, 0, viewMatrix, 0)

        val rayWorld = FloatArray(4)
        Matrix.multiplyMV(rayWorld, 0, inversedViewMatrix, 0, rayCamera, 0)

        val normalizedRayWorld = rayWorld.normalize()
        Timber.d("Normalized vector is ${normalizedRayWorld.joinToString()}")
        return normalizedRayWorld
    }

    private fun FloatArray.normalize(): FloatArray{
        check(this.size >= 3){
            Timber.e("Failed to normalize array with size ${this.size}")
            throw Exception("Cannot normalize array with size ${this.size}. Size should be at least 3")
        }

        // Calculate the vector length
        val currentX = this[0]
        val currentY = this[1]
        val currentZ = this[2]

        val length = sqrt((currentX * currentX) + (currentY * currentY) + (currentZ * currentZ))

        // Calculate the normalized vector
        val normalizedX = currentX / length
        val normalizedY = currentY / length
        val normalizedZ = currentZ / length

        return floatArrayOf(
            normalizedX,
            normalizedY,
            normalizedZ,
        )
    }

    fun FloatArray.times(n: Int): FloatArray {
        return this.map {
            it * n
        }.toFloatArray()
    }
}