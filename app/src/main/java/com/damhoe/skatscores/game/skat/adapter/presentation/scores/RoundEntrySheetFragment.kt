package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetRoundEntryBinding
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.scores.RoundDraft
import com.damhoe.skatscores.game.skat.domain.scores.RoundGame
import com.damhoe.skatscores.game.skat.domain.scores.RoundResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

/**
 * Enters a new round or edits an existing one on a single surface, with a live points preview.
 * Replaces the who-won / result / suit / spitzen / multipliers fragment chain.
 */
@AndroidEntryPoint
class RoundEntrySheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: RoundEntryViewModel by viewModels()
    private lateinit var binding: SheetRoundEntryBinding

    private val biddingValues = SkatBid.BiddingValues.toList()

    /** Set while pushing state into the controls, so their listeners do not echo back. */
    private var isBinding = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetRoundEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setupBidPicker()
        setupListeners()
        setupObservers()
    }

    private fun setupBidPicker()
    {
        binding.bidPicker.apply {
            minValue = 0
            maxValue = biddingValues.size - 1
            displayedValues = biddingValues.map { it.toString() }.toTypedArray()
            wrapSelectorWheel = false
            setOnValueChangedListener { _, _, index ->
                if (!isBinding) viewModel.setBid(SkatBid(biddingValues[index]))
            }
        }
    }

    private fun setupListeners()
    {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { viewModel.save() }

        binding.declarerPasse.setOnClickListener { viewModel.setDeclarer(null) }

        binding.gameDiamonds.setOnClickListener { viewModel.setGame(RoundGame.DIAMONDS) }
        binding.gameHearts.setOnClickListener { viewModel.setGame(RoundGame.HEARTS) }
        binding.gameSpades.setOnClickListener { viewModel.setGame(RoundGame.SPADES) }
        binding.gameClubs.setOnClickListener { viewModel.setGame(RoundGame.CLUBS) }
        binding.gameGrand.setOnClickListener { viewModel.setGame(RoundGame.GRAND) }
        binding.gameNull.setOnClickListener { viewModel.setGame(RoundGame.NULL) }

        binding.resultWon.setOnClickListener { viewModel.setResult(RoundResult.WON) }
        binding.resultLost.setOnClickListener { viewModel.setResult(RoundResult.LOST) }
        binding.resultOverbid.setOnClickListener { viewModel.setResult(RoundResult.OVERBID) }

        binding.spitzenSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setSpitzen(value.toInt())
        }

        binding.modifierHand.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setHand(checked)
        }
        binding.modifierSchneider.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setSchneider(checked)
        }
        binding.modifierSchwarz.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setSchwarz(checked)
        }
        binding.modifierAnnounced.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setAnnounced(checked)
        }
        binding.modifierOuvert.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setOuvert(checked)
        }
        binding.nullModifierHand.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setHand(checked)
        }
        binding.nullModifierOuvert.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setOuvert(checked)
        }
    }

    private fun setupObservers()
    {
        viewModel.participants.observe(viewLifecycleOwner) { bindSeatLabels(it) }

        viewModel.roundNumber.observe(viewLifecycleOwner) { number ->
            binding.sheetTitle.text = getString(R.string.title_round, number)
        }

        viewModel.draft.observe(viewLifecycleOwner) { draft ->
            bindDraft(draft)
        }

        viewModel.dismissEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                setFragmentResult(REQUEST_KEY, bundleOf())
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun bindSeatLabels(participants: SkatParticipants)
    {
        val seats = participants.asList()
        declarerButtons().forEachIndexed { index, button ->
            val seat = seats.getOrNull(index)
            button.visibility = if (seat == null) View.GONE else View.VISIBLE
            button.text = seat?.displayName.orEmpty()
            button.setOnClickListener { seat?.let { viewModel.setDeclarer(it) } }
        }
    }

    private fun declarerButtons(): List<MaterialButton> =
        listOf(binding.declarer1, binding.declarer2, binding.declarer3)

    private fun bindDraft(draft: RoundDraft)
    {
        isBinding = true

        bindSelectedDeclarer(draft)
        binding.gameDetails.visibility = if (draft.isPasse) View.GONE else View.VISIBLE

        checkOnly(
            when (draft.game)
            {
                RoundGame.DIAMONDS -> binding.gameDiamonds
                RoundGame.HEARTS -> binding.gameHearts
                RoundGame.SPADES -> binding.gameSpades
                RoundGame.CLUBS -> binding.gameClubs
                RoundGame.GRAND -> binding.gameGrand
                RoundGame.NULL -> binding.gameNull
            },
            binding.gameDiamonds, binding.gameHearts, binding.gameSpades,
            binding.gameClubs, binding.gameGrand, binding.gameNull
        )

        // Spitzen and win levels only matter outside a Null game.
        binding.multiplierBlock.visibility =
            if (draft.usesMultipliers) View.VISIBLE else View.GONE
        binding.nullModifierBlock.visibility =
            if (draft.usesMultipliers) View.GONE else View.VISIBLE

        binding.spitzenSlider.value = draft.spitzen.value.toFloat()
        binding.spitzenValue.text = getString(R.string.format_spitzen, draft.spitzen.value)

        binding.modifierHand.isChecked = draft.hand
        binding.modifierSchneider.isChecked = draft.schneider
        binding.modifierSchwarz.isChecked = draft.schwarz
        binding.modifierAnnounced.isChecked = draft.announced
        binding.modifierOuvert.isChecked = draft.ouvert
        binding.nullModifierHand.isChecked = draft.hand
        binding.nullModifierOuvert.isChecked = draft.ouvert

        // Announcing only means something once Schneider or Schwarz is set.
        binding.modifierAnnounced.isEnabled = draft.schneider || draft.schwarz

        checkOnly(
            when (draft.result)
            {
                RoundResult.WON -> binding.resultWon
                RoundResult.LOST -> binding.resultLost
                RoundResult.OVERBID -> binding.resultOverbid
            },
            binding.resultWon, binding.resultLost, binding.resultOverbid
        )
        binding.resultOverbid.isEnabled = draft.canBeOverbid

        val isOverbid = draft.result == RoundResult.OVERBID
        binding.bidBlock.visibility = if (isOverbid) View.VISIBLE else View.GONE
        binding.bidPicker.value = biddingValues
            .indexOf(draft.bid.value)
            .coerceAtLeast(0)

        bindPointsPreview(draft)

        isBinding = false
    }

    private fun bindSelectedDeclarer(draft: RoundDraft)
    {
        val seats = viewModel.participants.value?.asList() ?: emptyList()
        val selectedIndex = draft.declarer?.let { declarer ->
            seats.indexOfFirst { it.id == declarer.id }.takeIf { it >= 0 }
        }

        val allButtons = declarerButtons() + binding.declarerPasse
        val selected = selectedIndex?.let { declarerButtons().getOrNull(it) }
            ?: binding.declarerPasse

        checkOnly(selected, *allButtons.toTypedArray())
    }

    /**
     * MaterialButtonToggleGroup only enforces single selection for user taps, so restoring a
     * draft has to clear the others explicitly.
     */
    private fun checkOnly(selected: MaterialButton, vararg all: MaterialButton)
    {
        all.forEach { it.isChecked = it == selected }
    }

    private fun bindPointsPreview(draft: RoundDraft)
    {
        val points = draft.points

        binding.pointsForLabel.text = draft.declarer
            ?.let { getString(R.string.format_points_for, it.displayName) }
            ?: getString(R.string.label_passe)

        binding.pointsPreview.text = when
        {
            draft.isPasse -> "–"
            points > 0 -> "+$points"
            else -> points.toString()
        }
        binding.pointsPreview.setTextColor(
            MaterialColors.getColor(
                binding.pointsPreview,
                if (points < 0) R.attr.colorTertiary else R.attr.colorPrimary
            )
        )
    }

    companion object
    {
        const val TAG = "RoundEntrySheetFragment"
        const val REQUEST_KEY = "RoundEntryRequest"

        fun newInstance(gameId: UUID, scoreId: UUID? = null) = RoundEntrySheetFragment().apply {
            arguments = bundleOf(
                RoundEntryViewModel.ARG_GAME_ID to gameId.toString(),
                RoundEntryViewModel.ARG_SCORE_ID to scoreId?.toString(),
            )
        }
    }
}
