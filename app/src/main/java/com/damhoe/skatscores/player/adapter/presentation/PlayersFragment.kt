package com.damhoe.skatscores.player.adapter.presentation

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayersBinding
import com.damhoe.skatscores.library.GamePreviewAdapter
import com.damhoe.skatscores.player.domain.PlayerName
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class PlayersFragment :
    Fragment(R.layout.fragment_players),
    NotifyItemClickListener
{
    private lateinit var binding: FragmentPlayersBinding
    private lateinit var playerAdapter: PlayerAdapter

    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayersBinding.bind(view)

        applyInsets()
        setupRecyclerView()
        setupBottomBar()
        setupObservers()
        listenForNewName()
    }

    private fun applyInsets()
    {
        val listBottomPadding = binding.playerRecyclerView.paddingBottom
        val pillBottomMargin =
            (binding.bottomPill.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            binding.playerRecyclerView.setPadding(
                binding.playerRecyclerView.paddingLeft,
                binding.playerRecyclerView.paddingTop,
                binding.playerRecyclerView.paddingRight,
                listBottomPadding + bars.bottom
            )
            binding.bottomPill.updateLayoutParams<android.view.ViewGroup.MarginLayoutParams> {
                bottomMargin = pillBottomMargin + bars.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupRecyclerView()
    {
        playerAdapter = PlayerAdapter(this)
        binding.playerRecyclerView.apply {
            adapter = playerAdapter
            layoutManager = LinearLayoutManager(context)
            // Same hairline between rows as the recent lists on home.
            addItemDecoration(
                GamePreviewAdapter.DividerDecoration(
                    MaterialColors.getColor(this, R.attr.colorSurfaceContainer),
                    resources.getDimensionPixelSize(R.dimen.screen_side_padding),
                )
            )
            itemAnimator = null
        }
    }

    private fun setupBottomBar()
    {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.listsTab.setOnClickListener { findNavController().navigateUp() }
        binding.playersTab.setOnClickListener {
            binding.playerRecyclerView.smoothScrollToPosition(0)
        }
        binding.addPlayerButton.setOnClickListener { showAddPlayerSheet() }
    }

    private fun setupObservers()
    {
        viewModel.playerInfos.observe(viewLifecycleOwner) { players ->
            playerAdapter.submitList(players)

            val isEmpty = players.isEmpty()
            binding.textNoPlayers.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.playerRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun showAddPlayerSheet()
    {
        PlayerNameSheetFragment
            .newInstance(
                titleRes = R.string.dialog_title_create_player,
                takenNames = viewModel.playerNames(),
            )
            .show(parentFragmentManager, PlayerNameSheetFragment.TAG)
    }

    private fun listenForNewName()
    {
        parentFragmentManager.setFragmentResultListener(
            PlayerNameSheetFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(PlayerNameSheetFragment.RESULT_NAME)
                ?: return@setFragmentResultListener
            PlayerName.create(name).onSuccess { viewModel.addPlayer(it) }
        }
    }

    override fun notifyItemClick(playerId: UUID, position: Int)
    {
        findNavController().navigate(
            PlayersFragmentDirections
                .actionPlayersFragmentToPlayerDetails(playerId.toString())
        )
    }
}
