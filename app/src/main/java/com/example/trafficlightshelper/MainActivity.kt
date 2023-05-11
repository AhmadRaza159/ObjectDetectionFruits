package com.example.trafficlightshelper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.example.trafficlightshelper.databinding.ActivityMainBinding
import com.example.trafficlightshelper.ml.Detect
import java.util.*

class MainActivity : AppCompatActivity() {
    private val floatFactor=0.89
    private var count=0
    private lateinit var binding: ActivityMainBinding
    private var prevCat=""
    private val rectClearHandler:Handler by lazy { Handler(mainLooper) }
    private val textToSpeechEngine: TextToSpeech by lazy {
        // Pass in context and the listener.
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                // set our locale only if init was success.
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.ENGLISH
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Call Lollipop+ function
            textToSpeechEngine.speak("", TextToSpeech.QUEUE_FLUSH, null, "tts1")
        } else {
            // Call Legacy function
            textToSpeechEngine.speak("", TextToSpeech.QUEUE_FLUSH, null)
        }
        if (checkCameraPermission()){
            CameraController(this).registerCamera(binding.preview.surfaceProvider,
                object :InterfaceMLOutput{

                    override fun onSignalFound(listOfResult: List<Detect.DetectionResult>?) {
                        if (listOfResult!=null) {
                            runOnUiThread {
                                if (listOfResult[0].scoreAsFloat >= floatFactor){
                                    binding.boundingBox.clear()
                                    count=0
                                }
                                binding.detailsContainer.visibility=View.VISIBLE
                                for (i in listOfResult) {
                                    if (i.scoreAsFloat >= floatFactor) {
                                        count++
                                        binding.txtCat.text = i.categoryAsString
                                        binding.boundingBox.setRect(i.locationAsRectF)
                                    }
                                }
                                if (listOfResult[0].scoreAsFloat >= floatFactor){
                                    binding.txtProb.text ="Count: $count"
                                        speak(listOfResult[0].categoryAsString)
                                    binding.boundingBox.invalidate()
                                    clearRectHandler()
                                }

                            }

                        }

                    }
                })
        }
        else{
            reqCameraPermission()
        }

    }

    private fun speak(mlCategory: String?) {
        if (mlCategory!=prevCat){
            prevCat=mlCategory?:""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Call Lollipop+ function
                textToSpeechEngine.speak(mlCategory, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            } else {
                // Call Legacy function
                textToSpeechEngine.speak(mlCategory, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

    }

    private fun clearRectHandler(){
        try {
            rectClearHandler.removeCallbacksAndMessages(null)
            rectClearHandler.postDelayed({
                binding.boundingBox.clear()
                prevCat=""
                binding.detailsContainer.visibility=View.GONE
            },2000)
        }
        catch (exc:Exception){
            exc.printStackTrace()
        }
    }

    fun checkCameraPermission(): Boolean {
        var a = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        return a
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun reqCameraPermission() {
        requestPermissions(arrayOf(
            Manifest.permission.CAMERA
        ), 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onPause() {
        textToSpeechEngine.stop()
        super.onPause()
    }

    override fun onDestroy() {
        textToSpeechEngine.shutdown()
        super.onDestroy()
    }
}