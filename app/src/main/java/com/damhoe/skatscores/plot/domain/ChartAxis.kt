package com.damhoe.skatscores.plot.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * Vertical axis of the score chart: a rounded value range plus the gridlines to label.
 *
 * Kept free of Android types so the arithmetic can be tested directly. Skat totals are whole
 * numbers, so every tick is a whole number too - no decimal labels.
 */
data class ChartAxis(
    val min: Int,
    val max: Int,
    val ticks: List<Int>,
)
{
    val span: Int get() = max - min

    /** Where [value] sits in the range, 0 at [min] and 1 at [max]. */
    fun fraction(value: Int): Float =
        if (span == 0) 0.5f else (value - min).toFloat() / span

    companion object
    {
        /** Smallest range worth showing, so a flat game does not collapse to a line. */
        private const val MIN_SPAN = 40

        /** Steps a tick is allowed to take, scaled by powers of ten. */
        private val NICE_STEPS = listOf(1, 2, 5)

        /**
         * Builds an axis covering [values], padded out to whole ticks.
         *
         * @param targetTickCount roughly how many gridlines to aim for; the real count varies
         *   because the step is snapped to a 1/2/5 multiple to keep labels readable
         */
        fun of(values: List<Int>, targetTickCount: Int = 5): ChartAxis
        {
            val lowest = values.minOrNull() ?: 0
            val highest = values.maxOrNull() ?: 0

            // Widen a narrow range around its centre rather than from zero, so a game that
            // hovers near a high total is not squashed against the top edge.
            val centre = (highest + lowest) / 2
            val halfSpan = max((highest - lowest + 1) / 2, MIN_SPAN / 2)
            val paddedMin = centre - halfSpan
            val paddedMax = centre + halfSpan

            val step = niceStep(paddedMax - paddedMin, targetTickCount)
            val min = floorTo(paddedMin, step)
            val max = ceilTo(paddedMax, step)

            val ticks = generateSequence(min) { it + step }
                .takeWhile { it <= max }
                .toList()

            return ChartAxis(min = min, max = max, ticks = ticks)
        }

        /** The 1/2/5 x 10^n step that lands nearest [targetTickCount] gridlines. */
        private fun niceStep(span: Int, targetTickCount: Int): Int
        {
            val rawStep = span.toDouble() / targetTickCount.coerceAtLeast(1)
            if (rawStep <= 0.0) return 1

            val magnitude = 10.0.pow(floor(log10(rawStep)))

            return NICE_STEPS
                .map { (it * magnitude).toInt().coerceAtLeast(1) }
                .minByOrNull { abs(span.toDouble() / it - targetTickCount) }
                ?: 1
        }

        private fun floorTo(value: Int, step: Int) =
            (floor(value.toDouble() / step) * step).toInt()

        private fun ceilTo(value: Int, step: Int) =
            (ceil(value.toDouble() / step) * step).toInt()
    }
}
