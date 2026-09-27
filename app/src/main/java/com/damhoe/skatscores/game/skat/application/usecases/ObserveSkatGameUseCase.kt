package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * A list as it is now and after every write to it - a round saved, undone or edited, the
 * settings or seats changed - so a screen never works from a copy that is out of date.
 * Null once the list is gone.
 */
class ObserveSkatGameUseCase @Inject constructor(
    private val repository: SkatGamesRepository,
)
{
    operator fun invoke(id: UUID): Flow<SkatGame?> = repository.observe(id)
}
