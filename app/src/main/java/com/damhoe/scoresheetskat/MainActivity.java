package com.damhoe.scoresheetskat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.damhoe.scoresheetskat.app.app_settings.ThemeProvider;
import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject
    public ApplicationComponent appComponent;
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize dependency injection with dagger
        appComponent = ((ScoreSheetSkatApplication) getApplicationContext()).appComponent;
        appComponent.inject(this);

        super.onCreate(savedInstanceState);

        loadSharedPreferences();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        // UI mode
        String theme = sharedPreferences.getString(
                getString(R.string.theme_preference_key),
                getString(R.string.day_theme_preference_value)
        );
        AppCompatDelegate.setDefaultNightMode(new ThemeProvider(this).getTheme(theme));

        // System Locale
        String language = sharedPreferences.getString(
                getString(R.string.language_preference_key),
                getString(R.string.german_preference_value)
        );
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"));

        Log.d("Loaded Preference", "UI mode: " + theme);
        Log.d("Loaded Preference", "Language: " + language);
    }

    public AppBarConfiguration getAppBarConfiguration() {
        return new AppBarConfiguration.Builder(findNavController().getGraph()).build();
    }

    private NavController findNavController() {
        return Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(findNavController(), getAppBarConfiguration())
                || super.onSupportNavigateUp();
    }
}