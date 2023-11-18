package com.damhoe.scoresheetskat;

import android.app.Application;

public class ScoreSheetSkatApplication extends Application {
   protected ApplicationComponent appComponent;

   @Override
   public void onCreate() {
      super.onCreate();
      appComponent = DaggerApplicationComponent
              .builder()
              .applicationModule(new ApplicationModule(this))
              .build();
   }

   public ApplicationComponent getAppComponent() {
      return appComponent;
   }
}
