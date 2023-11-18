package com.damhoe.scoresheetskat.game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.damhoe.scoresheetskat.ApplicationComponent;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.ScoreSheetSkatApplication;
import com.damhoe.scoresheetskat.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {

    private ActivityGameBinding binding;
    private ApplicationComponent appComponent;

    /** @noinspection DataFlowIssue*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appComponent = ((ScoreSheetSkatApplication)getApplicationContext()).getAppComponent();
        appComponent.inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        initializeNavController(getIntent().getExtras());
    }

    public ApplicationComponent getAppComponent() {
        return appComponent;
    }

    /** @noinspection DataFlowIssue*/
    private void initializeNavController(Bundle extras) {
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        long gameId = extras.getLong("gameId", -1L);

        // If game id was passed, open the games fragment
        // with the game id
        if (gameId > 0) {
            navController.setGraph(R.navigation.game_nav_graph, extras);
        } else {
            navController.setGraph(R.navigation.game_nav_graph_full);
        }
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