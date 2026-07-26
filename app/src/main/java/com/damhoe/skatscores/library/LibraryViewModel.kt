package com.damhoe.skatscores.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.skat.application.usecases.DeleteSkatGameUseCase
import com.damhoe.skatscores.game.skat.application.usecases.GetGamePreviewsUseCase
import com.damhoe.skatscores.game.skat.domain.GamePreviewFilter
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getGamePreviewsUseCase: GetGamePreviewsUseCase,
    private val deleteSkatGameUseCase: DeleteSkatGameUseCase
) : ViewModel()
{
    val games: LiveData<List<SkatGamePreview>> =
        getGamePreviewsUseCase(GamePreviewFilter.All)

    fun deleteGame(id: UUID) = viewModelScope.launch {
        deleteSkatGameUseCase(id)
    }
}