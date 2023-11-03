package com.damhoe.scoresheetskat.statistics.adapter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentStatisticsBinding;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}