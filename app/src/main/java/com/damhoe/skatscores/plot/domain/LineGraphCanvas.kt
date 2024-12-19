package com.damhoe.skatscores.plot.domain

import android.content.res.Resources
import android.graphics.Canvas
import android.util.TypedValue

class LineGraphCanvas
{
    private lateinit var canvas: Canvas
    private lateinit var transform: Transform
    
    fun initialize(
        canvas: Canvas,
        transform: Transform,
    )
    {
        this@LineGraphCanvas.canvas = canvas
        this@LineGraphCanvas.transform = transform
    }
    
    private fun dpToPx(
        dp: Float,
    ): Int
    {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics)
            .toInt()
    }
}