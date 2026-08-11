package com.damhoe.skatscores.plot.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.damhoe.skatscores.R
import com.damhoe.skatscores.plot.domain.ChartAxis
import com.google.android.material.color.MaterialColors
import kotlin.math.roundToInt

/**
 * The score chart: one line per seat over the rounds of a list.
 *
 * Deliberately narrow. A list is at most 32 rounds of three players, so the whole series
 * always fits and there is no viewport to pan or zoom - dragging picks a round to read
 * instead, which is the question the chart is actually asked. Everything measurable is
 * computed when the data or the size changes, never per frame.
 */
class ScoreChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr)
{
    /** One player's values, indexed by round. Index 0 is the state before the first round. */
    data class Series(
        val label: String,
        @ColorInt val color: Int,
        val values: List<Int>,
    )

    /** Called with the picked round, or null once the selection is cleared. */
    var onRoundSelected: ((Int?) -> Unit)? = null

    var series: List<Series> = emptyList()
        private set

    var selectedRound: Int? = null
        private set

    private var axis: ChartAxis = ChartAxis.of(emptyList())

    /** Number of played rounds; the x axis runs 0..roundCount. */
    private val roundCount: Int
        get() = (series.firstOrNull()?.values?.size ?: 1) - 1

    // ---- Paints and reusable scratch, allocated once ----

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
    }

    private val zeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = dp(2.5f)
    }

    private val markerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
    }

    private val linePath = Path()
    private val textBounds = Rect()

    private var gridColor = 0
    private var zeroColor = 0
    private var labelColor = 0
    private var surfaceColor = 0

    // ---- Plot area, recomputed on size or data change ----

    private var plotLeft = 0f
    private var plotTop = 0f
    private var plotRight = 0f
    private var plotBottom = 0f

    init
    {
        isFocusable = true
        resolveColors()

        ResourcesCompat.getFont(context, R.font.app_mono)?.let { mono ->
            labelPaint.typeface = mono
        } ?: run { labelPaint.typeface = Typeface.MONOSPACE }
    }

    private fun resolveColors()
    {
        gridColor = MaterialColors.getColor(this, R.attr.colorSurfaceContainerHighest)
        zeroColor = MaterialColors.getColor(this, R.attr.colorOutlineVariant)
        labelColor = MaterialColors.getColor(this, R.attr.colorOutline)
        // Markers punch a hole in the line they sit on, so this has to be the colour of the
        // card behind the chart - see cardBackgroundColor in fragment_game_graph.
        surfaceColor = MaterialColors.getColor(this, R.attr.colorSurfaceContainerLow)

        gridPaint.color = gridColor
        zeroLinePaint.color = zeroColor
        selectionPaint.color = MaterialColors.getColor(this, R.attr.colorOutline)
        labelPaint.color = labelColor
        markerFillPaint.color = surfaceColor
    }

    fun setSeries(series: List<Series>)
    {
        this.series = series
        this.axis = ChartAxis.of(series.flatMap { it.values })
        this.selectedRound = null

        updateContentDescription()
        measurePlotArea()
        invalidate()
    }

    fun clearSelection()
    {
        if (selectedRound == null) return

        selectedRound = null
        onRoundSelected?.invoke(null)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)
        measurePlotArea()
    }

    /**
     * Leaves room for the widest y label on the left and one line of x labels below.
     *
     * Padding is part of the budget: it is what keeps the end markers and the corner-most
     * labels clear of the rounded corners of the card this sits in.
     */
    private fun measurePlotArea()
    {
        val widestLabel = axis.ticks
            .maxOfOrNull { labelPaint.measureText(it.toString()) }
            ?: 0f

        plotLeft = paddingLeft + widestLabel + dp(10f)
        plotTop = paddingTop + dp(8f)
        plotRight = width - paddingRight - dp(8f)
        plotBottom = height - paddingBottom - labelHeight() - dp(12f)
    }

    private fun labelHeight(): Float
    {
        labelPaint.getTextBounds(SAMPLE_DIGITS, 0, SAMPLE_DIGITS.length, textBounds)
        return textBounds.height().toFloat()
    }

    private fun xOf(round: Int): Float
    {
        if (roundCount <= 0) return (plotLeft + plotRight) / 2f
        return plotLeft + (plotRight - plotLeft) * round / roundCount
    }

    private fun yOf(value: Int): Float =
        plotBottom - (plotBottom - plotTop) * axis.fraction(value)

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)

        if (series.isEmpty() || plotRight <= plotLeft || plotBottom <= plotTop) return

        drawGrid(canvas)
        drawXLabels(canvas)
        drawSelection(canvas)
        drawSeries(canvas)
    }

    private fun drawGrid(canvas: Canvas)
    {
        axis.ticks.forEach { tick ->
            val y = yOf(tick)
            // The zero line carries meaning in skat: it separates ahead from behind.
            val paint = if (tick == 0) zeroLinePaint else gridPaint
            canvas.drawLine(plotLeft, y, plotRight, y, paint)

            val label = tick.toString()
            val labelWidth = labelPaint.measureText(label)
            canvas.drawText(
                label,
                plotLeft - dp(6f) - labelWidth,
                y + labelHeight() / 2f,
                labelPaint
            )
        }
    }

    private fun drawXLabels(canvas: Canvas)
    {
        if (roundCount <= 0) return

        val step = xLabelStep()
        val baseline = plotBottom + labelHeight() + dp(10f)

        var round = 0
        while (round <= roundCount)
        {
            val label = round.toString()
            canvas.drawText(
                label,
                xOf(round) - labelPaint.measureText(label) / 2f,
                baseline,
                labelPaint
            )
            round += step
        }
    }

    /** Keep labels from colliding on a long list. */
    private fun xLabelStep(): Int = when
    {
        roundCount <= 6 -> 1
        roundCount <= 14 -> 2
        roundCount <= 24 -> 4
        else -> 5
    }

    private fun drawSelection(canvas: Canvas)
    {
        val round = selectedRound ?: return
        val x = xOf(round)
        canvas.drawLine(x, plotTop, x, plotBottom, selectionPaint)
    }

    private fun drawSeries(canvas: Canvas)
    {
        // Markers on every point only stay legible on a short list; otherwise just the ends
        // and the selected round get one.
        val markEveryPoint = roundCount <= MARKERS_UP_TO_ROUNDS

        series.forEach { line ->
            linePath.rewind()

            line.values.forEachIndexed { round, value ->
                val x = xOf(round)
                val y = yOf(value)
                if (round == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }

            linePaint.color = line.color
            canvas.drawPath(linePath, linePaint)

            line.values.forEachIndexed { round, value ->
                val isEnd = round == 0 || round == roundCount
                val isSelected = round == selectedRound
                if (markEveryPoint || isEnd || isSelected)
                {
                    drawMarker(canvas, xOf(round), yOf(value), line.color, emphasised = isSelected)
                }
            }
        }
    }

    private fun drawMarker(
        canvas: Canvas,
        x: Float,
        y: Float,
        @ColorInt color: Int,
        emphasised: Boolean,
    )
    {
        val radius = if (emphasised) dp(5f) else dp(3.5f)

        canvas.drawCircle(x, y, radius, markerFillPaint)

        linePaint.color = color
        linePaint.strokeWidth = dp(2.5f)
        canvas.drawCircle(x, y, radius, linePaint)
    }

    // ---- Selection ----

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        if (series.isEmpty() || roundCount <= 0) return false

        when (event.actionMasked)
        {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
            {
                parent?.requestDisallowInterceptTouchEvent(true)
                selectRoundAt(event.x)
            }

            MotionEvent.ACTION_UP ->
            {
                parent?.requestDisallowInterceptTouchEvent(false)
                selectRoundAt(event.x)
                performClick()
            }

            MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)

            else -> return super.onTouchEvent(event)
        }

        return true
    }

    override fun performClick(): Boolean
    {
        super.performClick()
        return true
    }

    /** Snaps to the nearest round so a value can be read exactly rather than interpolated. */
    private fun selectRoundAt(x: Float)
    {
        val plotWidth = plotRight - plotLeft
        if (plotWidth <= 0f) return

        val fraction = ((x - plotLeft) / plotWidth).coerceIn(0f, 1f)
        val round = (fraction * roundCount).roundToInt().coerceIn(0, roundCount)

        if (round == selectedRound) return

        selectedRound = round
        onRoundSelected?.invoke(round)
        announceSelection(round)
        invalidate()
    }

    private fun updateContentDescription()
    {
        contentDescription = if (series.isEmpty())
        {
            null
        } else
        {
            context.getString(
                R.string.description_score_chart,
                roundCount,
                series.joinToString(", ") { "${it.label} ${signed(it.values.lastOrNull() ?: 0)}" }
            )
        }
    }

    private fun announceSelection(round: Int)
    {
        announceForAccessibility(
            context.getString(R.string.title_round, round) + ": " +
                    series.joinToString(", ") {
                        "${it.label} ${signed(it.values.getOrNull(round) ?: 0)}"
                    }
        )
    }

    private fun signed(value: Int) = if (value > 0) "+$value" else value.toString()

    private fun dp(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        resources.displayMetrics
    )

    private fun sp(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        value,
        resources.displayMetrics
    )

    companion object
    {
        private const val SAMPLE_DIGITS = "0123456789-"

        /** Above this many rounds, per-point markers turn into clutter. */
        private const val MARKERS_UP_TO_ROUNDS = 12
    }
}
