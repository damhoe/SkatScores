package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
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
import com.damhoe.scoresheetskat.databinding.FragmentGameBinding;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.player.adapter.in.ui.PlayersViewModel;
import com.damhoe.scoresheetskat.player.adapter.in.ui.PlayersViewModelFactory;
import com.damhoe.scoresheetskat.score.adapter.in.ui.SharedScoreResponseViewModel;
import com.damhoe.scoresheetskat.score.domain.ScoreEvent;
import com.damhoe.scoresheetskat.score.domain.ScoreEventType;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.shared_ui.base.BaseFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

public class GameFragment extends BaseFragment implements IScoreActionListener {
    @Inject
    GameViewModelFactory viewModelFactory;
    @Inject
    PlayersViewModelFactory playersViewModelFactory;
    private SkatGameViewModel viewModel;
    private PlayersViewModel playersViewModel;
    private SharedScoreResponseViewModel scoreResponseViewModel;
    private FragmentGameBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupNavigation();
        setupViewModel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((MainActivity) requireActivity()).appComponent.inject(this);
        super.onAttach(context);
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((MainActivity)requireActivity()).disableCollapsingToolbar();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
        setUpEditScoreButton();
        setUpRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        addMenu();
        initializeUI();
    }

    private void setupObservers() {
        final Observer<String> titleObserver = this::setToolbarTitle;
        viewModel.getTitle().observe(getViewLifecycleOwner(), titleObserver);

        final Observer<SkatGame> gameObserver = new Observer<SkatGame>() {
            @Override
            public void onChanged(SkatGame game) {
                updatePoints(game);
            }
        };
        viewModel.getGame().observe(getViewLifecycleOwner(), gameObserver);

        final Observer<Pair<Integer, Integer>> currentRoundObserver = roundPair -> {
            binding.currentRoundText.setText(
                    String.format(Locale.getDefault(),
                            "Round %d/%d", roundPair.first, roundPair.second));
        };
        viewModel.getCurrentRoundInfo().observe(getViewLifecycleOwner(), currentRoundObserver);

        viewModel.dealer.observe(getViewLifecycleOwner(), dealer -> {
            binding.dealerText.setText(String.format(getString(R.string.dealer_message), dealer.getName()));
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
                SkatGame game = viewModel.getGame().getValue();
                if (game != null)
                    updatePoints(game);

            }
        };
        viewModel.getSettings().observe(getViewLifecycleOwner(), settingsObserver);

        final Observer<ScoreEvent> scoreEventObserver = scoreEvent -> {
            Log.d("ScoreEvent", "New score is added at position: "
                    + scoreEvent.getScore().getIndex());
            if (scoreEvent.getEventType() == ScoreEventType.CREATE) {
                viewModel.addScore(scoreEvent.getScore());
                SkatScoreAdapter adapter = (SkatScoreAdapter) binding.scoresRv.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemInserted(scoreEvent.getScore().getIndex());
                }
            }
            else {
                Log.d("ScoreEvent", "Score is updated at position: "
                        + scoreEvent.getScore().getIndex());
                viewModel.updateScore(scoreEvent.getScore());
                SkatScoreAdapter adapter = (SkatScoreAdapter) binding.scoresRv.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemChanged(scoreEvent.getScore().getIndex());
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
        binding.pointsBoardTopSums.points1Text.setText(String.valueOf(totalPoints[0]));
        binding.pointsBoardTopSums.points2Text.setText(String.valueOf(totalPoints[1]));
        binding.pointsBoardTopSums.points3Text.setText(String.valueOf(totalPoints[2]));
        binding.bottomSumView.points1Text.setText(String.valueOf(totalPoints[0]));
        binding.bottomSumView.points2Text.setText(String.valueOf(totalPoints[1]));
        binding.bottomSumView.points3Text.setText(String.valueOf(totalPoints[2]));
    }

    private void addMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.game_menu, menu);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.game_settings:
                        //showGameSettingsDialog();
                        break;
                    case R.id.game_edit_players:
                        showEditPlayerDialog();
                        break;
                    case R.id.game_edit_dealer:
                        showEditDealerDialog();
                        break;
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
        //updatePoints(Objects.requireNonNull(viewModel.getGame().getValue()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updatePoints(SkatGame game) {
        // Set adapter items
        SkatScoreAdapter adapter = (SkatScoreAdapter)binding.scoresRv.getAdapter();
        if (adapter != null) {
            List<SkatScore> scores = game.getScores();
            if (adapter.getScores() != scores) {
                adapter.updateScores(scores);
                adapter.notifyDataSetChanged();
            }
            //List<SkatScore> oldScores = adapter.getScores();
            //DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
            //        new SkatScoreDiffCallback(oldScores, newScores));
            //adapter.updateScores(newScores);
            //diffResult.dispatchUpdatesTo(adapter);
            //adapter.notifyItemRangeChanged(0, Math.max(newScores.size(), oldScores.size()));
        }
        //binding.scoresRv.invalidate();

        ArrayList<Integer> playerPoints = new ArrayList<>();
        playerPoints.add(0);//game.getIndividualPoints();
        playerPoints.add(0);//game.getIndividualPoints();
        playerPoints.add(0);//game.getIndividualPoints();

        // Set top sum
        binding.pointsBoardTopSums.gamePointsText.setText("-");
        binding.pointsBoardTopSums.points1Text.setText(String.valueOf(playerPoints.get(0)));
        binding.pointsBoardTopSums.points2Text.setText(String.valueOf(playerPoints.get(1)));
        binding.pointsBoardTopSums.points3Text.setText(String.valueOf(playerPoints.get(2)));

        // Bottom sum
        binding.bottomSumView.points1Text.setText(String.valueOf(playerPoints.get(0)));
        binding.bottomSumView.points2Text.setText(String.valueOf(playerPoints.get(1)));
        binding.bottomSumView.points3Text.setText(String.valueOf(playerPoints.get(2)));
    }

    @Override
    public void onResume() {
        ((MainActivity)requireActivity()).disableCollapsingToolbar();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)requireActivity()).enableCollapsingToolbar();
    }

    @Override
    public void notifyDelete() {
        Result<SkatScore> deleteResult = viewModel.removeLastScore();
        if (deleteResult.isFailure()) {
            Snackbar.make(requireView(), deleteResult.getMessage(), Snackbar.LENGTH_SHORT)
                    .setAction("GOT IT", view -> { onDestroy(); })
                    .show();
            return;
        }
        SkatScoreAdapter adapter = (SkatScoreAdapter) binding.scoresRv.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(deleteResult.getValue().getIndex());
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
        findNavController().navigate(R.id.action_navigation_game_to_scoreFragment, bundle);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(SkatGameViewModel.class);
        scoreResponseViewModel = new ViewModelProvider(requireActivity()).get(SharedScoreResponseViewModel.class);

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

        playersViewModel = new ViewModelProvider(this, playersViewModelFactory).get(PlayersViewModel.class);
        playersViewModel.initialize();
    }

    private void setupNavigation() {
        bottomNavigationVisibility = View.GONE;
        showStartGameButton = false;
        showNewGameButton = false;
        showEditScoreButton = true;
    }

    private void showEditDealerDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Who was dealer in the first round?")
                //.setView()
                .setNegativeButton("Cancel", (d, i) -> d.cancel())
                .setPositiveButton("Save", (d, i) -> {
                    d.dismiss();
                })
                .create()
                .show();
    }

    @SuppressLint("InflateParams")
    private void showEditPlayerDialog() {
        View contentView = getLayoutInflater().inflate(R.layout.dialog_player_names, null);
        TextInputLayout input1 = contentView.findViewById(R.id.player1_input);
        TextInputLayout input2 = contentView.findViewById(R.id.player2_input);
        TextInputLayout input3 = contentView.findViewById(R.id.player3_input);
        MaterialAutoCompleteTextView editPlayer1 = contentView.findViewById(R.id.player1_edit_text);
        MaterialAutoCompleteTextView editPlayer2 = contentView.findViewById(R.id.player2_edit_text);
        MaterialAutoCompleteTextView editPlayer3 = contentView.findViewById(R.id.player3_edit_text);

        List<Player> players = viewModel.getPlayers().getValue();
        if (Objects.requireNonNull(players).size() >= 3) {
            editPlayer1.setText(players.get(0).getName());
            editPlayer1.setSelection(editPlayer1.getText().length());
            editPlayer2.setText(players.get(1).getName());
            editPlayer2.setSelection(editPlayer2.getText().length());
            editPlayer3.setText(players.get(2).getName());
            editPlayer3.setSelection(editPlayer3.getText().length());
        }

        List<Player> allPlayers = playersViewModel.getPlayers().getValue();
        if (allPlayers == null) {
            allPlayers = new ArrayList<>();
        }

        ArrayAdapter<Player > adapter = new ArrayAdapter<>(requireContext(),
                R.layout.text_input_list_item, allPlayers);
        editPlayer1.setAdapter(adapter);
        editPlayer2.setAdapter(adapter);
        editPlayer3.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(contentView)
                .setTitle("Edit Players")
                .setNegativeButton("Cancel", (d, i) -> d.cancel())
                .setPositiveButton("Save", (d, i) -> {
                    if (input1.getError() == null &&
                            input2.getError() == null &&
                            input3.getError() == null) {

                        String name1 = editPlayer1.getText().toString().trim();
                        String name2 = editPlayer2.getText().toString().trim();
                        String name3 = editPlayer3.getText().toString().trim();

                        Player p1 = playersViewModel.findPlayerByName(name1);
                        Player p2 = playersViewModel.findPlayerByName(name2);
                        Player p3 = playersViewModel.findPlayerByName(name3);

                        if (p1 == null) {
                            p1 = playersViewModel.addPlayer(name1);
                        }
                        if (p2 == null) {
                            p2 = playersViewModel.addPlayer(name2);
                        }
                        if (p3 == null) {
                            p3 = playersViewModel.addPlayer(name3);
                        }

                        List<Player> newPlayers = Arrays.asList(p1, p2, p3);
                        viewModel.updatePlayers(newPlayers);
                        d.dismiss();
                    }
                })
                .create();

        Button buttonSave = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

        // Add listeners
        addTextChangeListener(editPlayer1, input1, allPlayers, buttonSave);
        addTextChangeListener(editPlayer2, input2, allPlayers, buttonSave);
        addTextChangeListener(editPlayer3, input3, allPlayers, buttonSave);

        dialog.show();
    }

    private void addTextChangeListener(AutoCompleteTextView textView,
                                       TextInputLayout inputLayout,
                                       List<Player> allPlayers,
                                       Button buttonSave) {
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
                String name = s.toString().trim();
                inputLayout.setError(null);
                if (name.isEmpty()) {
                    inputLayout.setError("Name is required!");
                    return;
                }

                if (allPlayers.stream().noneMatch(player -> player.getName().equals(name))) {
                    inputLayout.setHelperText("New player is created!");
                }

                // Check if the current length exceeds the maximum length
                int maxLength = inputLayout.getCounterMaxLength();
                int currentLength = s.length();
                if (currentLength > maxLength) {
                    // Trim the text to the maximum length
                    s.replace(maxLength, currentLength, "");
                }
            }
        });
    }

    private void setUpRecyclerView() {
        binding.scoresRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.scoresRv.setAdapter(new SkatScoreAdapter(new ArrayList<>(), this));
    }

    private void setToolbarTitle(String title) {
        // Set toolbar label
        Objects.requireNonNull(((MainActivity) requireActivity())
                .getSupportActionBar()).setTitle(title);
    }

    private void setUpEditScoreButton() {
        FloatingActionButton editScoreButton = requireActivity().findViewById(R.id.edit_score_button);
        editScoreButton.setOnClickListener(view -> {
            Log.d("Event", "Edit score button clicked.");

            List<Player> players = viewModel.getPlayers().getValue();
            assert players != null;
            Random random = new Random();
            int index = random.nextInt(players.size());
            int points = random.nextInt(39) + 18;
            //SkatScore newScore = new SkatScore(points, players.get(index));
            //viewModel.addScore(newScore);
            //binding.scoresRv.smoothScrollToPosition(
            //        Objects.requireNonNull(viewModel.getGame().getValue()).getScores().size() - 1);

            ScoreRequest scoreRequest = new ScoreRequest.Builder()
                    .setPlayerNames(
                            players.get(0).getName(),
                            players.get(1).getName(),
                            players.get(2).getName())
                    .setPlayerPositions(0, 1, 2)
                    .build();
            Bundle bundle = new Bundle();
            bundle.putParcelable("scoreRequest", scoreRequest);
            findNavController().navigate(R.id.action_navigation_game_to_scoreFragment, bundle);
        });
    }
}