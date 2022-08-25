package com.colledk.opengltest.parser

import com.colledk.opengltest.parser.data.Face
import com.colledk.opengltest.parser.data.ObjectData
import com.colledk.opengltest.parser.data.Vertex
import java.io.File
import java.io.InputStream

object ObjectParser {

    fun parseFile(file: File): ObjectData{
        val inputStream = file.inputStream()

        return parseFile(inputStream = inputStream)
    }

    fun parseFile(inputStream: InputStream): ObjectData{
        val lines = mutableListOf<String>()

        inputStream.bufferedReader().forEachLine { line -> lines.add(line) }

        val data = parseLines(lines = lines)

        inputStream.close()

        return data
    }

    private fun parseLines(lines: List<String>): ObjectData{
        val vertices: MutableList<Vertex> = mutableListOf()
        val faces: MutableList<Face> = mutableListOf()

        lines.forEach { line ->
            when{
                line.startsWith("#") -> { /* Name of the file */ }
                line.startsWith("g") -> { /* Name of the object */ }
                line.startsWith("vn") -> { /* */ }
                line.startsWith("v") -> {
                    /* Vertex data */
                    val vertexData = line.replace("  ", " ").split(" ")

                    vertices.add(
                        Vertex(
                            x = vertexData[1].toFloatOrNull() ?: 0f,
                            y = vertexData[2].toFloatOrNull() ?: 0f,
                            z = vertexData[3].toFloatOrNull() ?: 0f,
                        )
                    )
                }
                line.startsWith("f") -> {
                    /* Face data */
                    val faceData = line.replace("  ", " ").split(" ")

                    faces.add(
                        Face(
                            coord1 = faceData[1].first().toString().toInt(),
                            coord2 = faceData[2].first().toString().toInt(),
                            coord3 = faceData[3].first().toString().toInt(),
                        )
                    )
                }
            }
        }

        return ObjectData(
            vertices = vertices.toList(),
            faces = faces.toList()
        )
    }

}