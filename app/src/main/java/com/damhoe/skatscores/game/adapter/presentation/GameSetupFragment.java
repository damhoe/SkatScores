package com.damhoe.skatscores.game.adapter.presentation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.damhoe.skatscores.R;
import com.damhoe.skatscores.databinding.FragmentGameSetupBinding;
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand;
import com.damhoe.skatscores.shared_ui.utils.InsetsManager;
import com.damhoe.skatscores.shared_ui.utils.LayoutMargins;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GameSetupFragment extends Fragment {

    private GameSetupViewModel viewModel;
    private FragmentGameSetupBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_game_setup,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        int buttonMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin_bottom);

        LayoutMargins defaultMargins = new LayoutMargins(
                buttonMargin,
                buttonMargin,
                buttonMargin,
                buttonMargin);
        InsetsManager.applyNavigationBarInsets(binding.startButton, defaultMargins);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        // Setup back navigation
        binding.backButton.setOnClickListener(view1 -> requireActivity().finish());

        viewModel = new GameSetupViewModel();
        binding.setViewModel(viewModel);
        viewModel.setTitle(getString(R.string.default_list_title) + createDateId());

        final Observer<Integer> numberOfRoundsObserver = numberOfRounds -> {
            Log.d("Observed LiveData change", "Number of rounds changed to " + numberOfRounds);
            // Update text view
            binding.roundCountText.setText(String.valueOf(numberOfRounds));
        };
        viewModel.getNumberOfRounds().observe(getViewLifecycleOwner(), numberOfRoundsObserver);

        final Observer<Boolean> scoringObserver = isTournamentScoring ->
                Log.d("Observed LiveData change",
                        "IsTournamentScoring changed to " + isTournamentScoring.toString());
        viewModel.isTournamentScoring().observe(getViewLifecycleOwner(), scoringObserver);

        final Observer<Integer> numberOfPlayersObserver = getNumberOfPlayers ->
            Log.d("Observed LiveData change",
                    "Number of players changed to " + getNumberOfPlayers);
        viewModel.getNumberOfPlayers().observe(getViewLifecycleOwner(), numberOfPlayersObserver);

        final Observer<String> listNameObserver = name -> {
            Log.d("Observed LiveData change", "New list name: " + name);
            requireActivity().findViewById(R.id.startButton).setEnabled(!name.isEmpty());
        };
        viewModel.getTitle().observe(getViewLifecycleOwner(), listNameObserver);

        final Observer<SkatGameCommand> commandObserver = skatGameCommand -> {
            Log.d("Observed LiveData change",
                    String.format("SkatGameCommand changed: %s, %d, %d, %b",
                            skatGameCommand.getTitle(),
                            skatGameCommand.getNumberOfPlayers(),
                            skatGameCommand.getSettings().getNumberOfRounds(),
                            skatGameCommand.getSettings().isTournamentScoring()
                    ));
        };
        viewModel.getSkatGameCommand().observe(getViewLifecycleOwner(), commandObserver);

        setUpListNameInput();
        setUpRoundsSeekbar();
        setUpScoringButtons();
        setUpStartButton();
        initializeUI();
    }

    private String createDateId() {
        Locale locale = Locale.getDefault();
        Date date = Calendar.getInstance().getTime();
        if (locale != Locale.GERMAN) {
            return new SimpleDateFormat(" LLL, d", locale).format(date);
        }
        return new SimpleDateFormat(" d LLL", locale).format(date);
    }

    private void setUpStartButton() {
        binding.startButton.setOnClickListener(view -> {
            SkatGameCommand command = viewModel.getSkatGameCommand().getValue();
            Bundle bundle = new Bundle();
            bundle.putParcelable("gameCommand", command);
            findNavController().navigate(R.id.action_new_game_to_game, bundle);
        });
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    private void setUpListNameInput() {
        binding.listNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.updateTitle(editable.toString());
            }
        });
    }

    private void setUpRoundsSeekbar() {
        binding.roundCountSlider.addOnChangeListener((slider, numberOfRounds, fromUser) ->
                viewModel.updateNumberOfRounds((int) numberOfRounds));
    }

    private void setUpScoringButtons() {
        binding.scoringSettingsRg.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.simple_scoring_rb) {
                    viewModel.updateScoring(false);
                    return;
                }
                viewModel.updateScoring(true);
            }
            else {
                ((MaterialButton) group.findViewById(checkedId)).setIcon(null);
            }


        });
    }

    /** @noinspection DataFlowIssue*/
    private void initializeUI() {
        binding.listNameEditText.setText(
                viewModel.getTitle().getValue());

        binding.playerCountInfoButton.setOnClickListener(
                view -> Toast
                        .makeText(
                                requireContext(),
                        "Currently only 3 players are supported.",
                        Toast.LENGTH_SHORT)
                        .show());

        boolean isTournamentScoring = viewModel.isTournamentScoring().getValue();
        binding.scoringSettingsRg.check(
                isTournamentScoring
                        ? R.id.tournament_scoring_rb
                        : R.id.simple_scoring_rb
        );
        binding.roundCountSlider.setValue(
                viewModel.getNumberOfRounds().getValue());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}