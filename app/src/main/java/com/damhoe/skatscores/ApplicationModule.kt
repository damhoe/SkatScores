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

    /** 2 added the Doppelkopf tables; the upgrade only creates what is missing. */
    @Provides
    @DatabaseInfo
    fun provideDatabaseVersion() = 2
}
