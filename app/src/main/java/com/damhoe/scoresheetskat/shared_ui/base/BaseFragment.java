package com.damhoe.scoresheetskat.shared_ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.databinding.ActivityMainBinding;

public abstract class BaseFragment extends Fragment {

    protected int bottomNavigationVisibility = View.VISIBLE;
    protected boolean showStartGameButton = false;
    protected boolean showNewGameButton = false;
    protected boolean showEditScoreButton = false;
    protected boolean showScoreDoneButton = false;
    protected boolean showAddPlayerButton = false;

    private ActivityMainBinding mainBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainBinding = mainActivity.binding;
            mainActivity.setBottomNavigationVisibility(bottomNavigationVisibility);

            if (showNewGameButton) {
                mainBinding.newGameButton.show();
            }
            if (showEditScoreButton) {
                mainBinding.editScoreButton.show();
            }
            if (showScoreDoneButton) {
                mainBinding.scoreDoneButton.show();
            }
            if (showStartGameButton) {
                mainBinding.startButton.show();
            }
            if (showAddPlayerButton) {
                mainBinding.addPlayerButton.show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (showNewGameButton) {
            mainBinding.newGameButton.hide();
        }
        if (showEditScoreButton) {
            mainBinding.editScoreButton.hide();
        }
        if (showScoreDoneButton) {
            mainBinding.scoreDoneButton.hide();
        }
        if (showStartGameButton) {
            mainBinding.startButton.hide();
        }
        if (showAddPlayerButton) {
            mainBinding.addPlayerButton.hide();
        }
    }
}
