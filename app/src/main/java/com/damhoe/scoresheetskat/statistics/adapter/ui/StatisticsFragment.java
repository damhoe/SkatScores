package com.damhoe.scoresheetskat.statistics.adapter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentStatisticsBinding;
import com.damhoe.scoresheetskat.shared_ui.base.TopLevelFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class StatisticsFragment extends TopLevelFragment {

    private StatisticsViewModel statisticsViewModel;
    private FragmentStatisticsBinding binding;

    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        viewModel.getNumberOfGames().observe(getViewLifecycleOwner(), ints -> {
//            binding.totalLabel.setText(String.valueOf(ints.first));
//            binding.totalProgressIndicator.setProgress(
//                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
//        });
//        viewModel.getNumberOfGamesYear().observe(getViewLifecycleOwner(), ints -> {
//            binding.yearLabel.setText(String.valueOf(ints.first));
//            binding.yearProgressIndicator.setProgress(
//                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
//        });
//        viewModel.getNumberOfGamesMonth().observe(getViewLifecycleOwner(), ints -> {
//            binding.monthLabel.setText(String.valueOf(ints.first));
//            binding.monthProgressIndicator.setProgress(
//                    ints.first != null ? (int) (100. * (1. - ints.second * 1./ ints.first)) : 0);
//        });
    }

    @Override
    protected String title() {
        return getString(R.string.title_statistics);
    }

    @Override
    protected boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}