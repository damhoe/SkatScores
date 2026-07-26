package com.damhoe.skatscores.game

import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesRepositoryImpl
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreRepositoryImpl
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class SkatGameModule
{
    @Binds
    abstract fun bindGameRepository(
        skatGamesRepositoryImpl: SkatGamesRepositoryImpl
    ): SkatGamesRepository

    @Binds
    abstract fun bindScoresRepository(
        skatScoreRepositoryImpl: SkatScoreRepositoryImpl
    ): SkatScoresRepository
}