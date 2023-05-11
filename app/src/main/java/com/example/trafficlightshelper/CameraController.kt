package com.example.trafficlightshelper

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraController(private val activity: MainActivity) {
    var imageAnalyzer: ImageAnalysis? = null
    var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()


    fun registerCamera(surfaceProvider: SurfaceProvider,interfaceMLOutput: InterfaceMLOutput) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }


            //initialize image analyzer use case for listening ML output
            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        MLModelAnalyzer(activity.applicationContext, interfaceMLOutput)
                    )
                }


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    activity, CameraSelector.DEFAULT_BACK_CAMERA, preview,imageAnalyzer
                )


            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity))
    }
}