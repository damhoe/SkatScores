package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import java.util.UUID
import javax.inject.Inject

class DeleteSkatGameUseCase @Inject constructor(
    private val repository: SkatGamesRepository,
)
{
    suspend operator fun invoke(id: UUID): Result<Unit> = repository.delete(id)
}
