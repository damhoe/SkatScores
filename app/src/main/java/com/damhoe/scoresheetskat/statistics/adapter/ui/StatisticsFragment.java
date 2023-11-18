package com.damhoe.scoresheetskat.statistics.adapter.ui;

import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentStatisticsBinding;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;
    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();
        addMenu();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

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

    private void addMenu() {
        /*
         * Bottom app bar menu
         */
        binding.toolbar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
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