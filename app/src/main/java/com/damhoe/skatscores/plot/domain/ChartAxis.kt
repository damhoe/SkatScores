package com.damhoe.skatscores.plot.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/** Value range rounded to whole ticks, with the gridlines to label. */
data class ChartAxis(
    val min: Int,
    val max: Int,
    val ticks: List<Int>,
)
{
    val span: Int get() = max - min

    /** 0 at [min], 1 at [max]. */
    fun fraction(value: Int): Float =
        if (span == 0) 0.5f else (value - min).toFloat() / span

    companion object
    {
        /** Keeps a flat list from collapsing to a line. */
        private const val MIN_SPAN = 40

        private val STEPS_PER_DECADE = listOf(1, 2, 5)

        /** [targetTickCount] is approximate: the step snaps to 1, 2 or 5 × 10ⁿ. */
        fun of(values: List<Int>, targetTickCount: Int = 5): ChartAxis
        {
            val lowest = values.minOrNull() ?: 0
            val highest = values.maxOrNull() ?: 0

            // Widen around the centre, not from zero, so high totals are not squashed.
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

        private fun niceStep(span: Int, targetTickCount: Int): Int
        {
            val rawStep = span.toDouble() / targetTickCount.coerceAtLeast(1)
            if (rawStep <= 0.0) return 1

            val magnitude = 10.0.pow(floor(log10(rawStep)))

            return STEPS_PER_DECADE
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
