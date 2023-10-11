package com.damhoe.scoresheetskat.statistics.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        statisticsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        contentLayout = root;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
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
                findNavController().navigate(R.id.action_navigation_statistics_to_navigation_new_game);
            });
        }
    }
}