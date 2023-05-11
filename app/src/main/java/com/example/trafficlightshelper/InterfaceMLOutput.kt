package com.example.trafficlightshelper

import android.graphics.RectF
import com.example.trafficlightshelper.ml.Detect

interface InterfaceMLOutput {
    fun onSignalFound(listOfResult:List<Detect.DetectionResult>?)
}