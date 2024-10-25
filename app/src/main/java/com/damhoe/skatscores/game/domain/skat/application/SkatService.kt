package com.damhoe.skatscores.game.domain.skat.application

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.damhoe.skatscores.game.domain.score.SkatScore
import com.damhoe.skatscores.game.domain.skat.SkatGame
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview
import com.damhoe.skatscores.game.domain.skat.SkatPlayers
import com.damhoe.skatscores.game.domain.skat.SkatSettings
import com.damhoe.skatscores.game.domain.skat.application.ports.AddScoreToGameUseCase
import com.damhoe.skatscores.game.domain.skat.application.ports.CouldNotFindSkatGame
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase
import com.damhoe.skatscores.game.domain.skat.application.ports.InvalidSkatSettings
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase
import java.util.Date
import java.util.UUID

class SkatService(
    override val unfinishedGames: LiveData<List<SkatGamePreview>>) :
    CrudSkatGameUseCase,
    AddScoreToGameUseCase,
    LoadSkatGameUseCase
{
    fun dasdo(players: SkatPlayers.ThreeSkatPlayers) : Any
    {
        var x : SkatPlayers = players
    }

    override fun create(command: SkatGameCommand): Result<SkatGame>
    {
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID): Either<CouldNotFindSkatGame, SkatGame>
    {
        TODO("Not yet implemented")
    }

    override fun updateSettings(
        id: UUID,
        settings: SkatSettings
    ): Either<InvalidSkatSettings, SkatSettings>
    {
        TODO("Not yet implemented")
    }

    override fun addScoreToGame(skatGame: SkatGame?, score: SkatScore?)
    {
        TODO("Not yet implemented")
    }

    override fun removeLastScore(skatGame: SkatGame?): com.damhoe.skatscores.base.Result<SkatScore?>?
    {
        TODO("Not yet implemented")
    }

    override fun getGame(id: Long): com.damhoe.skatscores.base.Result<SkatGame>
    {
        TODO("Not yet implemented")
    }

    override fun getGamesSince(oldest: Date): LiveData<List<SkatGamePreview>>
    {
        TODO("Not yet implemented")
    }

    override fun refreshDataFromRepository()
    {
        TODO("Not yet implemented")
    }
}