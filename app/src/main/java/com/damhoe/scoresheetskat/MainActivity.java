package com.damhoe.scoresheetskat;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public ApplicationComponent appComponent;
    public ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Menu menu;

    /** @noinspection DataFlowIssue*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize dependency injection with dagger
        appComponent = ((ScoreSheetSkatApplication) getApplicationContext()).appComponent;
        appComponent.inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Initialize appBar configuration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_statistics,
                R.id.navigation_history,
                R.id.players
        ).build();
    }

    public AppBarConfiguration getAppBarConfiguration() {
        return appBarConfiguration;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}