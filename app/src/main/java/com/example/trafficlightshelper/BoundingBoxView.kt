package com.example.trafficlightshelper

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList

class BoundingBoxView (context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var boxPaint = Paint()
    private var listOfRect=ArrayList<RectF?>()
//    private var rect:RectF?=null
    private var scaleFactorHorizontal: Float = 2.2f
    private var scaleFactorVertical: Float = 3.2f

    init {
        initPaints()
    }

    fun clear() {
        boxPaint.reset()
        listOfRect.clear()
        invalidate()
        initPaints()
    }

    fun setRect(rect: RectF?){
        listOfRect.add(rect)
    }

    private fun initPaints() {
        boxPaint.color = ContextCompat.getColor(context!!, R.color.rect_color)
        boxPaint.strokeWidth = 6F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

       //Rect(96, 338 - 323, 423)
        for (i in listOfRect){
            val top = i?.top?.times(scaleFactorVertical)
            val bottom = i?.bottom?.times(scaleFactorVertical)
            val left = i?.left?.times(scaleFactorHorizontal)
            val right = i?.right?.times(scaleFactorHorizontal)

            // Draw bounding box around detected objects
            val drawableRect = RectF(left?:0f, top?:0f, right?:0f, bottom?:0f)
            canvas.drawRect(drawableRect, boxPaint)
        }


        
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
