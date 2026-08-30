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
import com.damhoe.skatscores.game.common.GameType
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.NewDoppelkopfListSheetFragment
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
    private fun askBeforeDeleting(preview: ListPreview)
    {
        requireContext().confirmListDeletion(
            listName = preview.title.value,
            roundsPlayed = preview.roundsPlayed,
        ) {
            viewModel.confirmDelete(preview)
            Snackbar
                .make(binding.root, R.string.message_list_deleted, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.bottomPill)
                .show()
        }
    }

    /**
     * Game-type filters. The two games keep separate lists, separate setup and separate
     * scoring, so this row picks which of them the whole screen is about.
     */
    private fun setupGameTypeFilters()
    {
        binding.filterSkat.setOnClickListener { viewModel.selectGameType(GameType.SKAT) }
        binding.filterDoppelkopf.setOnClickListener {
            viewModel.selectGameType(GameType.DOPPELKOPF)
        }
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
        viewModel.gameType.observe(viewLifecycleOwner) { gameType ->
            binding.filterSkat.isChecked = gameType == GameType.SKAT
            binding.filterDoppelkopf.isChecked = gameType == GameType.DOPPELKOPF
            binding.addButton.contentDescription = getString(
                if (gameType == GameType.DOPPELKOPF) R.string.description_new_doppelkopf_list
                else R.string.description_new_list
            )
        }

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
            event.getContentIfNotHandled()?.let { navigateToGame(it.gameType, it.gameId) }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomPill)
                    .show()
            }
        }
    }

    private fun bindActiveList(preview: ListPreview?)
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

            // The fourth column only belongs to a Doppelkopf table.
            val hasFourthSeat = preview.playerNames.size > 3
            player4Column.visibility = if (hasFourthSeat) View.VISIBLE else View.GONE
            if (hasFourthSeat) bindStanding(player4Name, player4Total, preview, seat = 3)

            roundProgress.setProgressCompat(
                (preview.progress * 100).toInt(),
                /* animated = */ true
            )
            progressLabel.text = progressLabelFor(preview)

            root.setOnClickListener { navigateToGame(preview.gameType, preview.gameId) }
            keepPlayingButton.setOnClickListener {
                navigateToGame(preview.gameType, preview.gameId)
            }
        }
    }

    private fun bindStanding(
        nameView: TextView,
        totalView: TextView,
        preview: ListPreview,
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

    private fun progressLabelFor(preview: ListPreview): String
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
            listOf(avatar1, avatar2, avatar3, avatar4).forEachIndexed { seat, avatar ->
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
     * Opens the new-list sheet for the game currently selected, prefilled from the most recent
     * list so the usual case is "change nothing and hit Start".
     */
    private fun showNewListSheet()
    {
        val template = viewModel.quickStartTemplate.value
        val suggestedTitle = template?.let { viewModel.suggestedTitleFor(it) }
        val suggestedRoundCount = template?.totalRounds

        when (viewModel.gameType.value ?: GameType.Default)
        {
            GameType.SKAT -> NewListSheetFragment
                .newInstance(suggestedTitle, suggestedRoundCount)
                .show(parentFragmentManager, NewListSheetFragment.TAG)

            GameType.DOPPELKOPF -> NewDoppelkopfListSheetFragment
                .newInstance(suggestedTitle, suggestedRoundCount)
                .show(parentFragmentManager, NewDoppelkopfListSheetFragment.TAG)
        }
    }

    private fun listenForNewLists()
    {
        parentFragmentManager.setFragmentResultListener(
            NewListSheetFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(NewListSheetFragment.RESULT_GAME_ID)
                ?.let { navigateToGame(GameType.SKAT, UUID.fromString(it)) }
        }

        parentFragmentManager.setFragmentResultListener(
            NewDoppelkopfListSheetFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(NewDoppelkopfListSheetFragment.RESULT_GAME_ID)
                ?.let { navigateToGame(GameType.DOPPELKOPF, UUID.fromString(it)) }
        }
    }

    private fun navigateToGame(gameType: GameType, gameId: UUID) = when (gameType)
    {
        GameType.SKAT -> findNavController().navigate(
            LibraryFragmentDirections.actionLibraryFragmentToSkatGameNavGraph(gameId.toString())
        )

        GameType.DOPPELKOPF -> findNavController().navigate(
            LibraryFragmentDirections
                .actionLibraryFragmentToDoppelkopfGameNavGraph(gameId.toString())
        )
    }

    private fun navigateToPlayers() = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryToPlayersFragment()
    )

    override fun notifyDelete(preview: ListPreview) = askBeforeDeleting(preview)

    override fun notifySelect(preview: ListPreview) =
        navigateToGame(preview.gameType, preview.gameId)
}
