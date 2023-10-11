package com.damhoe.scoresheetskat.history;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentHistoryBinding;
import com.damhoe.scoresheetskat.shared_ui.base.TopLevelFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class HistoryFragment extends TopLevelFragment {

    private HistoryViewModel mViewModel;
    private FragmentHistoryBinding binding;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);

        View root = binding.getRoot();

        contentLayout = root;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNewGameButton = true;
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    public void setupNewButton() {
        ExtendedFloatingActionButton fab = requireActivity().findViewById(R.id.new_game_button);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                findNavController().navigate(R.id.action_navigation_history_to_navigation_new_game);
            });
        }
    }
}