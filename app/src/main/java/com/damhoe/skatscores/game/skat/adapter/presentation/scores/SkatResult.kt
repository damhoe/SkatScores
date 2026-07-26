package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class SkatResult : Parcelable
{
    class Won : SkatResult()
    class Lost : SkatResult()
    class Overbid : SkatResult()
}