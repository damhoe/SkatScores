package com.damhoe.skatscores

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule @Singleton constructor(private val mApplication: Application) {
    @Provides
    @ApplicationContext
    fun provideAppContext(): Context {
        return mApplication
    }

    @Provides
    fun provideContext(context: Context): Context = mApplication

    @Provides
    @DatabaseInfo
    fun provideDatabaseName() = "skat_scores_app.db"

    @Provides
    @DatabaseInfo
    fun provideDatabaseVersion() = 3

}
