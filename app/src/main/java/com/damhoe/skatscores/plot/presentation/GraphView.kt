package com.damhoe.skatscores.plot.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.compose.ui.geometry.Rect
import androidx.core.view.ViewCompat
import com.damhoe.skatscores.R
import com.damhoe.skatscores.plot.domain.Plot
import com.damhoe.skatscores.plot.domain.LineGraphData
import com.damhoe.skatscores.plot.domain.Transform
import com.google.android.material.color.MaterialColors
import kotlin.math.abs
import kotlin.properties.Delegates

class GraphView(
    context: Context,
    attrs: AttributeSet,
) : View(
    context,
    attrs
)
{
    @ColorInt
    private val defaultGridColor = MaterialColors.getColor(
        this,
        R.attr.colorSurfaceVariant
    )

    @ColorInt
    private val defaultTickLabelColor = MaterialColors.getColor(
        this,
        R.attr.colorOnSurfaceVariant
    )

    @ColorInt
    private val primaryColor = MaterialColors.getColor(
        this,
        R.attr.colorPrimary
    );

    @ColorInt
    private val gridColor: Int

    init
    {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GraphView,
            0,
            0
        )
            .apply {
                try
                {
                    gridColor = getColor(
                        R.styleable.GraphView_gridColor,
                        defaultGridColor
                    )
                } finally
                {
                    recycle()
                }
            }
    }

    var scorePlotDefault = Plot(
        RectF(
            0f,
            0f,
            20f,
            100f
        )
    ).apply {

        style.apply {
            gridColor = this@GraphView.gridColor
            tickLabelSize = 16f
            tickLabelColor = this@GraphView.gridColor
            legendColor = this@GraphView.gridColor
        }

        gridDistanceX = 2f
        gridDistanceY = 10f
        lineGraphs.add(
            LineGraphData(
                points = listOf(
                    PointF(
                        0f,
                        25f
                    ),
                    PointF(
                        1f,
                        23f
                    ),
                    PointF(
                        2f,
                        40f
                    ),
                    PointF(
                        3f,
                        43f
                    ),
                    PointF(
                        4f,
                        65f
                    ),
                    PointF(
                        5f,
                        12f
                    ),
                    PointF(
                        10f,
                        23f
                    ),
                    PointF(
                        20f,
                        20f
                    ),
                ),
                color = primaryColor,
                label = "Player 1"
            )
        )
        lineGraphs.add(
            LineGraphData(
                points = listOf(
                    PointF(
                        0f,
                        10f
                    ),
                    PointF(
                        1f,
                        26f
                    ),
                    PointF(
                        2f,
                        20f
                    ),
                    PointF(
                        3f,
                        23f
                    ),
                    PointF(
                        4f,
                        44f
                    ),
                    PointF(
                        5f,
                        11f
                    ),
                    PointF(
                        10f,
                        23f
                    ),
                    PointF(
                        11f,
                        12f
                    ),
                    PointF(
                        15f,
                        15f
                    ),
                    PointF(
                        20f,
                        20f
                    ),
                ),
                color = primaryColor,
                label = "Player 2"
            )
        )
    }

    var scorePlot: Plot by Delegates.observable(scorePlotDefault) { _, _, new ->
        new.style.apply {
            gridColor = this@GraphView.gridColor
            tickLabelColor = MaterialColors.getColor(
                this@GraphView,
                R.attr.colorOnSurfaceVariant
            )
            legendColor = MaterialColors.getColor(
                this@GraphView,
                R.attr.colorOnSurface
            )
        }

        transform.apply {
            viewportHeight = abs(new.bounds.height())
            viewportWidth = 3f
            viewportOffsetY = new.bounds.bottom
        }

        maxViewportWidth = new.bounds.width()
        minViewportHeight = abs(new.bounds.height())
        maxViewportHeight = abs(new.bounds.height())
    }

    private var minViewportWidth = 2.5f
    private var maxViewportWidth = scorePlot.bounds.width()
    private var minViewportHeight = scorePlot.bounds.height()
    private var maxViewportHeight = scorePlot.bounds.height()

    private val transform = Transform().apply {
        insets = Rect(
            left = 120f,
            right = 50f,
            top = 0f,
            bottom = 150f
        )
        viewportHeight = scorePlot.bounds.height()
        viewportWidth = 10f // scorePlot.maxX - scorePlot.minX
    }

    private val mScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener()
    {
        private val viewportFocus = PointF()

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean
        {
            viewportFocus.set(
                transform.toViewportX(detector.focusX),
                transform.toViewportY(detector.focusY)
            )
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean
        {
            val scaleX: Float = detector.previousSpanX / detector.currentSpanX
            val scaleY: Float = detector.previousSpanY / detector.currentSpanY

            val oldWidth: Float = transform.viewportWidth
            val oldHeight: Float = transform.viewportHeight

            val focusX: Float = detector.focusX
            val focusY: Float = detector.focusY

            Log.d(
                "Scale",
                "Focus at ($focusX, $focusY)"
            )
            Log.d(
                "Scale",
                "Focus of viewport at (${viewportFocus.x}, ${viewportFocus.y})"
            )
            Log.d(
                "Scale",
                "x-scale: $scaleX"
            )

            val targetWidth: Float = abs(scaleX) * oldWidth
            val targetHeight: Float = abs(scaleY) * oldHeight
            val newWidth = maxViewportWidth
                .coerceAtMost(targetWidth)
                .coerceAtLeast(minViewportWidth)
            val newHeight = maxViewportHeight
                .coerceAtMost(targetHeight)
                .coerceAtLeast(minViewportHeight)

            transform.apply {
                //viewportHeight = newHeight
                viewportWidth = newWidth
            }

            val viewportDistX: Float =
                viewportFocus.x - (focusX - transform.insets.left) *
                        newWidth / transform.width - transform.viewportOffsetX
            val viewportDistY: Float = 0f //viewportFocusY * (1 - newHeight / oldHeight)

            translateViewport(
                viewportDistX,
                viewportDistY
            )

            // Update display
            this@GraphView.postInvalidateOnAnimation()

            viewportFocus.set(
                transform.toViewportX(focusX),
                transform.toViewportY(focusY)
            )
            return true
        }
    }

    private val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(
        context,
        mScaleGestureListener
    )

    private val scrollListener = object : GestureDetector.SimpleOnGestureListener()
    {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean
        {
            // Calculate the viewport translation
            val viewportDistanceX: Float = distanceX * transform.viewportWidth / width
            val viewportDistanceY: Float = 0f //-distanceY * transform.viewportHeight / height

            translateViewport(
                viewportDistanceX,
                viewportDistanceY
            )

            // Update screen
            this@GraphView.postInvalidateOnAnimation()
            return true
        }
    }

    private val scrollDetector: GestureDetector = GestureDetector(
        context,
        scrollListener
    )

    private fun translateViewport(
        dX: Float,
        dY: Float,
    )
    {
        /*
        * Constrains the viewport within the image bounds.
        */
        val curX = transform.viewportOffsetX + dX
        val curY = transform.viewportOffsetY + dY
        val curWidth = transform.viewportWidth
        val curHeight = transform.viewportHeight
        val newX = scorePlot.bounds.left.coerceAtLeast(
            curX.coerceAtMost(scorePlot.bounds.right - curWidth)
        )
        val newY = scorePlot.bounds.bottom.coerceAtLeast(
            curY.coerceAtMost(scorePlot.bounds.top - curHeight)
        )

        transform.apply {
            viewportOffsetX = newX
            //viewportOffsetY = newY
        }
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        scorePlot.draw(
            canvas,
            transform
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    )
    {
        super.onSizeChanged(
            w,
            h,
            oldw,
            oldh
        )

        /*
        * Update transformation object width and height which matches
        * the canvas bounds minus the insets of the image in pixel coordinates
        */
        transform.apply {
            width = (w - (insets.left + insets.right)).toInt()
            height = (h - (insets.top + insets.bottom)).toInt()
        }

        // Log canvas size changes
        Log.d(
            "Canvas changed",
            "New size: width = $w, height = $h"
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        scaleDetector.onTouchEvent(event)
        scrollDetector.onTouchEvent(event)
        return true
    }
}