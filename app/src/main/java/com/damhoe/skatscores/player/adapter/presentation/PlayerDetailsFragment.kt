package com.damhoe.skatscores.player.adapter.presentation

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.base.DateConverter
import com.damhoe.skatscores.databinding.FragmentPlayerDetailsBinding
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@AndroidEntryPoint
class PlayerDetailsFragment :
    Fragment(R.layout.fragment_player_details)
{
    private lateinit var binding: FragmentPlayerDetailsBinding
    private val viewModel: PlayerViewModel by viewModels()
    private val args: PlayerDetailsFragmentArgs by navArgs()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    )
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayerDetailsBinding.bind(view)

        args.playerId.let {
            viewModel.loadPlayerById(UUID.fromString(it))
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.playerDetails.value?.let { player ->
                viewModel.removePlayer(player)
                findNavController().navigateUp()
            }
        }
        binding.buttonStats.setOnClickListener {
            val directions =
                PlayerDetailsFragmentDirections.actionPlayerDetailsToPlayerStatistics()
            findNavController().navigate(directions)
        }
        binding.editName.setOnClickListener { showEditPlayerDialog() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        InsetsManager.applyStatusBarInsets(binding.toolbar)
        InsetsManager.applyNavigationBarInsets(binding.content)

        viewModel.playerDetails.observe(viewLifecycleOwner) { player: Player? ->
            player?.let {
                this.updateUI(it)
            } ?: run {
                findNavController().navigateUp()
            }
        }
    }

    private fun showEditPlayerDialog()
    {
        val currentPlayer = viewModel.playerDetails.value ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.edit_name)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_name)

        editText.setText(currentPlayer.name.value)
        editText.setSelection(editText.text?.length ?: 0)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_edit_player_name))
            .setView(dialogView)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.background_dialog_fragment,
                    requireActivity().theme
                )
            )
            .setNegativeButton(getString(R.string.dialog_title_button_cancel), null)
            .setPositiveButton(getString(R.string.dialog_title_button_save)) { _, _ ->

                val newName = editText.text.toString().trim()
                if (newName != currentPlayer.name.value)
                {
                    val newPlayerName = PlayerName.create(newName).getOrThrow()
                    val updatedPlayer = currentPlayer.updateName(newPlayerName)
                    viewModel.updatePlayer(updatedPlayer)
                }
            }
            .create()

        editText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable?)
            {
                val currentText = s.toString().trim()
                inputLayout.error = null // Clear previous errors

                if (currentPlayer.name.value == currentText)
                {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
                    return
                }

                when
                {
                    currentText.isEmpty() ->
                    {
                        inputLayout.error = getString(R.string.error_name_has_wrong_size)
                    }

                    viewModel.isPlayerNameTaken(currentText) ->
                    {
                        inputLayout.error = getString(R.string.error_name_exists_already)
                    }

                    currentText.length > inputLayout.counterMaxLength ->
                    {
                        s?.delete(inputLayout.counterMaxLength, currentText.length)

                        inputLayout.error = getString(R.string.error_name_has_wrong_size)
                    }
                }

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                    inputLayout.error == null && currentPlayer.name.value != currentText
            }
        })

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
        }

        dialog.show()
    }

    private fun updateUI(player: Player)
    {
        binding.name.text = player.name.value

        val systemZoneId: ZoneId = ZoneId.systemDefault()
        val localizedFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        binding.created.text = String.format(
            getString(R.string.template_created_at),
            player.createdAt
                .atZone(systemZoneId)
                .format(localizedFormatter)
        )

        binding.numberGames.text = String.format(
            getString(R.string.template_game_count_long),
            viewModel.playerStatistics.value?.totalGamesPlayed
        )
    }
}