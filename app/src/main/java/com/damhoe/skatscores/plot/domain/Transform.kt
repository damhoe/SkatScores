package com.damhoe.skatscores.plot.domain

import android.graphics.PointF
import androidx.compose.ui.geometry.Rect

class Transform
{
    var width: Int = 0
    var height: Int = 0
    var insets: Rect = Rect(0f, 0f, 0f, 0f)

    var viewportOffsetX: Float = 0f
    var viewportOffsetY: Float = 0f
    var viewportWidth: Float = 0f
    var viewportHeight: Float = 0f

    fun toPixelX(x: Float) =
        (x - viewportOffsetX) * (width / viewportWidth) + insets.left

    fun toPixelY(y: Float) =
        insets.top + height - (y - viewportOffsetY) * (height / viewportHeight)

    fun toViewportX(x: Float, ) =
        (x - insets.left) * viewportWidth / width + viewportOffsetX

    fun toViewportY(y: Float) =
        (height - y + insets.top) * viewportHeight / height + viewportOffsetY

    fun isVisible(point: PointF): Boolean
    {
        return point.x in insets.left..(insets.left + width) &&
                point.y in insets.top..(insets.top + height)
    }
}