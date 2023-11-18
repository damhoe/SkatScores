package com.damhoe.scoresheetskat.player.adapter.in.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.damhoe.scoresheetskat.MainActivity;
import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentPlayersBinding;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;
import com.damhoe.scoresheetskat.shared_ui.utils.LayoutMargins;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import javax.inject.Inject;

public class PlayersFragment extends Fragment implements NotifyItemClickListener {

    private FragmentPlayersBinding binding;
    private PlayerViewModel viewModel;
    @Inject
    PlayerViewModelFactory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_players, container, false);
        View root = binding.getRoot();

        PlayerAdapter playerAdapter = new PlayerAdapter(this);
        binding.playerRecyclerView.setAdapter(playerAdapter);
        binding.playerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.playerRecyclerView.addItemDecoration(new PlayerAdapter.ItemDecoration());

        return binding.getRoot();
    }

    private void buildStartAddPlayerDialog() {
        View layout = getLayoutInflater().inflate(R.layout.dialog_edit_player_name, null);
        TextInputEditText editText = layout.findViewById(R.id.edit_name);
        TextInputLayout inputLayout = layout.findViewById(R.id.input_name);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Create player")
                .setView(layout)
                .setNegativeButton("Cancel", (d, i) -> d.cancel())
                .setPositiveButton("Create", (d, i) -> {
                    if (inputLayout.getError() != null) {
                        return;
                    }
                    Editable s = editText.getText();
                    if (s == null) {
                        return;
                    }
                    String name = s.toString().trim();
                    viewModel.addPlayer(name);
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
                // Reset error
                inputLayout.setError(null);

                // Show error if empty name
                if (s.toString().trim().isEmpty()) {
                    inputLayout.setError("Name is required!");
                }

                // Show error if name is not unique
                if (viewModel.isExistentName(s.toString().trim())) {
                    inputLayout.setError("This name already exists.");
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

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        dialog.show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((MainActivity) requireActivity()).appComponent.inject(this);
        super.onAttach(context);
    }

    public NavController findNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout);
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView);

        // Load default button margins for resource values
        int bottomMargin =
                (int) getResources().getDimension(R.dimen.fab_above_bottom_nav_margin_bottom);
        int rightMargin =
                (int) getResources().getDimension(R.dimen.fab_margin_right);
        LayoutMargins margins = new LayoutMargins(
                0,
                0,
                rightMargin,
                bottomMargin
        );
        InsetsManager.applyNavigationBarInsets(binding.addButton, margins);

        binding.addButton.setOnClickListener(v -> {
            buildStartAddPlayerDialog();
        });


        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(PlayerViewModel.class);
        viewModel.getPlayers().observe(getViewLifecycleOwner(), new Observer<List<Player>>() {
            /** @noinspection DataFlowIssue*/
            @Override
            public void onChanged(List<Player> players) {
                ((PlayerAdapter)binding.playerRecyclerView.getAdapter()).setPlayers(players);
                binding.playerRecyclerView.invalidate();
            }
        });
    }

    @Override
    public void notifyItemClick(Player player, int position) {
        viewModel.selectPlayer(player);
        findNavController().navigate(R.id.action_players_to_player_details);
    }
}