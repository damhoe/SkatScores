package com.damhoe.skatscores.player.adapter.presentation

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayersBinding
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class PlayersFragment :
    Fragment(R.layout.fragment_players),
    NotifyItemClickListener
{
    private lateinit var binding: FragmentPlayersBinding
    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPlayersBinding.bind(view)
        InsetsManager.applyStatusBarInsets(binding.toolbar)
        InsetsManager.applyNavigationBarInsets(binding.content)

        val playerAdapter = PlayerAdapter(this)

        binding.playerRecyclerView.apply {
            adapter = playerAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(PlayerAdapter.ItemDecoration())
        }

        binding.addPlayerButton.setOnClickListener { showAddPlayerDialog() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        viewModel.players.observe(viewLifecycleOwner) {
            playerAdapter.submitList(
                it.map { player ->
                    PlayerInfo(player.id, player.name, 0)
                })
        }
    }

    private fun showAddPlayerDialog()
    {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        val editText: TextInputEditText = dialogView.findViewById(R.id.edit_name)
        val inputLayout: TextInputLayout = dialogView.findViewById(R.id.input_name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_create_player))
            .setView(dialogView)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.background_dialog_fragment,
                    requireActivity().theme
                )
            )
            .setNegativeButton(getString(R.string.dialog_title_button_cancel), null)
            .setPositiveButton(getString(R.string.dialog_title_button_create)) { _, _ ->
                val nameString = editText.text.toString().trim()

                if (inputLayout.error == null)
                {
                    val playerName = PlayerName.create(nameString).getOrThrow()
                    viewModel.addPlayer(playerName)
                }
            }
            .create()

        editText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            { /* Ignore */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            { /* Ignore */
            }

            override fun afterTextChanged(s: Editable?)
            {
                val currentText = s.toString().trim()
                inputLayout.error = null // Clear previous errors

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
                    inputLayout.error == null
            }
        })

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
        }

        dialog.show()
    }

    override fun notifyItemClick(
        playerId: UUID, position: Int
    )
    {
        findNavController().navigate(
            PlayersFragmentDirections
                .actionPlayersFragmentToPlayerDetails(playerId.toString())
        )
    }
}