package com.damhoe.skatscores.game.application

sealed class PlayerSelectionValidationResult {
    data object Success : PlayerSelectionValidationResult()
    data object NewPlayer : PlayerSelectionValidationResult()
    data object EmptyName : PlayerSelectionValidationResult()
    data object DuplicateName : PlayerSelectionValidationResult()
}