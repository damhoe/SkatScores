package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import java.util.UUID
import javax.inject.Inject

data class UpdateSkatGameCommand(
    val id: UUID,
    val title: Title,
    val settings: SkatSettings
)

class UpdateSkatGameUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository,
)
{
    suspend operator fun invoke(command: UpdateSkatGameCommand): Result<Unit> =
        skatGameRepository.updateSettings(command.id, command.title, command.settings)
}
