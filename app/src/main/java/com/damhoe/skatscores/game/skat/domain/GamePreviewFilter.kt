package com.damhoe.skatscores.game.skat.domain

import java.time.Instant

sealed class GamePreviewFilter
{
    object All : GamePreviewFilter()
    data class SinceDate(val date: Instant) : GamePreviewFilter()
}