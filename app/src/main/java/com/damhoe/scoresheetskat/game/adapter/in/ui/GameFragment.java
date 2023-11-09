package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.databinding.FragmentGameV2Binding;
import com.damhoe.scoresheetskat.game.application.PlayerSelectionValidator;
import com.damhoe.scoresheetskat.game.application.PlayerSelectionValidator.MessageType;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.score.adapter.in.ui.SharedScoreResponseViewModel;
import com.damhoe.scoresheetskat.score.domain.ScoreEvent;
import com.damhoe.scoresheetskat.score.domain.ScoreEventType;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class GameFragment extends Fragment implements IScoreActionListener {
    @Inject
    GameViewModelFactory viewModelFactory;
    @Inject
    SelectPlayerVMFactory selectPlayerVMFactory;
    private SkatGameViewModel viewModel;
    private SelectPlayerViewModel selectPlayerViewModel;
    private SharedScoreResponseViewModel scoreResponseViewModel;
    private FragmentGameV2Binding binding;
    private PlayerSelectionValidator playerValidator = new PlayerSelectionValidator();
    private SkatScoreAdapter scoreAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((MainActivity) requireActivity()).appComponent.inject(this);
        super.onAttach(context);
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_v2, container, false);
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

        final Observer<Pair<Integer, Integer>> currentRoundObserver =
                roundPair -> binding.currentRoundText.setText(
                        String.format(Locale.getDefault(),
                                "%d / %d", roundPair.first, roundPair.second)
                );
        viewModel.getCurrentRoundInfo().observe(getViewLifecycleOwner(), currentRoundObserver);

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

        final Observer<SkatSettings> settingsObserver = new Observer<SkatSettings>() {
            @Override
            public void onChanged(SkatSettings skatSettings) {
                int visibilityWinLossBonus = skatSettings
                        .isTournamentScoring() ? View.VISIBLE : View.GONE;
                binding.winsLossesBonusView.getRoot().setVisibility(visibilityWinLossBonus);
                binding.lostGamesBonusView.getRoot().setVisibility(visibilityWinLossBonus);
                binding.middleDivider.setVisibility(visibilityWinLossBonus);
                SkatGame game = viewModel.getGame().getValue();
                if (game != null)
                    updatePoints(game);

                if (scoreAdapter != null) {
                    scoreAdapter.setNumberOfRounds(skatSettings.getNumberOfRounds());
                    scoreAdapter.notifyItemRangeChanged(0, scoreAdapter.getItemCount());
                }
            }
        };
        viewModel.getSettings().observe(getViewLifecycleOwner(), settingsObserver);

        final Observer<ScoreEvent> scoreEventObserver = scoreEvent -> {
            SkatScore score = scoreEvent.getScore();
            Log.d("ScoreEvent", "New score is added, id: " + score.getId());
            if (scoreEvent.getEventType() == ScoreEventType.CREATE) {
                viewModel.addScore(scoreEvent.getScore());
                if (scoreAdapter != null) {
                    scoreAdapter.notifyItemInserted(scoreAdapter.getItemCount() - 1);
                }
            }
            else {
                Log.d("ScoreEvent", "Score was updated, id: " + score.getId());
                viewModel.updateScore(scoreEvent.getScore());
                if (scoreAdapter != null) {
                    int position = scoreAdapter.getPosition(score.getId());
                    if (position >= 0) {
                        scoreAdapter.notifyItemChanged(position);
                    }
                }
            }
        };
        scoreResponseViewModel.getScoreEvent().observe(getViewLifecycleOwner(), scoreEventObserver);

        viewModel.totalPoints.observe(getViewLifecycleOwner(), this::displayTotalPoints);
        viewModel.winBonus.observe(getViewLifecycleOwner(), ints -> {
            binding.winsLossesBonusView.points1Text.setText(String.valueOf(ints[0]));
            binding.winsLossesBonusView.points2Text.setText(String.valueOf(ints[1]));
            binding.winsLossesBonusView.points3Text.setText(String.valueOf(ints[2]));
        });
        viewModel.lossOfOthersBonus.observe(getViewLifecycleOwner(), ints -> {
            binding.lostGamesBonusView.points1Text.setText(String.valueOf(ints[0]));
            binding.lostGamesBonusView.points2Text.setText(String.valueOf(ints[1]));
            binding.lostGamesBonusView.points3Text.setText(String.valueOf(ints[2]));
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
                    showEditDealerDialog();
                } else if (itemId == R.id.game_show_chart) {
                    Snackbar.make(binding.bottomSumView.getRoot(),
                                    "This feature is not implemented yet.",
                                    Snackbar.LENGTH_SHORT)
                            .show();
                } else if (itemId == R.id.home) {
                    findNavController().navigateUp();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showGameSettingsDialog() {
//        @SuppressLint("InflateParams")
//        View scoringView = getLayoutInflater().inflate(R.layout.dialog_scoring, null);
//        RadioGroup scoringRadioGroup = (RadioGroup) scoringView.findViewById(R.id.scoring_settings_rg);
//        scoringRadioGroup.check(viewModel.isTournamentScoring().getValue() ?
//                R.id.tournament_scoring_rb : R.id.simple_scoring_rb);
//
//        new MaterialAlertDialogBuilder(requireContext())
//                .setTitle(R.string.message_scoring_dialog)
//                .setView(scoringView)
//                .setPositiveButton("Save", (dialogInterface, i) -> {
//                    int buttonId = scoringRadioGroup.getCheckedRadioButtonId();
//                    viewModel.setTournamentScoring(buttonId != R.id.simple_scoring_rb);
//                    dialogInterface.dismiss();
//                })
//                .setNegativeButton("Cancel", ((d, i) -> d.cancel()))
//                .create()
//                .show();
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
            Snackbar.make(requireView(), deleteResult.getMessage(), Snackbar.LENGTH_SHORT)
                    .setAction("GOT IT", view -> onDestroy())
                    .show();
            return;
        }
        if (scoreAdapter != null) {
            int position = scoreAdapter.getPosition(deleteResult.getValue().getId());
            scoreAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyDetails(SkatScore skatScore) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Score details")
                .setPositiveButton("Got it", ((dialogInterface, i) -> dialogInterface.dismiss()))
                .create()
                .show();
    }

    @Override
    public void notifyEdit(SkatScore skatScore) {
        List<Player> players = viewModel.getPlayers().getValue();
        if (players == null) {
            throw new RuntimeException("Players are null when score request is created.");
        }
        ScoreRequest scoreRequest = new ScoreRequest.Builder()
                .setPlayerNames(
                        players.get(0).getName(),
                        players.get(1).getName(),
                        players.get(2).getName())
                .setPlayerPositions(0, 1, 2)
                .setScoreId(skatScore.getId())
                .build();
        Bundle bundle = new Bundle();
        bundle.putParcelable("scoreRequest", scoreRequest);
        findNavController().navigate(R.id.action_game_to_score, bundle);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(SkatGameViewModel.class);
        scoreResponseViewModel = new ViewModelProvider(requireActivity()).get(SharedScoreResponseViewModel.class);
        scoreResponseViewModel.reset();

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
        // Empty.
    }

    private void showEditDealerDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Who was dealer in the first round?")
                //.setView()
                .setNegativeButton("Cancel", (d, i) -> d.cancel())
                .setPositiveButton("Save", (d, i) -> d.dismiss())
                .create()
                .show();
    }

    @SuppressLint("InflateParams")
    private void showEditPlayerDialog() {
        // Get current players and registered players
        List<Player> currentPlayers = viewModel.getPlayers().getValue();
        assert currentPlayers != null;
        List<Player> allPlayers = selectPlayerViewModel.getAllPlayers();

        View contentView = getLayoutInflater().inflate(R.layout.dialog_player_names, null);
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
                new ArrayAdapter<>(requireContext(), R.layout.text_input_list_item, allPlayers);
        editPlayer1.setAdapter(adapter);
        editPlayer2.setAdapter(adapter);
        editPlayer3.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(contentView)
                .setTitle("Edit Players")
                .setNegativeButton("Cancel", (d, i) -> d.cancel())
                .setPositiveButton("Save", (d, i) -> {
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
        return playerResult.getValue();
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
            ScoreRequest scoreRequest = new ScoreRequest.Builder()
                    .setGameId(gameId)
                    .setPlayerNames(players.get(0).getName(),
                            players.get(1).getName(),
                            players.get(2).getName())
                    .setPlayerPositions(0, 1, 2)
                    .build();
            Bundle bundle = new Bundle();
            bundle.putParcelable("scoreRequest", scoreRequest);
            findNavController().navigate(R.id.action_game_to_score, bundle);
        });
    }
}