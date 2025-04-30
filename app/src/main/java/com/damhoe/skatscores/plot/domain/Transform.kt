package com.damhoe.skatscores.plot.domain

import android.graphics.PointF
import androidx.compose.ui.geometry.Rect

class Transform
{
    var width: Int = 0
    var height: Int = 0
    var insets: Rect = Rect(
        left = 0f,
        top = 0f,
        right = 0f,
        bottom = 0f)
    var viewportOffsetX: Float = 0f
    var viewportOffsetY: Float = 0f
    var viewportWidth: Float = 0f
    var viewportHeight: Float = 0f
    
    fun toPixelX(
        x: Float,
    ) = (x - viewportOffsetX) * width / viewportWidth + insets.left
    
    fun toPixelY(
        y: Float,
    ) = height - (y - viewportOffsetY) * height / viewportHeight + insets.top
    
    fun toViewportX(
        x: Float,
    ) = (x - insets.left) * viewportWidth / width + viewportOffsetX
    
    fun toViewportY(
        y: Float
    ) = (height - y + insets.top) * viewportHeight / height + viewportOffsetY
    
    fun isVisible(
        point: PointF
    ) = point.run {
        x >= insets.left &&
        x <= insets.left + width &&
        y >= insets.top &&
        y <= insets.top + height
    }
    
    fun isInViewport(
        point: PointF
    ) = point.run {
        x >= viewportOffsetX &&
        x <= viewportOffsetX + viewportWidth &&
        y >= viewportOffsetY &&
        y <= viewportOffsetY + viewportHeight
    }
}