package com.damhoe.skatscores.game.skat.application.usecases

import javax.inject.Inject

data class SkatGameUseCases @Inject constructor(
    val getSkatGame: GetSkatGameUseCase,
    val deleteSkatGame: DeleteSkatGameUseCase,
    val createSkatGame: CreateSkatGameUseCase,
    val addScoreToSkatGame: AddScoreToSkatGameUseCase,
    val removeLastScore: RemoveLastScoreUseCase,
    val refreshAllUseCase: RefreshAllUseCase,
    val updateSkatGame: UpdateSkatGameUseCase,
    val updateSkatParticipants: UpdateSkatParticipantsUseCase,
)

data class GameUseCases @Inject constructor(
    val getGamePreviews: GetGamePreviewsUseCase,
)