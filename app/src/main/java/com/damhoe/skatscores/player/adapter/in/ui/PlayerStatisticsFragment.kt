package com.damhoe.skatscores.player.adapter.in.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.skatscores.R;
import com.damhoe.skatscores.databinding.FragmentPlayerStatisticsBinding;
import com.damhoe.skatscores.shared_ui.utils.InsetsManager;

public class PlayerStatisticsFragment extends Fragment {

    private PlayerStatisticsViewModel viewModel;
    private FragmentPlayerStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(PlayerStatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_player_statistics, container, false);
        View root = binding.getRoot();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        NavigationUI.setupWithNavController(binding.toolbar, findNavController());

        viewModel.getOverallProgress().observe(getViewLifecycleOwner(), progressInfo -> {
            binding.totalLabel.setText(String.valueOf(progressInfo.getGamesCount()));
            binding.totalProgressIndicator.setProgress((int) progressInfo.toPercent());
        });
        viewModel.getThisYearProgress().observe(getViewLifecycleOwner(), progressInfo -> {
            binding.yearLabel.setText(String.valueOf(progressInfo.getGamesCount()));
            binding.yearProgressIndicator.setProgress((int) progressInfo.toPercent());
        });
        viewModel.getThisMonthProgress().observe(getViewLifecycleOwner(), progressInfo -> {
            binding.monthLabel.setText(String.valueOf(progressInfo.getGamesCount()));
            binding.monthProgressIndicator.setProgress((int) progressInfo.toPercent());
        });
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}