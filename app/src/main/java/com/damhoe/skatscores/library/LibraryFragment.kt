package com.damhoe.skatscores.library

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentLibraryBinding
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.shared.asListDate
import com.damhoe.skatscores.shared.confirmListDeletion
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.UUID

@AndroidEntryPoint
class LibraryFragment :
    Fragment(R.layout.fragment_library),
    GamePreviewItemClickListener
{
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var binding: FragmentLibraryBinding
    private lateinit var gamePreviewAdapter: GamePreviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryBinding.bind(view)

        applyInsets()
        setupGreeting()
        setupRecyclerView()
        setupGameTypeFilters()
        setupBottomBar()
        setupObservers()
        listenForNewLists()
    }

    /**
     * One listener for the whole screen: the helpers in InsetsManager each consume the insets,
     * so they cannot be combined on sibling views.
     */
    private fun applyInsets()
    {
        val scrollBottomPadding = binding.content.paddingBottom
        val pillBottomMargin =
            (binding.bottomPill.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.content.setPadding(
                binding.content.paddingLeft,
                bars.top,
                binding.content.paddingRight,
                scrollBottomPadding + bars.bottom
            )
            binding.bottomPill.updateLayoutParams<android.view.ViewGroup.MarginLayoutParams> {
                bottomMargin = pillBottomMargin + bars.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupGreeting()
    {
        binding.greeting.setText(greetingForTimeOfDay())
    }

    private fun greetingForTimeOfDay() = when (LocalTime.now().hour)
    {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        in 18..22 -> R.string.greeting_evening
        else -> R.string.greeting_night
    }

    private fun setupRecyclerView()
    {
        gamePreviewAdapter = GamePreviewAdapter(this)
        binding.gamesRv.apply {
            adapter = gamePreviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                GamePreviewAdapter.DividerDecoration(
                    MaterialColors.getColor(this, R.attr.colorSurfaceContainer),
                    resources.getDimensionPixelSize(R.dimen.screen_side_padding),
                )
            )
            itemAnimator = null
        }
    }

    /** Long press on a row: confirm, then the list is gone. */
    private fun askBeforeDeleting(preview: SkatGamePreview)
    {
        requireContext().confirmListDeletion(
            listName = preview.title.value,
            roundsPlayed = preview.roundsPlayed,
        ) {
            viewModel.confirmDelete(preview.gameId)
            Snackbar
                .make(binding.root, R.string.message_list_deleted, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.bottomPill)
                .show()
        }
    }

    /**
     * Game-type filters. Skat is the only scoring the app has, so the row is presentational
     * for now: Skat stays selected and Doppelkopf is disabled rather than silently doing
     * nothing when tapped.
     */
    private fun setupGameTypeFilters()
    {
        binding.filterSkat.isChecked = true
        binding.filterSkat.setOnClickListener { binding.filterSkat.isChecked = true }

        binding.filterDoppelkopf.isChecked = false
        binding.filterDoppelkopf.isEnabled = false
        binding.filterDoppelkopf.contentDescription =
            getString(R.string.description_game_type_unavailable)
    }

    private fun setupBottomBar()
    {
        binding.settingsButton.setOnClickListener { showAppSettingsDialog() }
        binding.addButton.setOnClickListener { showNewListSheet() }
        binding.playersTab.setOnClickListener { navigateToPlayers() }
        binding.listsTab.setOnClickListener { binding.content.smoothScrollTo(0, 0) }
        binding.showAllLink.setOnClickListener { viewModel.showAllRecent() }
    }

    private fun setupObservers()
    {
        viewModel.activeList.observe(viewLifecycleOwner) { bindActiveList(it) }

        viewModel.quickStartTemplate.observe(viewLifecycleOwner) { template ->
            val hasTemplate = template != null
            binding.quickStartLabel.visibility = if (hasTemplate) View.VISIBLE else View.GONE
            binding.quickStart.root.visibility = if (hasTemplate) View.VISIBLE else View.GONE

            if (template == null) return@observe

            bindAvatars(template.playerNames)
            binding.quickStart.root.setOnClickListener { viewModel.quickStart(template) }
        }

        viewModel.recentGames.observe(viewLifecycleOwner) { previews ->
            gamePreviewAdapter.submitList(previews)
            binding.recentHeader.visibility =
                if (previews.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.canShowMoreRecent.observe(viewLifecycleOwner) { canShowMore ->
            binding.showAllLink.visibility = if (canShowMore) View.VISIBLE else View.GONE
        }

        viewModel.navigateToGame.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { navigateToGame(it) }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomPill)
                    .show()
            }
        }
    }

    private fun bindActiveList(preview: SkatGamePreview?)
    {
        val hasActiveList = preview != null
        binding.activeList.root.visibility = if (hasActiveList) View.VISIBLE else View.GONE
        binding.noRunningListHint.visibility = if (hasActiveList) View.GONE else View.VISIBLE

        if (preview == null) return

        binding.activeList.apply {
            listName.text = preview.title.value
            listDate.text = preview.playedAt.asListDate()

            bindStanding(player1Name, player1Total, preview, seat = 0)
            bindStanding(player2Name, player2Total, preview, seat = 1)
            bindStanding(player3Name, player3Total, preview, seat = 2)

            roundProgress.setProgressCompat(
                (preview.progress * 100).toInt(),
                /* animated = */ true
            )
            progressLabel.text = progressLabelFor(preview)

            root.setOnClickListener { navigateToGame(preview.gameId) }
            keepPlayingButton.setOnClickListener { navigateToGame(preview.gameId) }
        }
    }

    private fun bindStanding(
        nameView: TextView,
        totalView: TextView,
        preview: SkatGamePreview,
        seat: Int
    )
    {
        nameView.text = preview.playerNames.getOrNull(seat).orEmpty()

        val total = preview.totals.getOrNull(seat) ?: 0
        totalView.text = if (total > 0) "+$total" else total.toString()
        totalView.setTextColor(
            if (total < 0) requireContext().getColor(R.color.hero_negative)
            else requireContext().getColor(R.color.hero_on_surface)
        )
    }

    private fun progressLabelFor(preview: SkatGamePreview): String
    {
        val rounds = getString(
            R.string.label_round_progress,
            preview.roundsPlayed,
            preview.totalRounds
        )
        val dealer = preview.dealerName
            ?.let { getString(R.string.label_dealer, it) }
            ?: return rounds

        return getString(R.string.format_list_meta, rounds, dealer)
    }

    private fun bindAvatars(playerNames: List<String>)
    {
        binding.quickStart.apply {
            listOf(avatar1, avatar2, avatar3).forEachIndexed { seat, avatar ->
                val name = playerNames.getOrNull(seat)
                avatar.visibility = if (name == null) View.GONE else View.VISIBLE
                avatar.text = name?.take(1)?.uppercase().orEmpty()
            }
        }
    }

    private fun showAppSettingsDialog() = findNavController().navigate(
        LibraryFragmentDirections.actionHomeToAppSettings()
    )

    /**
     * Opens the new-list sheet, prefilled from the most recent list so the usual case is
     * "change nothing and hit Start".
     */
    private fun showNewListSheet()
    {
        val template = viewModel.quickStartTemplate.value

        NewListSheetFragment
            .newInstance(
                suggestedTitle = template?.let { viewModel.suggestedTitleFor(it) },
                suggestedRoundCount = template?.totalRounds,
            )
            .show(parentFragmentManager, NewListSheetFragment.TAG)
    }

    private fun listenForNewLists()
    {
        parentFragmentManager.setFragmentResultListener(
            NewListSheetFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(NewListSheetFragment.RESULT_GAME_ID)
                ?.let { navigateToGame(UUID.fromString(it)) }
        }
    }

    private fun navigateToGame(gameId: UUID) = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryFragmentToSkatGameNavGraph(gameId.toString())
    )

    private fun navigateToPlayers() = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryToPlayersFragment()
    )

    override fun notifyDelete(skatGamePreview: SkatGamePreview) = askBeforeDeleting(skatGamePreview)

    override fun notifySelect(skatGamePreview: SkatGamePreview) =
        navigateToGame(skatGamePreview.gameId)
}
