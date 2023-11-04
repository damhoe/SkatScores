package com.damhoe.scoresheetskat.app.app_settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.damhoe.scoresheetskat.R;

public class SettingsContentFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

   public static SettingsContentFragment newInstance() {
      return new SettingsContentFragment();
   }

   @Override
   public void onCreatePreferences(@Nullable Bundle savedInstanceState,
                                   @Nullable String rootKey) {
      setPreferencesFromResource(R.xml.preferences, rootKey);
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                         @Nullable String key) {
      Log.d("Preference action", "Change preference value: " + key);

      if (key == null) {
         return;
      }

      ConfigManager configManager = new ConfigManager(requireActivity());

      switch (key) {
         case "ui_mode":
            String uiMode = sharedPreferences.getString(key, "");
            configManager.setUiMode(uiMode);
            break;
         case "language":
            String language = sharedPreferences.getString(key, "");
            configManager.setLocale(language);
            break;
      }
   }

   @Override
   public void onResume() {
      super.onResume();
      PreferenceManager.getDefaultSharedPreferences(requireContext())
              .registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   public void onPause() {
      super.onPause();
      PreferenceManager.getDefaultSharedPreferences(requireContext())
              .unregisterOnSharedPreferenceChangeListener(this);
   }
}
