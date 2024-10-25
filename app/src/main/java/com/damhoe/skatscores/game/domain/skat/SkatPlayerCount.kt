package com.damhoe.skatscores.game.domain.skat

import android.os.Parcelable
import arrow.core.Either
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SkatPlayerCount(
    val value: Int) : Parcelable
{
    Three(3)
}