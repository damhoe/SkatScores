package com.damhoe.skatscores.player

import com.damhoe.skatscores.player.adapter.persistence.PlayerRepositoryImpl
import com.damhoe.skatscores.player.adapter.persistence.PlayerStatisticsRepositoryImpl
import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@dagger.Module
abstract class PlayerModule
{
    @Binds
    abstract fun bindPlayersRepository(
        impl: PlayerRepositoryImpl
    ): PlayersRepository

    @Binds
    abstract fun bindGetPlayerStatisticsPort(
        impl: PlayerStatisticsRepositoryImpl
    ): PlayerStatisticsRepository
}