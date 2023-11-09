package com.damhoe.scoresheetskat.score.adapter.in.ui;

import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentScoreBinding;
import com.damhoe.scoresheetskat.score.domain.ScoreEvent;
import com.damhoe.scoresheetskat.score.domain.ScoreEventType;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatResult;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;
import com.damhoe.scoresheetskat.score.domain.SkatSuit;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.damhoe.scoresheetskat.shared_ui.utils.LayoutMargins;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ScoreFragment extends Fragment {
    @Inject
    ScoreViewModelFactory viewModelFactory;
    private ScoreViewModel viewModel;
    private SharedScoreResponseViewModel sharedViewModel;
    private ScoreEventType eventType;
    private FragmentScoreBinding binding;
    private final Map<Integer, SkatSuit> buttonSkatSuitMap = new HashMap<>();
    private final Map<Integer, Runnable> buttonPlayerActionMap = new HashMap<>();
    private final Map<Integer, Runnable> playerPositionCheckButtonMap = new HashMap<>();
    private final Map<SkatSuit, Runnable> suitCheckButtonMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).appComponent.inject(this);
        super.onCreate(savedInstanceState);

        ScoreRequest scoreRequest = requireArguments().getParcelable("scoreRequest");
        if (scoreRequest == null) {
            throw new RuntimeException("Score request should have nonnull value.");
        }

        viewModelFactory.setScoreRequest(scoreRequest);
        eventType = scoreRequest.getScoreId() == -1L ? ScoreEventType.CREATE : ScoreEventType.UPDATE;
        viewModel = viewModelFactory.create(ScoreViewModel.class);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedScoreResponseViewModel.class);
        sharedViewModel.reset();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_score, container, false);
        initializeButtonSkatSuitMap();
        initializeButtonPlayerActionMap();

        binding.toggleGroupSoloPlayer.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                handlePlayerButtonCheck(checkedId);
            }
        });
        binding.toggleGroupResult.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonOverbid) {
                    viewModel.setResult(SkatResult.OVERBID);
                } else if (checkedId == R.id.buttonWon){
                    viewModel.setResult(SkatResult.WON);
                } else {
                    viewModel.setResult(SkatResult.LOST);
                }
            }
        });
        binding.toggleGroupSuit.addOnButtonCheckedListener(((group, checkedId, isChecked) -> {
            if (isChecked) {
                binding.toggleGroupSpecialSuit.clearChecked();
                handleSuitButtonCheck(checkedId);
            }
        }));
        binding.toggleGroupSpecialSuit.addOnButtonCheckedListener(((group, checkedId, isChecked) -> {
            if (isChecked) {
                binding.toggleGroupSuit.clearChecked();
                handleSuitButtonCheck(checkedId);
            }
        }));
        binding.announcementsChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            binding.content.requestLayout();
            handleAnnouncementChips(checkedIds);
        });
        binding.winLevelChips.setOnCheckedStateChangeListener(((group, checkedIds) -> {
            binding.content.requestLayout();
            handleWinLevelChips(checkedIds);
        }));
        binding.spitzenSlider.addOnChangeListener((slider, value, fromUser) -> {
            binding.spitzenLabel.setText(String.valueOf((int) value));
            viewModel.setSpitzen((int) value);
        });
        binding.spitzenRemoveButton.setOnClickListener(
                view -> binding.spitzenSlider.setValue(binding.spitzenSlider.getValue() - 1)
        );
        binding.spitzenAddButton.setOnClickListener(
                view -> binding.spitzenSlider.setValue(binding.spitzenSlider.getValue() + 1)
        );
        binding.scoreDoneButton.setOnClickListener(view -> {
            saveScore();
            findNavController().navigateUp();
        });
        return binding.getRoot();
    }

    private void handleAnnouncementChips(@NonNull List<Integer> checkedIds) {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_announced_chip) &&
                !checkedIds.contains(R.id.schneider_announced_chip)) {
            binding.announcementsChips.check(R.id.schneider_announced_chip);
            return;
        }

        // Set viewModel data
        viewModel.setSchneiderAnnounced(checkedIds.contains(R.id.schneider_announced_chip));
        viewModel.setSchwarzAnnounced(checkedIds.contains(R.id.schwarz_announced_chip));

    }

    private void handleWinLevelChips(@NonNull List<Integer> checkedIds) {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_chip) &&
                !checkedIds.contains(R.id.schneider_chip)) {
            binding.winLevelChips.check(R.id.schneider_chip);
            return;
        }
        viewModel.setHand(checkedIds.contains(R.id.hand_chip));
        viewModel.setOuvert(checkedIds.contains(R.id.ouvert_chip));
        viewModel.setSchneider(checkedIds.contains(R.id.schneider_chip));
        viewModel.setSchwarz(checkedIds.contains(R.id.schwarz_chip));
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    private void handlePlayerButtonCheck(int buttonId) {
        Runnable action = buttonPlayerActionMap.get(buttonId);
        if (action != null) {
            action.run();
        }
    }

    private void handleSuitButtonCheck(int buttonId) {
        viewModel.setSuit(buttonSkatSuitMap.get(buttonId));
    }

    // Action if the button gets checked
    private void initializeButtonPlayerActionMap() {
        buttonPlayerActionMap.put(binding.buttonPasse.getId(), () -> viewModel.setPasse());
        buttonPlayerActionMap.put(binding.buttonPlayer1.getId(),
                () -> viewModel.setPlayerPosition(viewModel.getPlayerPositions()[0]));
        buttonPlayerActionMap.put(binding.buttonPlayer2.getId(),
                () -> viewModel.setPlayerPosition(viewModel.getPlayerPositions()[1]));
        buttonPlayerActionMap.put(binding.buttonPlayer3.getId(),
                () -> viewModel.setPlayerPosition(viewModel.getPlayerPositions()[2]));

        playerPositionCheckButtonMap.put(viewModel.getPlayerPositions()[0], () -> binding.toggleGroupSoloPlayer.check(binding.buttonPlayer1.getId()));
        playerPositionCheckButtonMap.put(viewModel.getPlayerPositions()[1], () -> binding.toggleGroupSoloPlayer.check(binding.buttonPlayer2.getId()));
        playerPositionCheckButtonMap.put(viewModel.getPlayerPositions()[2], () -> binding.toggleGroupSoloPlayer.check(binding.buttonPlayer3.getId()));
    }

    private void initializeButtonSkatSuitMap() {
        buttonSkatSuitMap.put(binding.buttonClubs.getId(), SkatSuit.CLUBS);
        buttonSkatSuitMap.put(binding.buttonSpades.getId(), SkatSuit.SPADES);
        buttonSkatSuitMap.put(binding.buttonHearts.getId(), SkatSuit.HEARTS);
        buttonSkatSuitMap.put(binding.buttonDiamonds.getId(), SkatSuit.DIAMONDS);
        buttonSkatSuitMap.put(binding.buttonGrand.getId(), SkatSuit.GRAND);
        buttonSkatSuitMap.put(binding.buttonNull.getId(), SkatSuit.NULL);

        suitCheckButtonMap.put(SkatSuit.CLUBS, () -> binding.toggleGroupSuit.check(binding.buttonClubs.getId()));
        suitCheckButtonMap.put(SkatSuit.SPADES, () -> binding.toggleGroupSuit.check(binding.buttonSpades.getId()));
        suitCheckButtonMap.put(SkatSuit.HEARTS, () -> binding.toggleGroupSuit.check(binding.buttonHearts.getId()));
        suitCheckButtonMap.put(SkatSuit.DIAMONDS, () -> binding.toggleGroupSuit.check(binding.buttonDiamonds.getId()));
        suitCheckButtonMap.put(SkatSuit.GRAND, () -> binding.toggleGroupSpecialSuit.check(binding.buttonGrand.getId()));
        suitCheckButtonMap.put(SkatSuit.NULL, () -> binding.toggleGroupSpecialSuit.check(binding.buttonNull.getId()));
    }

    private void showOrHideCheckedIcon(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (isChecked) {
            ((MaterialButton) group.findViewById(checkedId)).setIcon(
                    ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_done_black_24dp, null));
        }
        else {
            ((MaterialButton) group.findViewById(checkedId)).setIcon(null);
        }
    }

    /** @noinspection DataFlowIssue*/
    private void initializeUI() {
        String[] names = viewModel.getPlayerNames();
        if (names.length != 3) {
            throw new RuntimeException("Expected 3 player names" +
                    " in observed LiveData but got " + names.length);
        }
        binding.buttonPlayer1.setText(names[0]);
        binding.buttonPlayer2.setText(names[1]);
        binding.buttonPlayer3.setText(names[2]);

        SkatScoreCommand command = viewModel.getScoreCommand().getValue();
        if (command != null) {
            if (command.isPasse()) {
                binding.toggleGroupSoloPlayer.check(R.id.buttonPasse);
                binding.spitzenSlider.setValue(1);
                return;
            }

            if (command.isLost()) {
                binding.toggleGroupResult.check(R.id.buttonLost);
            } else if (command.isWon()) {
                binding.toggleGroupResult.check(R.id.buttonWon);
            } else {
                binding.toggleGroupResult.check(R.id.buttonOverbid);
            }

            playerPositionCheckButtonMap.get(command.getPlayerPosition()).run();
            suitCheckButtonMap.get(command.getSuit()).run();
            binding.spitzenSlider.setValue(command.getSpitzen());
            binding.handChip.setChecked(command.isHand());
            binding.ouvertChip.setChecked(command.isOuvert());
            binding.schneiderChip.setChecked(command.isSchneider());
            binding.schwarzChip.setChecked(command.isSchwarz());
            binding.schneiderAnnouncedChip.setChecked(command.isSchneiderAnnounced());
            binding.schwarzAnnouncedChip.setChecked(command.isSchwarzAnnounced());
            binding.spitzenSlider.setValue(command.getSpitzen());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup navigation
        NavController navController = findNavController();
        AppBarConfiguration appBarConfiguration =
                ((MainActivity)requireActivity()).getAppBarConfiguration();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        // Set insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        int marginRight = getResources().getDimensionPixelSize(R.dimen.fab_margin_right);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.fab_margin_bottom);
        LayoutMargins defaultMargins =
                new LayoutMargins(0, 0, marginRight, marginBottom);
        InsetsManager.applyNavigationBarInsets(binding.scoreDoneButton, defaultMargins);
        InsetsManager.applyNavigationBarInsets(binding.content);

        viewModel.isSuitsEnabled.observe(getViewLifecycleOwner(), this::enableSuitButtons);
        viewModel.isHandEnabled.observe(getViewLifecycleOwner(), isEnabled -> {
            binding.handChip.setEnabled(isEnabled);
        });
        viewModel.isSchneiderSchwarzEnabled.observe(getViewLifecycleOwner(),
                this::enableSchneiderSchwarzChips);
        viewModel.isOuvertEnabled.observe(getViewLifecycleOwner(),
                isEnabled -> binding.ouvertChip.setEnabled(isEnabled)
        );
        viewModel.isSpitzenEnabled.observe(getViewLifecycleOwner(),
                isEnabled -> binding.spitzenSlider.setEnabled(isEnabled)
        );
        viewModel.isIncreaseSpitzenEnabled.observe(getViewLifecycleOwner(),
                isEnabled -> binding.spitzenAddButton.setEnabled(isEnabled)
        );
        viewModel.isDecreaseSpitzenEnabled.observe(getViewLifecycleOwner(),
                isEnabled -> binding.spitzenRemoveButton.setEnabled(isEnabled)
        );
        viewModel.isResultsEnabled.observe(getViewLifecycleOwner(), isEnabled -> {
            binding.buttonWon.setEnabled(isEnabled);
            binding.buttonLost.setEnabled(isEnabled);
            binding.buttonOverbid.setEnabled(isEnabled);
        });
        viewModel.getSkatResult.observe(getViewLifecycleOwner(), skatResult -> {
            if (skatResult == SkatResult.WON) {
                binding.toggleGroupResult.check(R.id.buttonWon);
            } else if (skatResult == SkatResult.LOST) {
                binding.toggleGroupResult.check(R.id.buttonLost);
            } else if (skatResult == SkatResult.OVERBID) {
                binding.toggleGroupResult.check(R.id.buttonOverbid);
            } else {
                binding.toggleGroupResult.clearChecked();
            }
        });
        viewModel.getSuit.observe(getViewLifecycleOwner(), skatSuit -> {
            if (skatSuit == SkatSuit.INVALID) {
                binding.announcementsChips.clearCheck();
                binding.winLevelChips.clearCheck();
                binding.spitzenSlider.setValue(binding.spitzenSlider.getValueFrom());
                binding.toggleGroupSuit.clearChecked();
            } else if (skatSuit == SkatSuit.CLUBS) {
                binding.toggleGroupSuit.check(R.id.buttonClubs);
            } else if (skatSuit == SkatSuit.SPADES) {
                binding.toggleGroupSuit.check(R.id.buttonSpades);
            } else if (skatSuit == SkatSuit.HEARTS) {
                binding.toggleGroupSuit.check(R.id.buttonHearts);
            } else if (skatSuit == SkatSuit.DIAMONDS) {
                binding.toggleGroupSuit.check(R.id.buttonDiamonds);
            } else if (skatSuit == SkatSuit.NULL) {
                binding.toggleGroupSuit.check(R.id.buttonNull);
            } else if (skatSuit == SkatSuit.GRAND) {
                binding.toggleGroupSuit.check(R.id.buttonGrand);
            }
        });

        initializeUI();
    }

    private void enableSchneiderSchwarzChips(Boolean isEnabled) {
        binding.schneiderChip.setEnabled(isEnabled);
        binding.schwarzChip.setEnabled(isEnabled);
        binding.schneiderAnnouncedChip.setEnabled(isEnabled);
        binding.schwarzAnnouncedChip.setEnabled(isEnabled);
    }

    private void enableSuitButtons(Boolean isEnabled) {
        binding.buttonClubs.setEnabled(isEnabled);
        binding.buttonHearts.setEnabled(isEnabled);
        binding.buttonDiamonds.setEnabled(isEnabled);
        binding.buttonSpades.setEnabled(isEnabled);
        binding.buttonNull.setEnabled(isEnabled);
        binding.buttonGrand.setEnabled(isEnabled);
        binding.buttonHearts.setAlpha(isEnabled ? 1f : 0.38f);
        binding.buttonDiamonds.setAlpha(isEnabled ? 1f : 0.38f);
    }

    public void saveScore() {
        SkatScore score;
        switch (eventType) {
            case CREATE:
                score = viewModel.createScore();
                break;
            case UPDATE:
                score = viewModel.updateScore();
                break;
            default:
                score = null;
        }
        if (score != null) {
            sharedViewModel.setScoreEvent(new ScoreEvent(score, eventType));
        }
    }
}