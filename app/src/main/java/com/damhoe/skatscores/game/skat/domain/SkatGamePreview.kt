package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Title
import java.time.Instant
import java.util.UUID

data class SkatGamePreview(
    val title: Title,
    val playedAt: Instant,
    val playerNames: List<String>,
    val gameId: UUID,
)
{
    companion object
    {
        fun mapFrom(skatGame: SkatGame): SkatGamePreview
        {
            return SkatGamePreview(
                title = skatGame.title,
                playedAt = skatGame.playedAt,
                playerNames = skatGame.participants.run {
                    listOf(foreHand.displayName, middleHand.displayName, rearHand.displayName)
                },
                gameId = skatGame.id,
            )
        }
    }
}

