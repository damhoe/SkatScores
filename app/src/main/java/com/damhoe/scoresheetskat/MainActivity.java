package com.damhoe.scoresheetskat;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public ApplicationComponent appComponent;
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize dependency injection with dagger
        appComponent = ((ScoreSheetSkatApplication) getApplicationContext()).appComponent;
        appComponent.inject(this);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
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