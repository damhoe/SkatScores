package com.damhoe.skatscores;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

   private final Application mApplication;

   @Singleton
   protected ApplicationModule(Application app) {
      mApplication = app;
   }

   @Provides
   @ApplicationContext
   Context provideAppContext() {
      return mApplication;
   }

   @Provides
   Context provideContext(Context context) { return mApplication; }

   @Provides
   @DatabaseInfo
   String provideDatabaseName() {
      return "skat_scores_app.db";
   }

   @Provides
   @DatabaseInfo
   Integer provideDatabaseVersion() {
      return 3;
   }
}
