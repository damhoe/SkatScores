package com.damhoe.skatscores.player.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class PlayerName(val value: String) : Parcelable
{
    init
    {
        require(value.length in 1..20) {
            "Player name must be between 1 and 20 characters long. Current length: ${value.length}"
        }
    }

    companion object
    {
        fun create(value: String): Result<PlayerName>
        {
            return runCatching { PlayerName(value) }
        }
    }
}