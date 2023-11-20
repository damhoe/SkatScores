package com.damhoe.scoresheetskat.library;

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
import com.damhoe.scoresheetskat.databinding.FragmentLibraryBinding;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewAdapter;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewItemClickListener;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.damhoe.scoresheetskat.shared_ui.utils.LayoutMargins;

import javax.inject.Inject;

public class LibraryFragment extends Fragment implements GamePreviewItemClickListener {

    @Inject
    LibraryViewModelFactory factory;
    private LibraryViewModel viewModel;
    private FragmentLibraryBinding binding;
    private GamePreviewAdapter gamesThisMonthAdapter;
    private GamePreviewAdapter gamesOldAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, factory).get(LibraryViewModel.class);
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
                R.layout.fragment_library, container, false);

        // Setup recycler view
        gamesThisMonthAdapter = new GamePreviewAdapter(this);
        binding.thisMonthGamesRv.setAdapter(gamesThisMonthAdapter);
        binding.thisMonthGamesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.thisMonthGamesRv.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));
        gamesThisMonthAdapter.setGamePreviews(viewModel.getLastMonthGames().getValue());

        // Setup recycler view
        gamesOldAdapter = new GamePreviewAdapter(this);
        binding.olderGamesRv.setAdapter(gamesOldAdapter);
        binding.olderGamesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.olderGamesRv.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));
        gamesOldAdapter.setGamePreviews(viewModel.getOldGames().getValue());

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

        // Load default button margins for resource values
        int bottomMargin =
                (int) getResources().getDimension(R.dimen.fab_above_bottom_nav_margin_bottom);
        int rightMargin =
                (int) getResources().getDimension(R.dimen.fab_margin_right);
        LayoutMargins margins = new LayoutMargins(0, 0, rightMargin, bottomMargin);
        InsetsManager.applyNavigationBarInsets(binding.addButton, margins);


        viewModel.getUnfinishedGames().observe(getViewLifecycleOwner(), gamePreviews -> {
            // Ignore.
        });

        // Add live data observers
        viewModel.getLastMonthGames().observe(getViewLifecycleOwner(), skatGamePreviews -> {
            boolean showNoGamesInfo = skatGamePreviews.size() == 0;
            binding.thisMonthNoGames.setVisibility(showNoGamesInfo ? View.VISIBLE : View.GONE);

            gamesThisMonthAdapter.setGamePreviews(skatGamePreviews);
            binding.thisMonthGamesRv.invalidate();
        });

        // Add live data observers
        viewModel.getOldGames().observe(getViewLifecycleOwner(), skatGamePreviews -> {
            boolean showNoGamesInfo = skatGamePreviews.size() == 0;
            binding.olderNoGames.setVisibility(showNoGamesInfo ? View.VISIBLE : View.GONE);

            gamesOldAdapter.setGamePreviews(skatGamePreviews);
            binding.olderGamesRv.invalidate();
        });

        addMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupNewGameButton() {
        binding.addButton.setOnClickListener(view -> navigateToGameSetup());

        // Hide and show FAB depending on scroll events
    }

    private void navigateToGameSetup() {
        LibraryFragmentDirections.ActionLibraryToGameActivity directions =
                LibraryFragmentDirections.actionLibraryToGameActivity();
        findNavController().navigate(directions);
    }

    private void navigateToGame(long gameId) {
        LibraryFragmentDirections.ActionLibraryToGameActivity directions =
                LibraryFragmentDirections.actionLibraryToGameActivity();
        directions.setGameId(gameId);
        findNavController().navigate(directions);
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
                    findNavController().navigate(R.id.action_library_to_settings);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void notifySelect(SkatGamePreview skatGamePreview) {
        // Navigate to game using gameId
        navigateToGame(skatGamePreview.getGameId());
    }

    @Override
    public void notifyDelete(SkatGamePreview skatGamePreview) {
        Result<SkatGame> result = viewModel.deleteGame(skatGamePreview.getGameId());

        if (result.isFailure()) {
            Log.d("Unexpected behavior", result.message);
        }
    }
}