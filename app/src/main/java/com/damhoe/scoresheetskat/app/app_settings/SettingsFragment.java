package com.damhoe.scoresheetskat.app.app_settings;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentSettingsBinding;
import com.damhoe.scoresheetskat.game.Constants;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.slider.Slider;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(new Locale("de"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_settings, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        // Setup toolbar
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                ((MainActivity)requireActivity()).getAppBarConfiguration();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
        setupMenu();

        // Get ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        final Observer<String> languageObserver = newLanguage -> {
            Log.d("Setting changed", "Language was set to " + newLanguage);
            binding.languageText.setText(newLanguage);
        };

        viewModel.getLanguage().observe(getViewLifecycleOwner(), languageObserver);

        final Observer<Boolean> unfinishedAsBadgeObserver = shouldShowAsBatch -> {
            Log.d("Setting changed", "Should show unfinished as badge set to " +
                    shouldShowAsBatch.toString());
            binding.switchMaterial.setChecked(shouldShowAsBatch);
        };
        viewModel.shouldShowUnfinishedAsBadge().observe(getViewLifecycleOwner(), unfinishedAsBadgeObserver);

        final Observer<Boolean> scoringObserver = isTournamentScoring -> {
            Log.d("Setting changed", "Tournament scoring set to " +
                    isTournamentScoring.toString());
            binding.scoringText.setText(isTournamentScoring ? "Tournament" : "Simple");
        };
        viewModel.isTournamentScoring().observe(getViewLifecycleOwner(), scoringObserver);

        final Observer<Integer> roundsObserver = numberOfRounds -> {
            Log.d("Setting changed", "Number of rounds set to " + numberOfRounds);
            binding.numberRoundsText.setText(String.valueOf(numberOfRounds));
        };
        viewModel.getNumberOfRounds().observe(getViewLifecycleOwner(), roundsObserver);

        final Observer<Integer> playersObserver = numberOfPlayers -> {
            Log.d("Setting changed", "Number of players set to " + numberOfPlayers);
            binding.numberPlayersText.setText(String.valueOf(numberOfPlayers));
        };
        viewModel.getNumberOfPlayers().observe(getViewLifecycleOwner(), playersObserver);

        setUpEditLanguageDialog();
        setUpUnfinishedAsBatchSwitch();
        setUpScoringDialog();
        setUpRoundsDialog();
        setUpPlayersDialog();
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    private void setupMenu() {
        binding.toolbar.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.settings_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_about) {
                    findNavController().navigate(R.id.action_navigation_settings_to_aboutFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void setUpPlayersDialog() {
        binding.itemNumberPlayers.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(R.string.message_dialog_number_players);

            // Create a radio group
            @SuppressLint("InflateParams")
            View contentView = getLayoutInflater().inflate(R.layout.dialog_vertical_radio_group, null);
            RadioGroup radioGroup = contentView.findViewById(R.id.radio_group);
            for (int i = 0; i < Constants.ALLOWED_NUMBER_OF_PLAYERS.length; i++) {
                MaterialRadioButton radioButton = new MaterialRadioButton(
                        new ContextThemeWrapper(requireContext(), R.style.Widget_App_RadioButton));
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                radioButton.setLayoutParams(params);
                radioButton.setText(String.valueOf(Constants.ALLOWED_NUMBER_OF_PLAYERS[i]));
                radioButton.setId(i);  // Set the radio button's ID to its index
                radioGroup.addView(radioButton);
            }
            radioGroup.check(Arrays.asList(Constants.ALLOWED_NUMBER_OF_PLAYERS)
                    .indexOf(viewModel.getNumberOfRounds().getValue()));

            // Add the radio group to the dialog's view
            builder.setView(contentView);

            builder.setPositiveButton("OK", (dialog, which) -> {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    int selectedNumber = Constants.ALLOWED_NUMBER_OF_PLAYERS[selectedId];
                    viewModel.setNumberOfPlayers(selectedNumber);
                }
                dialog.dismiss();
            });

            builder.setNegativeButton("Cancel", (d, i) -> d.cancel());
            builder.create().show();
        });
    }

    private void setUpRoundsDialog() {
        binding.itemNumberRounds.setOnClickListener(view -> {
            @SuppressLint("InflateParams")
                    View roundsView = getLayoutInflater().inflate(R.layout.dialog_rounds, null);
            Slider slider = (Slider) roundsView.findViewById(R.id.rounds_seekbar);

            //viewModel.getNumberOfRounds()
            //if ( != null) {
            //    slider.setValue(viewModel.getNumberOfRounds().getValue());
            //}

            TextView roundsText = (TextView) roundsView.findViewById(R.id.number_of_rounds_text);
            roundsText.setText(String.valueOf(
                    Objects.requireNonNull(viewModel.getNumberOfRounds().getValue())));

            slider.addOnChangeListener(new Slider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull Slider slider, float numberOfRounds, boolean fromUser) {
                    roundsText.setText(String.valueOf((int) numberOfRounds));
                }
            });

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.message_dialog_rounds)
                    .setView(roundsView)
                    .setNegativeButton("Cancel", (d, i) -> d.cancel())
                    .setPositiveButton("Save", (d, i) -> {
                        int numberOfRounds = (int) slider.getValue();
                        viewModel.setNumberOfRounds(numberOfRounds);
                        d.dismiss();
                    })
                    .create()
                    .show();
        });
    }

    /** @noinspection DataFlowIssue*/
    private void setUpScoringDialog() {
        binding.itemScoring.setOnClickListener(view -> {
            @SuppressLint("InflateParams")
            View scoringView = getLayoutInflater().inflate(R.layout.dialog_scoring, null);
            MaterialButtonToggleGroup toggleGroup = (MaterialButtonToggleGroup) scoringView.findViewById(R.id.scoring_settings_rg);
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    ((MaterialButton)toggleGroup.findViewById(checkedId)).setIcon(
                            ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.ic_done_black_24dp, null));
                }
                else {
                    ((MaterialButton)toggleGroup.findViewById(checkedId)).setIcon(null);
                }
            });

            toggleGroup.check(viewModel.isTournamentScoring().getValue() ?
                    R.id.tournament_scoring_rb : R.id.simple_scoring_rb);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.message_scoring_dialog)
                    .setView(scoringView)
                    .setPositiveButton("Save", (dialogInterface, i) -> {
                        int buttonId = toggleGroup.getCheckedButtonId();
                        viewModel.setTournamentScoring(buttonId != R.id.simple_scoring_rb);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("Cancel", ((d, i) -> d.cancel()))
                    .create()
                    .show();
        });
    }

    private void setUpUnfinishedAsBatchSwitch() {
        binding.switchMaterial.setOnCheckedChangeListener((view, bool) -> {
            viewModel.shouldShowUnfinishedAsBadge(bool);
        });
    }

    private void setUpEditLanguageDialog() {
        binding.itemLanguage.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(R.string.message_language_dialog);

            // Create a radio group
            String[] languages = getResources().getStringArray(R.array.app_languages);
            @SuppressLint("InflateParams")
            View contentView = getLayoutInflater().inflate(R.layout.dialog_vertical_radio_group, null);
            RadioGroup radioGroup = contentView.findViewById(R.id.radio_group);
            for (int i = 0; i < languages.length; i++) {
                MaterialRadioButton radioButton = new MaterialRadioButton(
                        new ContextThemeWrapper(requireContext(), R.style.Widget_App_RadioButton));
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                radioButton.setLayoutParams(params);
                radioButton.setText(languages[i]);
                radioButton.setId(i);  // Set the radio button's ID to its index
                radioGroup.addView(radioButton);
            }
            radioGroup.check(Arrays.asList(languages).indexOf(viewModel.getLanguage().getValue()));

            // Add the radio group to the dialog's view
            builder.setView(contentView);

            builder.setPositiveButton("OK", (dialog, which) -> {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    String selectedLanguage = languages[selectedId];
                    viewModel.setLanguage(selectedLanguage);
                }
                dialog.dismiss();
            });

            builder.setNegativeButton("Cancel", (d, i) -> d.cancel());
            builder.create().show();
        });
    }
}