package com.damhoe.scoresheetskat.app.help;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentHelpBinding;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;


public class HelpFragment extends Fragment {

    FragmentHelpBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.content);

        AppBarConfiguration appBarConfiguration =
                ((MainActivity)requireActivity()).getAppBarConfiguration();
        NavigationUI.setupWithNavController(binding.toolbar, findNavController(), appBarConfiguration);
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }
}