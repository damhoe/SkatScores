package com.damhoe.skatscores

import com.damhoe.skatscores.persistence.DatabaseConstants
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
    fun provideDatabaseName() = DatabaseConstants.DATABASE_NAME

    @Provides
    @DatabaseInfo
    fun provideDatabaseVersion() = 1
}
