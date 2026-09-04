package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DeleteSkatGameUseCase @Inject constructor(
    private val repository: SkatGamesRepository,
)
{
    suspend operator fun invoke(id: UUID): Result<SkatGame?>
    {
        return withContext(Dispatchers.IO) {
            repository.delete(id)
        }
    }
}