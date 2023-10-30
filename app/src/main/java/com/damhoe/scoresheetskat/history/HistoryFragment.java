package com.damhoe.scoresheetskat.history;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
        return binding.getRoot();
    }

    @Override
    protected String title() {
        return getString(R.string.title_history);
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Override
    protected boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}