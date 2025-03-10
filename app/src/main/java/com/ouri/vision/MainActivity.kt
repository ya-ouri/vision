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
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorMatrixFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
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
            finish()
        }
        previewView = findViewById(R.id.previewView)
        gpuImageView = findViewById(R.id.gpuImageView)
        gpuImage = GPUImage(this)

        
        val filterType = intent.getStringExtra("FILTER_TYPE") ?: "normal"
        applyFilter(filterType)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
        if (filterType == "dog") {
            val dogFilter = GPUImageColorMatrixFilter(
                1.0f, floatArrayOf(
                    0.45f, 0.55f, 0.00f, 0.0f,
                    0.45f, 0.55f, 0.00f, 0.0f,
                    0.00f, 0.00f, 1.00f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            )

            val blurFilter = GPUImageGaussianBlurFilter(0.5f)
            val contrastFilter = GPUImageContrastFilter(0.8f)
            val brightnessFilter = GPUImageBrightnessFilter(-0.1f)

            val filterGroup = GPUImageFilterGroup()
            filterGroup.addFilter(dogFilter)
            filterGroup.addFilter(blurFilter)
            filterGroup.addFilter(contrastFilter)
            filterGroup.addFilter(brightnessFilter)

            gpuImage.setFilter(filterGroup) 
            return 
        }
        if (filterType == "cat") {
            val catColorMatrix = GPUImageColorMatrixFilter(1.0f, floatArrayOf(
                0.2f, 0.4f, 0.4f, 0.0f,  
                0.0f, 0.7f, 0.3f, 0.0f, 
                0.0f, 0.2f, 0.8f, 0.0f, 
                0.0f, 0.0f, 0.0f, 1.0f  
            ))
                //                0.2f, 0.4f, 0.4f, 0.0f,  
            //                0.0f, 0.7f, 0.3f, 0.0f,  
            //                0.0f, 0.2f, 0.8f, 0.0f,  
            //                0.0f, 0.0f, 0.0f, 1.0f   

            val blurFilter = GPUImageGaussianBlurFilter(1f)
            val contrastFilter = GPUImageContrastFilter(0.5f)
            val brightnessFilter = GPUImageBrightnessFilter(-0.1f) 

            val filterGroup = GPUImageFilterGroup()
            filterGroup.addFilter(catColorMatrix)
            filterGroup.addFilter(blurFilter)
            filterGroup.addFilter(contrastFilter)
            filterGroup.addFilter(brightnessFilter)

            gpuImage.setFilter(filterGroup) 
            return
        }

        
        selectedFilter = when (filterType) {
            "deuteranopia" -> GPUImageColorMatrixFilter(
                1.0f, floatArrayOf(
                    0.38f, 0.68f, -0.10f, 0.0f,
                    0.30f, 0.52f, 0.08f, 0.0f,
                    -0.01f, 0.02f, 0.90f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            )

            "protanopia" -> GPUImageColorMatrixFilter(
                1.0f, floatArrayOf(
                    0.40f, 0.60f, -0.10f, 0.0f,
                    0.34f, 0.50f, 0.16f, 0.0f,
                    -0.02f, 0.02f, 0.90f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            )

            "tritanopia" -> GPUImageColorMatrixFilter(
                1.0f, floatArrayOf(
                    0.95f, -0.01f, 0.10f, 0.0f,
                    -0.10f, 0.90f, 0.1f, 0.0f,
                    0.00f, 1.3f, 0.275f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            )

            "achromatopsia" -> GPUImageColorMatrixFilter(
                1.0f, floatArrayOf(
                    0.33f, 0.33f, 0.33f, 0.0f,
                    0.33f, 0.33f, 0.33f, 0.0f,
                    0.33f, 0.33f, 0.33f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            )

            else -> null
        }

        selectedFilter?.let { gpuImage.setFilter(it) }
    }
}
