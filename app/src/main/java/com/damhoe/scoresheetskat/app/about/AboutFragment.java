package com.damhoe.scoresheetskat.app.about;

import android.os.Bundle;

import androidx.annotation.ColorInt;
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

import com.damhoe.scoresheetskat.DatabaseInfo;
import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentAboutBinding;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.google.android.material.color.MaterialColors;


public class AboutFragment extends Fragment {

    FragmentAboutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_about, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.content);

        // Setup back navigation
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                ((MainActivity)requireActivity()).getAppBarConfiguration();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
    }
}