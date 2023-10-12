package com.damhoe.scoresheetskat;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;
import com.damhoe.scoresheetskat.shared_ui.behaviors.CollapseAppBarBehavior;

public class MainActivity extends AppCompatActivity {

    public ApplicationComponent appComponent;
    public ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appComponent = ((ScoreSheetSkatApplication) getApplicationContext()).appComponent;
        appComponent.inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_statistics,
                R.id.navigation_history,
                R.id.navigation_players
        ).build();
        setSupportActionBar(binding.toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.toolbarLayout, binding.toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController,
                                             @NonNull NavDestination navDestination,
                                             @Nullable Bundle bundle) {
                // Ignore.
            }
        });
    }

    public void disableCollapsingToolbar() {
        binding.appbarLayout.setExpanded(false, false);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) binding.appbarLayout.getLayoutParams();
        CollapseAppBarBehavior behavior = (CollapseAppBarBehavior) layoutParams.getBehavior();
        if (behavior != null)
            behavior.setShouldCollapse(false);
    }

    public void enableCollapsingToolbar() {
        binding.appbarLayout.setExpanded(true, false);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) binding.appbarLayout.getLayoutParams();
        CollapseAppBarBehavior behavior = (CollapseAppBarBehavior) layoutParams.getBehavior();
        if (behavior != null)
            behavior.setShouldCollapse(true);
    }

    public void setBottomNavigationVisibility(int visibility) {
        binding.navView.setVisibility(visibility);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}