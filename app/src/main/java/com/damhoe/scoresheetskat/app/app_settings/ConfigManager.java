package com.damhoe.scoresheetskat.app.app_settings;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.damhoe.scoresheetskat.R;

import java.util.Locale;

public class ConfigManager {
   private final Activity mActivity;

   public ConfigManager(Activity activity) {
      mActivity = activity;
   }

   public void setLocale(String language) {
      String english = mActivity.getString(R.string.pref_language_english_value);
      String german = mActivity.getString(R.string.pref_language_german_value);

      String tag = "";
      if (language.equals(english)) {
         tag = "en";
      } else if (language.equals(german)) {
         tag = "de";
      }

      String currentLanguage = Locale.getDefault().getLanguage();
      if (!currentLanguage.equals(language)) {
         mActivity.runOnUiThread(() -> {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
            //AppCompatDelegate.setApplicationLocales(appLocale);
         });
      }
   }

   public void setUiMode(String uiMode) {
      String dayUiMode = mActivity.getString(R.string.pref_uiMode_day_value);
      String nightUiMode = mActivity.getString(R.string.pref_uiMode_night_value);

      int mode;
      if (uiMode.equals(dayUiMode)) {
         mode = AppCompatDelegate.MODE_NIGHT_NO;
      } else if (uiMode.equals(nightUiMode)) {
         mode = AppCompatDelegate.MODE_NIGHT_YES;
      } else { // R.string.pref_uiMode_system_value
         mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
      }

      if (mode != AppCompatDelegate.getDefaultNightMode()) {
         AppCompatDelegate.setDefaultNightMode(mode);
      }
   }
}
