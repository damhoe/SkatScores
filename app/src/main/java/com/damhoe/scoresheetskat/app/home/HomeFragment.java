package com.damhoe.scoresheetskat.app.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentHomeBinding;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.damhoe.scoresheetskat.shared_ui.base.TopLevelFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import javax.inject.Inject;

public class HomeFragment extends TopLevelFragment implements GamePreviewItemClickListener{

    @Inject
    HomeViewModelFactory factory;
    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private GamePreviewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNewGameButton = true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((MainActivity)requireActivity()).appComponent.inject(this);
        super.onAttach(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        contentLayout = binding.getRoot();

        // Set up recycler view
        adapter = new GamePreviewAdapter(this);
        binding.openGamesRecycler.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.openGamesRecycler.setLayoutManager(layoutManager);
        binding.openGamesRecycler.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));
        setupNewButton();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        addMenu();
        super.onViewCreated(view, savedInstanceState);

        viewModel.getNumberOfGames().observe(getViewLifecycleOwner(), ints -> {
            binding.totalLabel.setText(String.valueOf(ints.first));
            binding.totalProgressIndicator.setProgress(
                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
        });
        viewModel.getNumberOfGamesYear().observe(getViewLifecycleOwner(), ints -> {
            binding.yearLabel.setText(String.valueOf(ints.first));
            binding.yearProgressIndicator.setProgress(
                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
        });
        viewModel.getNumberOfGamesMonth().observe(getViewLifecycleOwner(), ints -> {
            binding.monthLabel.setText(String.valueOf(ints.first));
            binding.monthProgressIndicator.setProgress(
                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
        });
        viewModel.getOpenGames().observe(getViewLifecycleOwner(), gamePreviews -> {
            if (gamePreviews.size() == 0) {
                binding.welcomeText.setText(getString(R.string.welcome_text));
                return;
            }
            binding.welcomeText.setText(getString(R.string.welcome_text_open_games));

            // Refill adapter
            adapter.setGamePreviews(gamePreviews);
            binding.openGamesRecycler.invalidate();
        });
    }

    private void addMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_settings) {
                    findNavController().navigate(R.id.navigation_settings);
                    return true;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupNewButton() {
        ExtendedFloatingActionButton fab = requireActivity().findViewById(R.id.new_game_button);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_new_game);
            });
        }
    }

    @Override
    public void notifyItemClicked(GamePreview gamePreview) {
        // Navigate to game using gameId
        Snackbar.make(requireView(),
                "Navigate to game with id: " + gamePreview.getGameId(),
                Snackbar.LENGTH_SHORT).show();
    }
}