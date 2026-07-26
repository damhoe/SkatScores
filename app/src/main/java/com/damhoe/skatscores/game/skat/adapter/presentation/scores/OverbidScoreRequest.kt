package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class OverbidScoreRequest : Parcelable
{
    data class Create(
        val gameId: UUID,
        val soloPlayer: SkatParticipant,
    ) : OverbidScoreRequest()

    data class Update(
        val score: SkatScore,
        val soloPlayer: SkatParticipant,
    ) : OverbidScoreRequest()
}