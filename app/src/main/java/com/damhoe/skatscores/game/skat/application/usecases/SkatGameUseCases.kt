package com.damhoe.skatscores.game.skat.application.usecases

import javax.inject.Inject

data class SkatGameUseCases @Inject constructor(
    val getSkatGame: GetSkatGameUseCase,
    val deleteSkatGame: DeleteSkatGameUseCase,
    val createSkatGame: CreateSkatGameUseCase,
    val addScoreToSkatGame: AddScoreToSkatGameUseCase,
    val updateScore: UpdateScoreUseCase,
    // No deleteScore here on purpose: a round is only ever removed through removeLastScore,
    // which reaches DeleteScoreUseCase itself. Exposing it would hand every caller a
    // delete-any-round door that the UI does not have.
    val removeLastScore: RemoveLastScoreUseCase,
    val updateSkatGame: UpdateSkatGameUseCase,
    val updateSkatParticipants: UpdateSkatParticipantsUseCase,
)

data class GameUseCases @Inject constructor(
    val getGamePreviews: GetGamePreviewsUseCase,
)