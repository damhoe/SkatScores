package com.damhoe.scoresheetskat;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ConfigurationHelper;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.damhoe.scoresheetskat.app.app_settings.ConfigManager;
import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        ConfigManager configManager = new ConfigManager(this);

        // UI mode
        String uiMode = sharedPreferences.getString("ui_mode", "");
        //configManager.setUiMode(uiMode);

        // System Locale
        String language = sharedPreferences.getString("language", "");
        Locale.setDefault(Locale.forLanguageTag(language));
        //configManager.setLocale(language);
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