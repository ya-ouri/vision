package com.ouri.vision

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorMatrixFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter

import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

private val backgroundExecutor = Executors.newSingleThreadExecutor()

class MainActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var gpuImageView: GPUImageView
    private lateinit var gpuImage: GPUImage
    private var selectedFilter: GPUImageColorMatrixFilter? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        window.statusBarColor = resources.getColor(R.color.cool_black, theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val backButton: ImageButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, ChoiceVisionActivity::class.java)
            startActivity(intent)
            finish()// Закрываем текущую активность, если не нужен возврат
        }
        previewView = findViewById(R.id.previewView)
        gpuImageView = findViewById(R.id.gpuImageView)
        gpuImage = GPUImage(this)

        // Получаем переданный фильтр из ChoiceVisionActivity
        val filterType = intent.getStringExtra("FILTER_TYPE") ?: "normal"
        applyFilter(filterType)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(backgroundExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("MainActivity", "Error binding camera use cases", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        fun rotateBitmapIfNeeded(bitmap: Bitmap, imageProxy: ImageProxy): Bitmap {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Log.d("MainActivity", "Rotation Degrees: $rotationDegrees")
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        if (bitmap == null) {
            Log.e("MainActivity", "Bitmap is null after conversion")
            imageProxy.close()
            return
        }

        val correctedBitmap = rotateBitmapIfNeeded(bitmap, imageProxy)
        gpuImage.setImage(correctedBitmap)

        // ✅ Применяем ТОЛЬКО выбранный фильтр из ChoiceVisionActivity
        selectedFilter?.let {
            gpuImage.setFilter(it)
        }

        val filteredBitmap = gpuImage.bitmapWithFilterApplied

        if (filteredBitmap == null) {
            Log.e("MainActivity", "Filtered bitmap is null")
            imageProxy.close()
            return
        }

        runOnUiThread {
            gpuImageView.setImage(filteredBitmap)
            gpuImageView.requestRender()
            Log.d("MainActivity", "Filtered bitmap set to GPUImageView")
        }

        imageProxy.close()
    }

    private fun applyFilter(filterType: String) {
        selectedFilter = when (filterType) {
            "deuteranopia" -> GPUImageColorMatrixFilter(1.0f, floatArrayOf(
                0.38f, 0.68f, -0.10f, 0.0f,
                0.30f, 0.52f,  0.08f, 0.0f,
                -0.01f, 0.02f,  0.90f, 0.0f,
                0.0f,   0.0f,   0.0f,   1.0f
            ))
                //                0.43f, 0.72f, -0.15f, 0.0f,  // Красный канал (R)
            //                0.34f, 0.57f,  0.09f, 0.0f,  // Зеленый канал (G)
            //                -0.02f, 0.03f,  1.00f, 0.0f, // Синий канал (B)
            //                0.0f,   0.0f,   0.0f,   1.0f  // Альфа-канал



            "protanopia" -> GPUImageColorMatrixFilter(1.0f, floatArrayOf(
                0.40f, 0.60f, -0.10f, 0.0f,
                0.34f, 0.50f,  0.16f, 0.0f,
                -0.02f, 0.02f, 0.90f, 0.0f,
                0.0f,   0.0f,  0.0f,  1.0f
            ))

            "tritanopia" -> GPUImageColorMatrixFilter(1.0f, floatArrayOf(
                0.95f, 0.05f, 0.00f, 0.0f,
                0.00f, 0.90f, 0.10f, 0.0f,
                0.20f, 0.875f, 0.10f, 0.0f,
                0.0f,  0.0f,  0.0f,  1.0f

                //                0.950f, 0.050f, 0.000f, 0.0f,  // Красный → Почти не меняется
                //                0.000f, 0.433f, 0.567f, 0.0f,  // Зелёный → Голубоватый
                //                0.000f, 0.475f, 0.525f, 0.0f,  // Синий → Светло-зелёный/серый
                //                0.0f, 0.0f, 0.0f, 1.0f
            ))
            else -> null
        }
    }
}
