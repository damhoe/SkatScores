package com.damhoe.skatscores.player.adapter.`in`.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.damhoe.skatscores.player.application.ports.`in`.GetPlayerStatisticsUseCase
import javax.inject.Inject

class PlayerStatisticsViewModelFactory @Inject constructor(
    private val statisticsUseCase: GetPlayerStatisticsUseCase
) : ViewModelProvider.Factory
{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T
    {
        if (modelClass != PlayerStatisticsViewModel::class.java)
        {
            throw IllegalArgumentException("PlayerStatisticsViewModel class expected.")
        }
        return PlayerStatisticsViewModel(statisticsUseCase) as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T
    {
        if (modelClass != PlayerStatisticsViewModel::class.java)
        {
            throw IllegalArgumentException("PlayerStatisticsViewModel class expected.")
        }
        return PlayerStatisticsViewModel(statisticsUseCase) as T
    }
}