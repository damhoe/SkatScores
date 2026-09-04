package com.damhoe.skatscores.game.common

import android.os.Parcelable
import com.damhoe.skatscores.game.PlayerSelectionValidationResult
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class Title(val value: String) : Parcelable
{
    init
    {
        require(value.length in 1..20) {
            "Label must be between 1 and 20 characters long. Current length: ${value.length}"
        }
    }

    companion object
    {
        fun create(value: String): Result<Title>
        {
            return runCatching { Title(value) }
        }
    }
}