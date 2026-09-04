package com.damhoe.skatscores.game

sealed class PlayerSelectionValidationResult {
    data object Success : PlayerSelectionValidationResult()
    data object NewPlayer : PlayerSelectionValidationResult()
    data object EmptyName : PlayerSelectionValidationResult()
    data object DuplicateName : PlayerSelectionValidationResult()
}