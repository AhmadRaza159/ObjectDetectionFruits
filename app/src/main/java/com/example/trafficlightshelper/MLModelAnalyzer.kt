package com.example.trafficlightshelper

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.trafficlightshelper.ml.Detect
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class MLModelAnalyzer(
    private val context: Context,
    private val interfaceMLOutput: InterfaceMLOutput
) : ImageAnalysis.Analyzer {
    var buffer: ByteBuffer? = null
    var vuBuffer: ByteBuffer? = null
    var ySize: Int? = null
    var vuSize: Int? = null
    var nv21: ByteArray? = null
    var yuvImage: YuvImage? = null
    var out: ByteArrayOutputStream? = null
    var imageBytes: ByteArray? = null
    var lumaBitmap: Bitmap? = null
    var matrix: Matrix? = null


    var image: TensorImage? = null
    var outputs: Detect.Outputs? = null


    override fun analyze(imageProxy: ImageProxy) {
        buffer = imageProxy.planes[0].buffer
        vuBuffer = imageProxy.planes[2].buffer // VU
        ySize = buffer?.remaining()
        vuSize = vuBuffer?.remaining()
        nv21 = ByteArray(ySize!! + vuSize!!)
        buffer?.get(nv21, 0, ySize!!)
        vuBuffer?.get(nv21, ySize!!, vuSize!!)
        yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        out = ByteArrayOutputStream()
        yuvImage?.compressToJpeg(
            Rect(0, 0, yuvImage!!.width, yuvImage!!.height),
            100,
            out
        )
        imageBytes = out?.toByteArray()
        lumaBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
        matrix = Matrix()
        matrix?.postRotate(90f)
        lumaBitmap = Bitmap.createBitmap(
            lumaBitmap!!,
            0,
            0,
            lumaBitmap!!.getWidth(),
            lumaBitmap!!.getHeight(),
            matrix,
            true
        )

// Creates inputs for reference.
        image = TensorImage.fromBitmap(lumaBitmap)

// Runs model inference and gets result.
        val model = Detect.newInstance(context)

        outputs = model.process(image!!)
// Gets result from DetectionResult.
        interfaceMLOutput.onSignalFound(
            outputs?.detectionResultList
        )


// Releases model resources if no longer used.
        model.close()
        imageProxy.close()


    }
}