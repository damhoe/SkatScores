package com.damhoe.skatscores.plot.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartAxisTest
{
    @Test
    fun `range covers every value`()
    {
        val values = listOf(0, 24, -96, 148, 12)

        val axis = ChartAxis.of(values)

        assertTrue(axis.min <= values.min())
        assertTrue(axis.max >= values.max())
    }

    @Test
    fun `range never collapses when every value is the same`()
    {
        // A game of nothing but passes: without a floor the chart would divide by zero
        // mapping points to pixels.
        val axis = ChartAxis.of(listOf(0, 0, 0, 0))

        assertTrue(axis.span >= 40)
        assertTrue(axis.min < 0)
        assertTrue(axis.max > 0)
    }

    @Test
    fun `a narrow range is widened around its centre, not towards zero`()
    {
        val axis = ChartAxis.of(listOf(500, 505, 510))

        assertTrue("min should stay near the data", axis.min > 400)
        assertTrue(axis.min <= 500)
        assertTrue(axis.max >= 510)
    }

    @Test
    fun `ticks are whole numbers on a round step`()
    {
        val axis = ChartAxis.of(listOf(-96, 148))

        assertTrue(axis.ticks.isNotEmpty())

        val step = axis.ticks[1] - axis.ticks[0]
        val normalised = generateSequence(step) { if (it % 10 == 0) it / 10 else null }.last()

        assertTrue("step $step should be a 1/2/5 multiple", normalised in listOf(1, 2, 5))
        assertTrue(axis.ticks.all { (it - axis.min) % step == 0 })
    }

    @Test
    fun `ticks are evenly spaced and span the range`()
    {
        val axis = ChartAxis.of(listOf(-40, 260))

        val steps = axis.ticks.zipWithNext { a, b -> b - a }.toSet()

        assertEquals(1, steps.size)
        assertEquals(axis.min, axis.ticks.first())
        assertEquals(axis.max, axis.ticks.last())
    }

    @Test
    fun `fraction maps the range onto zero to one`()
    {
        val axis = ChartAxis.of(listOf(-100, 100))

        assertEquals(0f, axis.fraction(axis.min), 0.0001f)
        assertEquals(1f, axis.fraction(axis.max), 0.0001f)
        assertEquals(0.5f, axis.fraction((axis.min + axis.max) / 2), 0.0001f)
    }

    @Test
    fun `an empty game still yields a usable axis`()
    {
        val axis = ChartAxis.of(emptyList())

        assertTrue(axis.span > 0)
        assertTrue(axis.ticks.size >= 2)
    }
}
