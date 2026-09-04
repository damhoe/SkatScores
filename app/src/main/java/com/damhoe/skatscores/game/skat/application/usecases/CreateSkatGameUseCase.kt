package com.damhoe.skatscores.game.skat.application.usecases

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayerCount
import com.damhoe.skatscores.game.skat.domain.SkatPlayers
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.util.UUID
import javax.inject.Inject

@Parcelize
data class CreateSkatGameCommand(
    val settings: SkatSettings,
    val title: Title,
    val playerCount: SkatPlayerCount,
    /** Who sits at the table. Null means start with placeholder guests. */
    val participants: SkatParticipants? = null,
) : Parcelable

class CreateSkatGameUseCase @Inject constructor(
    private val repository: SkatGamesRepository
)
{
    suspend operator fun invoke(command: CreateSkatGameCommand): Result<SkatGame>
    {
        return withContext(Dispatchers.IO) {
            require(command.playerCount == SkatPlayerCount.THREE_PLAYERS) {
                "Invalid skat player count ${command.playerCount}"
            }

            val participants = command.participants ?: SkatParticipants.createNew()

            val skatGame = SkatGame.create(
                command.title,
                command.settings,
                participants
            )

            repository.save(skatGame).map { skatGame }
        }
    }
}