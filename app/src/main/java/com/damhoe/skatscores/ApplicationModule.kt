package com.damhoe.skatscores

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object ApplicationModule
{
    @Provides
    @DatabaseInfo
    fun provideDatabaseName() = "skat_scores_app.db"

    @Provides
    @DatabaseInfo
    fun provideDatabaseVersion() = 3
}
