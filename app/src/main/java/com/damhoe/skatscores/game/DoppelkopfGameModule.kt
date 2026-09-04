package com.damhoe.skatscores.game

import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.DoppelkopfGamesRepositoryImpl
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScoreRepositoryImpl
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfGamesRepository
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class DoppelkopfGameModule
{
    @Binds
    abstract fun bindDoppelkopfGamesRepository(
        impl: DoppelkopfGamesRepositoryImpl
    ): DoppelkopfGamesRepository

    @Binds
    abstract fun bindDoppelkopfScoresRepository(
        impl: DoppelkopfScoreRepositoryImpl
    ): DoppelkopfScoresRepository
}
