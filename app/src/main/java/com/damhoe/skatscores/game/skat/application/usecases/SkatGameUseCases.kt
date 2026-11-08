package com.damhoe.skatscores.game.skat.application.usecases

import javax.inject.Inject

data class SkatGameUseCases @Inject constructor(
    val getSkatGame: GetSkatGameUseCase,
    val observeSkatGame: ObserveSkatGameUseCase,
    val deleteSkatGame: DeleteSkatGameUseCase,
    val createSkatGame: CreateSkatGameUseCase,
    val addScoreToSkatGame: AddScoreToSkatGameUseCase,
    val updateScore: UpdateScoreUseCase,
    // Deliberately no delete-by-id: only the newest round can be removed.
    val removeLastScore: RemoveLastScoreUseCase,
    val updateSkatGame: UpdateSkatGameUseCase,
    val updateSkatParticipants: UpdateSkatParticipantsUseCase,
)

data class GameUseCases @Inject constructor(
    val getGamePreviews: GetGamePreviewsUseCase,
)
