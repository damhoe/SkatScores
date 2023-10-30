package com.damhoe.scoresheetskat.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentHomeBinding;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.damhoe.scoresheetskat.shared_ui.base.TopLevelFragment;
import com.google.android.material.snackbar.Snackbar;

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
    protected View onCreateContentView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home, container, false);

        // Setup recycler view
        adapter = new GamePreviewAdapter(this);
        binding.openGamesRecycler.setAdapter(adapter);
        binding.openGamesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.openGamesRecycler.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));
        return binding.getRoot();
    }

    @Override
    protected String title() {
        return getString(R.string.title_home);
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //NavigationUI.setupWithNavController(binding.toolbar, findNavController());
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

    @Override
    protected boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_settings) {
            findNavController().navigate(R.id.action_home_to_settings);
            return true;
        }
        if (menuItem.getItemId() == R.id.menu_about) {
            findNavController().navigate(R.id.action_home_to_about);
            return true;
        }
        if (menuItem.getItemId() == R.id.menu_help) {
            findNavController().navigate(R.id.action_home_to_help);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupNewGameButton() {
        baseBinding.addButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        findNavController().navigate(R.id.action_home_to_game_setup);
                    }
                }
        );

        // Hide and show FAB depending on scroll events

    }

    @Override
    public void notifyItemClicked(GamePreview gamePreview) {
        // Navigate to game using gameId
        Snackbar.make(requireView(),
                "Navigate to game with id: " + gamePreview.getGameId(),
                Snackbar.LENGTH_SHORT).show();
    }
}