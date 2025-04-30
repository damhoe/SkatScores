package com.damhoe.skatscores.plot.domain.geometry

sealed class FindSegmentInsideResult
{
    data class Segment(val segment: LineSegment) : FindSegmentInsideResult()
    object NoSegment : FindSegmentInsideResult()
}