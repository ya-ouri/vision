package com.ouri.vision

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.InputStreamReader

class ShaderProgram(context: Context, vertexShaderResId: Int, fragmentShaderResId: Int) {

    val program: Int

    init {
        // Загрузка шейдеров из ресурсов
        val vertexShaderSource = loadShaderSource(context, vertexShaderResId)
        val fragmentShaderSource = loadShaderSource(context, fragmentShaderResId)

        // Компиляция шейдеров
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)

        // Создание и привязка программы
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        // Проверка ошибок линковки
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val errorMessage = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Ошибка линковки программы: $errorMessage")
        }
    }

    private fun loadShaderSource(context: Context, resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        // Проверка ошибок компиляции
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val errorMessage = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Ошибка компиляции шейдера: $errorMessage")
        }
        return shader
    }

    fun use() {
        GLES20.glUseProgram(program)
    }
}
