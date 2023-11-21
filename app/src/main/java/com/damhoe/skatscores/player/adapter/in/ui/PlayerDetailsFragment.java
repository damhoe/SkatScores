package com.damhoe.skatscores.player.adapter.in.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.skatscores.MainActivity;
import com.damhoe.skatscores.R;
import com.damhoe.skatscores.base.DateConverter;
import com.damhoe.skatscores.databinding.FragmentPlayerDetailsBinding;
import com.damhoe.skatscores.player.domain.Player;
import com.damhoe.skatscores.shared_ui.utils.InsetsManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import javax.inject.Inject;

public class PlayerDetailsFragment extends Fragment {

    private FragmentPlayerDetailsBinding binding;
    private PlayerViewModel viewModel;
    @Inject
    PlayerViewModelFactory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity)requireActivity()).appComponent.inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player_details, container, false);
        binding.buttonDelete.setOnClickListener(view -> {
            Player player = viewModel.getSelectedPlayer().getValue();
            if (player != null) {
                viewModel.removePlayer(player);
            }
            findNavController().navigateUp();
        });
        binding.editName.setOnClickListener(view -> buildStartAddPlayerDialog());
        return binding.getRoot();
    }

    private void buildStartAddPlayerDialog() {
        View layout = getLayoutInflater().inflate(R.layout.dialog_edit_player_name, null);
        TextInputEditText editText = layout.findViewById(R.id.edit_name);
        Player player = viewModel.getSelectedPlayer().getValue();
        if (player == null) {
            return;
        }
        editText.setText(player.getName());
        editText.setSelection(Objects.requireNonNull(editText.getText()).length());
        TextInputLayout inputLayout = layout.findViewById(R.id.input_name);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_title_edit_player_name))
                .setView(layout)
                .setNegativeButton(getString(R.string.dialog_title_button_cancel), (d, i) -> d.cancel())
                .setPositiveButton(getString(R.string.dialog_title_button_save), (d, i) -> {
                    if (inputLayout.getError() != null) {
                        return;
                    }
                    Editable s = editText.getText();
                    if (s == null) {
                        return;
                    }
                    String name = s.toString().trim();
                    if (!name.equals(player.getName())) {
                        player.setName(name);
                        viewModel.updatePlayer(player);
                    }
                    d.dismiss();
                })
                .create();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                String trimmed = s.toString().trim();
                // Reset error
                inputLayout.setError(null);

                // Show error if empty name
                if (trimmed.isEmpty()) {
                    inputLayout.setError(getString(R.string.error_name_required));
                }

                // Show error if name is not unique
                if (viewModel.isExistentName(trimmed) && !trimmed.equals(player.getName())) {
                    inputLayout.setError(getString(R.string.error_player_name_exists));
                }

                int maxLength = inputLayout.getCounterMaxLength();
                int currentLength = s.length();

                // Check if the current length exceeds the maximum length
                if (currentLength > maxLength) {
                    // Trim the text to the maximum length
                    s.replace(maxLength, currentLength, "");
                }

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(inputLayout.getError() == null);
            }
        });

        dialog.show();
    }

    private NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.content);

        // Setup navigation controller
        NavController navController = findNavController();
        AppBarConfiguration appBarConfiguration =
                ((MainActivity)requireActivity()).getAppBarConfiguration();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
                .get(PlayerViewModel.class);
        viewModel.getSelectedPlayer().observe(getViewLifecycleOwner(), this::updateUI);
        initializeUI();
    }

    private void updateUI(Player player) {
        String gameCountTemplate = getString(R.string.template_game_count_long);
        String createdAtTemplate = getString(R.string.template_created_at);
        String updatedAtTemplate = getString(R.string.template_updated_at);

        binding.name.setText(player.getName());

        binding.created.setText(String.format(createdAtTemplate,
                DateConverter.toAppLocaleString(player.getCreatedAt())
        ));
        binding.updated.setText(String.format(updatedAtTemplate,
                DateConverter.toAppLocaleString(player.getUpdatedAt())));
        binding.numberGames.setText(String.format(gameCountTemplate, player.getGameCount()));
    }

    private void initializeUI() {
        Player player = viewModel.getSelectedPlayer().getValue();
        if (player != null) {
            updateUI(player);
        }
    }
}