package com.damhoe.skatscores.game.skat.application.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.GamePreviewFilter
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import java.time.Instant
import javax.inject.Inject

class GetGamePreviewsUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository
)
{
    operator fun invoke(filter: GamePreviewFilter): LiveData<List<SkatGamePreview>>
    {
        return when (filter)
        {
            is GamePreviewFilter.All -> skatGameRepository
                .getAll()
                .asLiveData()

            is GamePreviewFilter.SinceDate -> skatGameRepository
                .getAllSince(filter.date)
                .asLiveData()
        }
    }
}