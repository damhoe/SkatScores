package com.damhoe.scoresheetskat.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.databinding.FragmentHomeBinding;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewAdapter;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewItemClickListener;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;

import javax.inject.Inject;

public class HomeFragment extends Fragment implements GamePreviewItemClickListener {

    @Inject
    HomeViewModelFactory factory;
    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private GamePreviewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((MainActivity)requireActivity()).appComponent.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupNewGameButton();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home, container, false);

        // Setup recycler view
        adapter = new GamePreviewAdapter(this);
        binding.openGamesRecycler.setAdapter(adapter);
        binding.openGamesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.openGamesRecycler.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));
        adapter.setGamePreviews(viewModel.getUnfinishedGames().getValue());
        return binding.getRoot();
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set insets to account for status and navigation bars
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        //NavigationUI.setupWithNavController(binding.toolbar, findNavController());
        viewModel.getUnfinishedGames().observe(getViewLifecycleOwner(), gamePreviews -> {
            if (gamePreviews.size() == 0) {
                binding.welcomeText.setText(getString(R.string.welcome_text));
                return;
            }
            binding.welcomeText.setText(getString(R.string.welcome_text_open_games));

            // Refill adapter
            adapter.setGamePreviews(gamePreviews);
            binding.openGamesRecycler.invalidate();
        });

        addMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupNewGameButton() {
        binding.addButton.setOnClickListener(
                view -> findNavController().navigate(R.id.action_home_to_game_setup)
        );

        // Hide and show FAB depending on scroll events
    }

    private void addMenu() {
        /*
         * Toolbar menu
         */
        binding.toolbar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_settings) {
                    findNavController().navigate(R.id.action_home_to_settings);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        /*
         * Bottom app bar menu
         */
        binding.bottomAppBar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.home_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.players) {
                    findNavController().navigate(R.id.action_home_to_players);
                    return true;
                } else if (menuItem.getItemId() == R.id.history) {
                    findNavController().navigate(R.id.action_home_to_history);
                    return true;
                } else if (menuItem.getItemId() == R.id.statistics) {
                    findNavController().navigate(R.id.action_home_to_statistics);
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_help) {
                    findNavController().navigate(R.id.action_home_to_help);
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_about) {
                    findNavController().navigate(R.id.action_home_to_about);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void notifySelect(SkatGamePreview skatGamePreview) {
        // Navigate to game using gameId
        Bundle bundle = new Bundle();
        bundle.putLong("gameId", skatGamePreview.getGameId());
        findNavController().navigate(R.id.action_home_to_game, bundle);
    }

    @Override
    public void notifyDelete(SkatGamePreview skatGamePreview) {
        Result<SkatGame> result = viewModel.deleteGame(skatGamePreview.getGameId());

        if (result.isFailure()) {
            Log.d("Unexpected behavior", result.getMessage());
        }
    }
}