package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class ScoreRequest : Parcelable
{
    data class Create(
        val gameId: UUID,
        val players: SkatParticipants,
    ) : ScoreRequest()

    data class Update(
        val scoreId: UUID,
        val players: SkatParticipants,
    ) : ScoreRequest()
}

