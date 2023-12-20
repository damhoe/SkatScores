package com.damhoe.skatscores.game.game_home.adapter.in.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.damhoe.skatscores.R;
import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.databinding.DialogGameSettingsBinding;
import com.damhoe.skatscores.databinding.FragmentGameBinding;
import com.damhoe.skatscores.game.game_home.GameActivity;
import com.damhoe.skatscores.game.game_home.application.PlayerSelectionValidator;
import com.damhoe.skatscores.game.game_home.application.PlayerSelectionValidator.MessageType;
import com.damhoe.skatscores.game.game_home.domain.SkatGame;
import com.damhoe.skatscores.game.game_home.domain.SkatSettings;
import com.damhoe.skatscores.game.game_setup.domain.SkatGameCommand;
import com.damhoe.skatscores.game.score.domain.ScoreRequest.CreateScoreRequest;
import com.damhoe.skatscores.game.score.domain.ScoreRequest.UpdateScoreRequest;
import com.damhoe.skatscores.game.score.domain.SkatScore;
import com.damhoe.skatscores.player.domain.Player;
import com.damhoe.skatscores.shared_ui.utils.InsetsManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static com.damhoe.skatscores.game.score.adapter.in.ui.ScoreFragment.SCORE_REQUEST_KEY;

public class GameFragment extends Fragment implements IScoreActionListener {
    @Inject
    GameViewModelFactory viewModelFactory;
    @Inject
    SelectPlayerVMFactory selectPlayerVMFactory;
    private SkatGameViewModel viewModel;
    private SelectPlayerViewModel selectPlayerViewModel;
    private FragmentGameBinding binding;
    private PlayerSelectionValidator playerValidator = new PlayerSelectionValidator();
    private SkatScoreAdapter scoreAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((GameActivity) requireActivity()).getAppComponent().inject(this);
        super.onAttach(context);
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
        setUpEditScoreButton();
        setUpEditPlayersButton();
        setUpRecyclerView();
        return binding.getRoot();
    }

    private void setUpEditPlayersButton() {
        binding.titleView.buttonEdit.setOnClickListener(view -> showEditPlayerDialog());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set insets
        InsetsManager.applySystemBarInsets(binding.content);
        InsetsManager.applyNavigationBarInsets(binding.editScoreButton);
        InsetsManager.applyNavigationBarInsets(binding.bottomAppBar);


        setupNavigation();
        setupObservers();
        addMenu();
        initializeUI();
    }

    private void setupObservers() {
        final Observer<String> titleObserver = this::setTitle;
        viewModel.getTitle().observe(getViewLifecycleOwner(), titleObserver);

        final Observer<SkatGame> gameObserver = this::updatePoints;
        viewModel.getGame().observe(getViewLifecycleOwner(), gameObserver);

        viewModel.getGameRunStateInfo().observe(getViewLifecycleOwner(), gameRunStateInfo -> {
            String text;
            if (gameRunStateInfo.isFinished()) {
                text = "Finished";
            } else {
                text = String.format(Locale.getDefault(),
                        "%d/%d",
                        gameRunStateInfo.getCurrentRound(),
                        gameRunStateInfo.getRoundsCount()
                );
            }
            binding.currentRoundText.setText(text);
        });

        viewModel.dealerPosition.observe(getViewLifecycleOwner(), dealerPosition -> {
            if (dealerPosition == 0) {
                binding.titleView.indicator1.setAlpha(1.0f);
                binding.titleView.indicator2.setAlpha(0.0f);
                binding.titleView.indicator3.setAlpha(0.0f);
            } else if (dealerPosition == 1) {
                binding.titleView.indicator1.setAlpha(0.0f);
                binding.titleView.indicator2.setAlpha(1.0f);
                binding.titleView.indicator3.setAlpha(0.0f);
            } else if (dealerPosition == 2) {
                binding.titleView.indicator1.setAlpha(0.0f);
                binding.titleView.indicator2.setAlpha(0.0f);
                binding.titleView.indicator3.setAlpha(1.0f);
            }
        });

        final Observer<List<Player>> playerObserver = players -> {
            // Update player names
            binding.titleView.name1Text.setText(players.get(0).getName());
            binding.titleView.name2Text.setText(players.get(1).getName());
            binding.titleView.name3Text.setText(players.get(2).getName());
        };
        viewModel.getPlayers().observe(getViewLifecycleOwner(), playerObserver);

        final Observer<SkatSettings> settingsObserver = skatSettings -> {
            int visibilityWinLossBonus = skatSettings
                    .isTournamentScoring() ? View.VISIBLE : View.GONE;
            binding.bottomSumView.lostContainer.setVisibility(visibilityWinLossBonus);
            binding.bottomSumView.soloContainer.setVisibility(visibilityWinLossBonus);
            binding.bottomSumView.divider.setVisibility(visibilityWinLossBonus);
            SkatGame game = viewModel.getGame().getValue();
            if (game != null)
                updatePoints(game);
        };
        viewModel.getSettings().observe(getViewLifecycleOwner(), settingsObserver);

        viewModel.totalPoints.observe(getViewLifecycleOwner(), this::displayTotalPoints);
        viewModel.winBonus.observe(getViewLifecycleOwner(), ints -> {
            binding.bottomSumView.solo1Text.setText(String.valueOf(ints[0]));
            binding.bottomSumView.solo2Text.setText(String.valueOf(ints[1]));
            binding.bottomSumView.solo3Text.setText(String.valueOf(ints[2]));
        });
        viewModel.lossOfOthersBonus.observe(getViewLifecycleOwner(), ints -> {
            binding.bottomSumView.lost1Text.setText(String.valueOf(ints[0]));
            binding.bottomSumView.lost2Text.setText(String.valueOf(ints[1]));
            binding.bottomSumView.lost3Text.setText(String.valueOf(ints[2]));
        });
    }

    private void displayTotalPoints(int[] totalPoints) {
        binding.bottomSumView.points1Text.setText(String.valueOf(totalPoints[0]));
        binding.bottomSumView.points2Text.setText(String.valueOf(totalPoints[1]));
        binding.bottomSumView.points3Text.setText(String.valueOf(totalPoints[2]));
    }

    private void addMenu() {
        binding.bottomAppBar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.game_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.game_edit) {
                    showGameSettingsDialog();
                } else if (itemId == R.id.game_show_chart) {
                    NavDirections directions =
                            GameFragmentDirections.actionGameToGraph();
                    findNavController().navigate(directions);
                } else if (itemId == R.id.library) {
                    findNavController().navigateUp();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showGameSettingsDialog() {
        DialogGameSettingsBinding dialogBinding =
                DataBindingUtil.inflate(
                        getLayoutInflater(),
                        R.layout.dialog_game_settings,
                        null,
                        false
                );

        // Initialize values
        dialogBinding.listNameEditText.setText(viewModel.getTitle().getValue());

        List<Player> players = viewModel.getPlayers().getValue();
        if (players != null && players.size() == 3) {
            dialogBinding.buttonPlayer1.setText(players.get(0).getName());
            dialogBinding.buttonPlayer2.setText(players.get(1).getName());
            dialogBinding.buttonPlayer3.setText(players.get(2).getName());
        }

        SkatGame skatGame = viewModel.getGame().getValue();
        if (skatGame == null) {
            return;
        }

        // Create a map for (position, buttonId)
        SparseIntArray startDealerMap = new SparseIntArray();
        startDealerMap.put(0, R.id.buttonPlayer1);
        startDealerMap.put(1, R.id.buttonPlayer2);
        startDealerMap.put(2, R.id.buttonPlayer3);
        dialogBinding.toggleGroupStartDealer.check(
                startDealerMap.get(skatGame.getStartDealerPosition(), R.id.buttonPlayer1));

        SkatSettings settings = skatGame.getSettings();

        dialogBinding.numberOfRoundsText.setText(
                String.valueOf(settings.getNumberOfRounds()));
        dialogBinding.roundsSlider.setValue(settings.getNumberOfRounds());

        dialogBinding.scoringSettingsRg.check(settings.isTournamentScoring() ?
                R.id.tournament_scoring_rb : R.id.simple_scoring_rb);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.message_scoring_dialog)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(getString(R.string.dialog_title_button_save), (dialogInterface, i) -> {
                    Editable editable = dialogBinding.listNameEditText.getText();
                    String title = editable != null ?
                            editable.toString() : viewModel.getTitle().getValue();

                    settings.setNumberOfRounds((int) dialogBinding.roundsSlider.getValue());
                    settings.setTournamentScoring(dialogBinding.tournamentScoringRb.isChecked());

                    int checkedButton = dialogBinding.toggleGroupStartDealer.getCheckedButtonId();
                    int startDealerPosition = startDealerMap
                            .keyAt(startDealerMap.indexOfValue(checkedButton));

                    skatGame.setStartDealerPosition(startDealerPosition);
                    skatGame.setTitle(title);
                    skatGame.setSettings(settings);
                    viewModel.updateGame(skatGame);
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.dialog_title_button_cancel), ((d, i) -> d.cancel()))
                .create();

        // Add listeners
        dialogBinding.listNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void afterTextChanged(Editable s) {
                int counterMaxLength = dialogBinding.listNameTextInput.getCounterMaxLength();
                int length = s.length();
                if (length > counterMaxLength) {
                    s.replace(counterMaxLength, length, "");
                }

                String title = s.toString().trim();

                // Set error if invalid title
                dialogBinding.listNameTextInput.setError(
                        title.isEmpty() ? getString(R.string.error_valid_title_required) : null
                );

                // Disable positive button if error exists
                Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setEnabled( dialogBinding.listNameTextInput.getError() == null );
            }
        });

        dialogBinding.roundsSlider.addOnChangeListener((slider, value, fromUser) -> {
            dialogBinding.numberOfRoundsText.setText(String.valueOf((int) value));

            int currentRound = Objects.requireNonNull(viewModel.getGameRunStateInfo().getValue()).getCurrentRound();

            // Disable positive button if error exists
            Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            buttonPositive.setEnabled( (int) value >= currentRound - 1);
        });

        dialog.show();
    }

    private void initializeUI() {
        // Empty
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updatePoints(SkatGame game) {
        // Set adapter items
        if (scoreAdapter != null) {
            List<SkatScore> scores = game.getScores();
            if (scoreAdapter.getScores() != scores) {
                scoreAdapter.setScores(scores);
            }
        }

        ArrayList<Integer> playerPoints = new ArrayList<>();
        playerPoints.add(0);//game.getIndividualPoints();
        playerPoints.add(0);//game.getIndividualPoints();
        playerPoints.add(0);//game.getIndividualPoints();

        // Bottom sum
        binding.bottomSumView.points1Text.setText(String.valueOf(playerPoints.get(0)));
        binding.bottomSumView.points2Text.setText(String.valueOf(playerPoints.get(1)));
        binding.bottomSumView.points3Text.setText(String.valueOf(playerPoints.get(2)));
    }

    @Override
    public void notifyDelete() {
        Result<SkatScore> deleteResult = viewModel.removeLastScore();
        if (deleteResult.isFailure()) {
            Snackbar.make(requireView(), deleteResult.message, Snackbar.LENGTH_SHORT)
                    .setAction("GOT IT", view -> onDestroy())
                    .show();
            return;
        }
        if (scoreAdapter != null) {
            int position = scoreAdapter.getPosition(deleteResult.value.getId());
            scoreAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyDetails(SkatScore skatScore) {

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_title_score_details))
                .setMessage(
                        new SkatScore.TextMaker(requireContext())
                                .setupWithSkatScore(skatScore)
                                .make()
                )
                .setPositiveButton(getString(R.string.dialog_title_button_got_it), ((dialogInterface, i) -> dialogInterface.dismiss()))
                .create()
                .show();
    }

    @Override
    public void notifyEdit(SkatScore skatScore) {
        List<Player> players = viewModel.getPlayers().getValue();
        if (players == null) {
            throw new RuntimeException("Players are null when score request is created.");
        }

        List<String> names = new ArrayList<>();
        names.add(players.get(0).getName());
        names.add(players.get(1).getName());
        names.add(players.get(2).getName());

        List<Integer> positions = new ArrayList<>();
        positions.add(0);
        positions.add(1);
        positions.add(2);

        UpdateScoreRequest scoreRequest = new UpdateScoreRequest(skatScore.getId(), names, positions);
        Bundle bundle = new Bundle();
        bundle.putParcelable(SCORE_REQUEST_KEY, scoreRequest);
        findNavController().navigate(R.id.action_game_to_score, bundle);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(SkatGameViewModel.class);

        // Get arguments and initialize the game
        SkatGameCommand command = GameFragmentArgs.fromBundle(requireArguments()).getGameCommand();
        long gameId = GameFragmentArgs.fromBundle(requireArguments()).getGameId();

        if (gameId != -1) {
            viewModel.initialize(gameId);
        } else if (command != null) {
            viewModel.initialize(command);
        } else {
            throw new RuntimeException("GameFragment needs nonnull command or game ID");
        }

        selectPlayerViewModel = new ViewModelProvider(this, selectPlayerVMFactory)
                .get(SelectPlayerViewModel.class);
    }

    private void setupNavigation() {
        // Back navigation
        binding.returnButton.setOnClickListener(view -> requireActivity().finish());

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                });
    }

    @SuppressLint("InflateParams")
    private void showEditPlayerDialog() {
        // Get current players and registered players
        List<Player> currentPlayers = viewModel.getPlayers().getValue();
        assert currentPlayers != null;
        List<Player> allPlayers = selectPlayerViewModel.getAllPlayers();

        View contentView = getLayoutInflater().inflate(R.layout.dialog_select_players, null);
        TextInputLayout input1 = contentView.findViewById(R.id.player1_input);
        TextInputLayout input2 = contentView.findViewById(R.id.player2_input);
        TextInputLayout input3 = contentView.findViewById(R.id.player3_input);
        MaterialAutoCompleteTextView editPlayer1 = contentView.findViewById(R.id.player1_edit_text);
        MaterialAutoCompleteTextView editPlayer2 = contentView.findViewById(R.id.player2_edit_text);
        MaterialAutoCompleteTextView editPlayer3 = contentView.findViewById(R.id.player3_edit_text);

        // Set current player names
        editPlayer1.setText(currentPlayers.get(0).getName());
        editPlayer1.setSelection(editPlayer1.getText().length());
        editPlayer2.setText(currentPlayers.get(1).getName());
        editPlayer2.setSelection(editPlayer2.getText().length());
        editPlayer3.setText(currentPlayers.get(2).getName());
        editPlayer3.setSelection(editPlayer3.getText().length());

        ArrayAdapter<Player> adapter =
                new ArrayAdapter<>(requireContext(), R.layout.item_popup_list, allPlayers);
        editPlayer1.setAdapter(adapter);
        editPlayer2.setAdapter(adapter);
        editPlayer3.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(contentView)
                .setTitle(getString(R.string.dialog_title_edit_players))
                .setNegativeButton(getString(R.string.dialog_title_button_cancel), (d, i) -> d.cancel())
                .setPositiveButton(getString(R.string.dialog_title_button_save), (d, i) -> {
                    String name1 = editPlayer1.getText().toString().trim();
                    String name2 = editPlayer2.getText().toString().trim();
                    String name3 = editPlayer3.getText().toString().trim();

                    // Map names to players
                    Player player1 = mapToPlayer(name1);
                    Player player2 = mapToPlayer(name2);
                    Player player3 = mapToPlayer(name3);

                    List<Player> newPlayers = Arrays.asList(player1, player2, player3);
                    viewModel.updatePlayers(newPlayers);
                    d.dismiss();
                })
                .create();

        // Add listeners
        addTextChangeListener(editPlayer1, input1, dialog, 0);
        addTextChangeListener(editPlayer2, input2, dialog, 1);
        addTextChangeListener(editPlayer3, input3, dialog, 2);

        if (playerValidator == null) {
            playerValidator = new PlayerSelectionValidator();
        }
        playerValidator.initialize(
                allPlayers,
                currentPlayers.stream()
                        .map(Player::getName)
                        .collect(Collectors.toList())
        );

        dialog.show();
    }

    private Player mapToPlayer(String name) {
        Result<Player> playerResult = selectPlayerViewModel.findOrCreatePlayer(name);
        if (playerResult.isFailure()) {
            return Player.createDummy(0);
        }
        return playerResult.value;
    }

    private void addTextChangeListener(
            AutoCompleteTextView textView,
            TextInputLayout inputLayout,
            AlertDialog dialog,
            int position
    ) {
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Reset error
                inputLayout.setError(null);
                inputLayout.setHelperText(null);

                // Check if the current length exceeds the maximum length
                int maxLength = inputLayout.getCounterMaxLength();
                int currentLength = s.length();
                if (currentLength > maxLength) {
                    // Trim the text to the maximum length
                    s.replace(maxLength, currentLength, "");
                }

                String name = s.toString().trim();
                playerValidator.select(position, name);
                List<Pair<MessageType, String>> messages = playerValidator.validate();
                MessageType type = messages.get(position).first;
                String message = messages.get(position).second;

                if (type == MessageType.Error) {
                    inputLayout.setError(message);
                } else if (message != null) {
                    inputLayout.setHelperText(message);
                }

                Button buttonSave = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonSave.setEnabled(messages.stream().noneMatch(pair -> pair.first == MessageType.Error));
            }
        });
    }

    /** */
    private void setUpRecyclerView() {
        binding.scoresRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        scoreAdapter = new SkatScoreAdapter(this);
        binding.scoresRv.setAdapter(scoreAdapter);
        binding.scoresRv.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    private void setTitle(String title) {
        // Set toolbar label
        binding.gameTitle.setText(title);
    }

    private void setUpEditScoreButton() {
        binding.editScoreButton.setOnClickListener(view -> {
            Log.d("Event", "Edit score button clicked.");

            List<Player> players = viewModel.getPlayers().getValue();
            assert players != null;

            SkatGame game = viewModel.getGame().getValue();
            long gameId = game != null ? game.getId() : -1L;

            List<String> names = new ArrayList<>();
            names.add(players.get(0).getName());
            names.add(players.get(1).getName());
            names.add(players.get(2).getName());

            List<Integer> positions = new ArrayList<>();
            positions.add(0);
            positions.add(1);
            positions.add(2);

            CreateScoreRequest request = new CreateScoreRequest(gameId, names, positions);
            Bundle bundle = new Bundle();
            bundle.putParcelable(SCORE_REQUEST_KEY, request);
            findNavController().navigate(R.id.action_game_to_score, bundle);
        });
    }
}