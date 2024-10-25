package com.damhoe.skatscores.game.domain.skat.application.ports

import androidx.lifecycle.LiveData
import com.damhoe.skatscores.base.Result
import com.damhoe.skatscores.game.domain.skat.SkatGame
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview
import java.util.Date

interface LoadSkatGameUseCase
{
    fun getGame(id: Long): Result<SkatGame>

    fun getGamesSince(oldest: Date): LiveData<List<SkatGamePreview>>

    val unfinishedGames: LiveData<List<SkatGamePreview>>

    fun refreshDataFromRepository()
}
