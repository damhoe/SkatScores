package com.damhoe.skatscores.player.adapter.presentation

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayerDetailsBinding
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@AndroidEntryPoint
class PlayerDetailsFragment : Fragment(R.layout.fragment_player_details)
{
    private lateinit var binding: FragmentPlayerDetailsBinding

    private val viewModel: PlayerViewModel by viewModels()
    private val args: PlayerDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayerDetailsBinding.bind(view)

        viewModel.loadPlayerById(UUID.fromString(args.playerId))

        applyInsets()
        setupListeners()
        setupObservers()
        listenForNewName()
    }

    private fun applyInsets()
    {
        val contentBottomPadding = binding.content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            binding.content.setPadding(
                binding.content.paddingLeft,
                binding.content.paddingTop,
                binding.content.paddingRight,
                contentBottomPadding + bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupListeners()
    {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.editName.setOnClickListener { showRenameSheet() }
        binding.buttonStats.setOnClickListener {
            findNavController().navigate(
                PlayerDetailsFragmentDirections
                    .actionPlayerDetailsToPlayerStatistics(args.playerId)
            )
        }
        binding.buttonDelete.setOnClickListener {
            viewModel.playerDetails.value?.let { player ->
                viewModel.removePlayer(player)
                findNavController().navigateUp()
            }
        }
    }

    private fun setupObservers()
    {
        viewModel.playerDetails.observe(viewLifecycleOwner) { player ->
            player?.let { updateUi(it) } ?: findNavController().navigateUp()
        }

        // Statistics arrive after the player, so the count is bound separately.
        viewModel.playerStatistics.observe(viewLifecycleOwner) { statistics ->
            val played = statistics?.totalGamesPlayed ?: 0
            binding.numberGames.text = resources.getQuantityString(
                R.plurals.label_player_list_count,
                played,
                played
            )
        }
    }

    private fun updateUi(player: Player)
    {
        binding.headerName.text = player.name.value
        binding.name.text = player.name.value
        binding.initial.text = player.name.value.take(1).uppercase()
        PlayerAvatar.bind(binding.initial, player.id)

        binding.created.text = getString(
            R.string.template_created_at,
            player.createdAt
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        )
    }

    private fun showRenameSheet()
    {
        val player = viewModel.playerDetails.value ?: return

        PlayerNameSheetFragment
            .newInstance(
                titleRes = R.string.dialog_title_edit_player_name,
                initialName = player.name.value,
                // The player keeps their own name, so it must not read as taken.
                takenNames = viewModel.playerNames().filterNot { it == player.name.value },
            )
            .show(parentFragmentManager, PlayerNameSheetFragment.TAG)
    }

    private fun listenForNewName()
    {
        parentFragmentManager.setFragmentResultListener(
            PlayerNameSheetFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val player = viewModel.playerDetails.value ?: return@setFragmentResultListener
            val name = bundle.getString(PlayerNameSheetFragment.RESULT_NAME)
                ?: return@setFragmentResultListener

            PlayerName.create(name).onSuccess { viewModel.updatePlayer(player.updateName(it)) }
        }
    }
}
