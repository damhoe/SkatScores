package com.damhoe.skatscores.game.domain.skat.application.ports

import arrow.core.Either
import com.damhoe.skatscores.game.domain.skat.SkatGame
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand
import com.damhoe.skatscores.game.domain.skat.SkatGameLegacy
import com.damhoe.skatscores.game.domain.skat.SkatSettings
import java.util.UUID

interface CrudSkatGameUseCase
{
    fun create(command: SkatGameCommand) : Result<SkatGameLegacy>

    fun delete(id: UUID) : Either<CouldNotFindSkatGame, SkatGame>

    fun updateSettings(id: UUID, settings: SkatSettings) : Either<InvalidSkatSettings, SkatSettings>
}

data class CouldNotFindSkatGame(
    val id: UUID)

class InvalidSkatSettings
