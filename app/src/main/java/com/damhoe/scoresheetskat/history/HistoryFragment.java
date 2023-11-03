package com.damhoe.scoresheetskat.history;

import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentHistoryBinding;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewItemClickListener;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.damhoe.scoresheetskat.game.adapter.in.ui.shared.GamePreviewAdapter;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;

import java.util.List;

import javax.inject.Inject;

public class HistoryFragment extends Fragment implements GamePreviewItemClickListener {

    @Inject
    HistoryViewModelFactory factory;
    private HistoryViewModel mViewModel;
    private FragmentHistoryBinding binding;
    private GamePreviewAdapter lastMonthGamesAdapter;
    private GamePreviewAdapter oldGamesAdapter;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity)requireActivity()).appComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, factory).get(HistoryViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_history, container, false);

        // Setup last month games list
        lastMonthGamesAdapter = new GamePreviewAdapter(this);
        binding.monthlyGamesRv.setAdapter(lastMonthGamesAdapter);
        binding.monthlyGamesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.monthlyGamesRv.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));

        // Setup old games list
        oldGamesAdapter = new GamePreviewAdapter(this);
        binding.olderGamesRv.setAdapter(oldGamesAdapter);
        binding.olderGamesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.olderGamesRv.addItemDecoration(new GamePreviewAdapter.ItemDecoration(16));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        // Add live data observers
        mViewModel.getLastMonthGames().observe(getViewLifecycleOwner(),
                new Observer<List<GamePreview>>() {
            @Override
            public void onChanged(List<GamePreview> gamePreviews) {
                lastMonthGamesAdapter.setGamePreviews(gamePreviews);
                binding.monthlyGamesRv.invalidate();
            }
        });

        // Add live data observers
        mViewModel.getOldGames().observe(getViewLifecycleOwner(),
                new Observer<List<GamePreview>>() {
                    @Override
                    public void onChanged(List<GamePreview> gamePreviews) {
                        oldGamesAdapter.setGamePreviews(gamePreviews);
                        binding.olderGamesRv.invalidate();
                    }
                });

        addMenu();
    }

    private void addMenu() {
        /*
         * Bottom app bar menu
         */
        binding.bottomAppBar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.history_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.home) {
                    findNavController().navigateUp();
                    return true;
                } else if (menuItem.getItemId() == R.id.players) {
                    findNavController().navigate(R.id.action_history_to_players);
                } else if (menuItem.getItemId() == R.id.statistics) {
                    // findNavController().navigate(R.id.); // TODO
                }
                return false;
            }
        });
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void notifyItemClicked(GamePreview gamePreview) {

    }
}