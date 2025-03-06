package com.ouri.vision

import android.opengl.GLES20
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import java.nio.FloatBuffer

class ProtanopiaFilter : GPUImageFilter() {

    private val colorMatrix = floatArrayOf(
        0.567f, 0.433f, 0.000f, 0.0f,
        0.558f, 0.442f, 0.000f, 0.0f,
        0.000f, 0.242f, 0.758f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    private var matrixHandle = 0

    override fun onInitialized() {
        super.onInitialized()
        // Получаем индекс шейдера для использования colorMatrix
        matrixHandle = GLES20.glGetUniformLocation(program, "colorMatrix")
    }

    // Правильная сигнатура метода onDraw
    override fun onDraw(textureId: Int, cubeBuffer: FloatBuffer?, textureBuffer: FloatBuffer?) {
        // Используем текущую программу и передаем матрицу для цвета
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, colorMatrix, 0)

        // Вызов родительского метода для отрисовки текстуры
        super.onDraw(textureId, cubeBuffer, textureBuffer)
    }
}
