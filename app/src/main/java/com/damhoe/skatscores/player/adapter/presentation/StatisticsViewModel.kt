package com.damhoe.skatscores.player.adapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.player.application.usecases.GetPlayerStatisticsUseCase
import com.damhoe.skatscores.player.domain.PlayerStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPlayerStatisticsUseCase: GetPlayerStatisticsUseCase
) : ViewModel()
{

}