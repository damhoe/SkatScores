package com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetDoppelkopfRoundEntryBinding
import com.damhoe.skatscores.databinding.ViewExtraPointsStepperBinding
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundDraft
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundKind
import com.damhoe.skatscores.shared.fillWithChipRows
import com.damhoe.skatscores.shared.signed
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

/**
 * Enters a new Doppelkopf round or edits an existing one on a single surface, with a live
 * points preview.
 */
@AndroidEntryPoint
class DoppelkopfRoundEntrySheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: DoppelkopfRoundEntryViewModel by viewModels()
    private lateinit var binding: SheetDoppelkopfRoundEntryBinding

    private var soloKindChips: Map<DoppelkopfSoloKind, MaterialButton> = emptyMap()
    private var winLevelChips: Map<DoppelkopfWinLevel, MaterialButton> = emptyMap()

    /** Absage chips, plus a "none" entry for a round played without one. */
    private var absageChips: Map<DoppelkopfWinLevel?, MaterialButton> = emptyMap()

    /** Set while pushing state into the controls, so their listeners do not echo back. */
    private var isBinding = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetDoppelkopfRoundEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        buildChipRows()
        setupListeners()
        setupObservers()
    }

    /**
     * A round has more to say than fits in the collapsed peek height, and a sheet that opens
     * half-drawn hides the save button behind a drag.
     */
    override fun onStart()
    {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun buildChipRows()
    {
        soloKindChips = binding.soloKindRows.fillWithChipRows(
            values = viewModel.soloKindOptions,
            label = { getString(DoppelkopfRoundTextFactory.soloKindLabel(it)) },
            onClick = { viewModel.setSoloKind(it) },
        )

        winLevelChips = binding.winLevelRows.fillWithChipRows(
            values = viewModel.winLevelOptions,
            label = { getString(DoppelkopfRoundTextFactory.winLevelLabel(it)) },
            onClick = { viewModel.setWinLevel(it) },
        )

        // Null leads the row: most rounds are played without an Absage, so "none" is the
        // resting state rather than something the user has to clear.
        val absageValues: List<DoppelkopfWinLevel?> = listOf(null) + viewModel.absageOptions
        absageChips = binding.absageRows.fillWithChipRows(
            values = absageValues,
            label = { level ->
                level
                    ?.let { getString(DoppelkopfRoundTextFactory.winLevelLabel(it)) }
                    ?: getString(R.string.label_absage_none)
            },
            onClick = { viewModel.setAbsage(it) },
        )
    }

    private fun setupListeners()
    {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { viewModel.save() }

        binding.kindNormal.setOnClickListener {
            viewModel.setKind(DoppelkopfRoundKind.NORMAL)
        }
        binding.kindSolo.setOnClickListener { viewModel.setKind(DoppelkopfRoundKind.SOLO) }

        seatButtons().forEachIndexed { seat, button ->
            button.setOnClickListener { viewModel.toggleSeat(seat) }
        }

        binding.winnerRe.setOnClickListener { viewModel.setWinner(DoppelkopfParty.RE) }
        binding.winnerKontra.setOnClickListener { viewModel.setWinner(DoppelkopfParty.KONTRA) }

        binding.announceRe.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setReAnnounced(checked)
        }
        binding.announceKontra.addOnCheckedChangeListener { _, checked ->
            if (!isBinding) viewModel.setKontraAnnounced(checked)
        }

        setupStepper(binding.extraRe, DoppelkopfParty.RE)
        setupStepper(binding.extraKontra, DoppelkopfParty.KONTRA)
    }

    private fun setupStepper(stepper: ViewExtraPointsStepperBinding, party: DoppelkopfParty)
    {
        stepper.extraLabel.setText(
            if (party == DoppelkopfParty.RE) R.string.label_re else R.string.label_kontra
        )
        stepper.extraAdd.setOnClickListener { viewModel.changeExtraPoints(party, +1) }
        stepper.extraRemove.setOnClickListener { viewModel.changeExtraPoints(party, -1) }
    }

    private fun setupObservers()
    {
        viewModel.participants.observe(viewLifecycleOwner) { bindSeatLabels(it) }

        viewModel.roundNumber.observe(viewLifecycleOwner) { number ->
            binding.sheetTitle.text = getString(R.string.title_round, number)
        }

        viewModel.countsExtraPoints.observe(viewLifecycleOwner) { counts ->
            binding.extraPointsBlock.visibility = if (counts) View.VISIBLE else View.GONE
            // The preview has to follow the setting, not just the section's visibility.
            viewModel.draft.value?.let { bindPointsPreview(it, counts) }
        }

        viewModel.draft.observe(viewLifecycleOwner) { bindDraft(it) }

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

    private fun bindSeatLabels(participants: DoppelkopfParticipants)
    {
        val seats = participants.asList()
        seatButtons().forEachIndexed { index, button ->
            val seat = seats.getOrNull(index)
            button.visibility = if (seat == null) View.INVISIBLE else View.VISIBLE
            button.text = seat?.displayName.orEmpty()
        }
    }

    private fun seatButtons(): List<MaterialButton> =
        listOf(binding.seat1, binding.seat2, binding.seat3, binding.seat4)

    private fun bindDraft(draft: DoppelkopfRoundDraft)
    {
        isBinding = true

        checkOnly(
            if (draft.isSolo) binding.kindSolo else binding.kindNormal,
            binding.kindNormal, binding.kindSolo,
        )

        bindSeatSelection(draft)

        binding.soloKindBlock.visibility = if (draft.isSolo) View.VISIBLE else View.GONE
        soloKindChips.forEach { (kind, chip) -> chip.isChecked = kind == draft.soloKind }

        checkOnly(
            if (draft.winner == DoppelkopfParty.RE) binding.winnerRe else binding.winnerKontra,
            binding.winnerRe, binding.winnerKontra,
        )

        winLevelChips.forEach { (level, chip) -> chip.isChecked = level == draft.winLevel }

        absageChips.forEach { (level, chip) -> chip.isChecked = level == draft.absage }

        binding.announceRe.isChecked = draft.reAnnounced
        binding.announceKontra.isChecked = draft.kontraAnnounced

        binding.extraRe.extraValue.text = draft.extraPointsRe.toString()
        binding.extraKontra.extraValue.text = draft.extraPointsKontra.toString()
        binding.extraRe.extraRemove.isEnabled = draft.extraPointsRe > 0
        binding.extraKontra.extraRemove.isEnabled = draft.extraPointsKontra > 0

        binding.saveButton.isEnabled = draft.isComplete

        bindPointsPreview(draft, viewModel.countsExtraPoints.value == true)

        isBinding = false
    }

    /**
     * Seats are a multi-select in a normal round and a single choice in a solo, so the label
     * and the hint change with the round kind rather than leaving the user to guess.
     */
    private fun bindSeatSelection(draft: DoppelkopfRoundDraft)
    {
        val seats = viewModel.participants.value?.asList().orEmpty()

        binding.seatsLabel.setText(
            if (draft.isSolo) R.string.title_who_played_solo else R.string.title_who_was_re
        )

        seatButtons().forEachIndexed { index, button ->
            val seat = seats.getOrNull(index) ?: return@forEachIndexed
            button.isChecked =
                if (draft.isSolo) draft.soloist?.id == seat.id else seat.id in draft.reSeats
        }

        binding.seatsHint.visibility = if (draft.isComplete) View.GONE else View.VISIBLE
        binding.seatsHint.setText(
            if (draft.isSolo) R.string.text_pick_soloist_first else R.string.text_pick_re_first
        )
    }

    /**
     * MaterialButtonToggleGroup only enforces single selection for user taps, so restoring a
     * draft has to clear the others explicitly.
     */
    private fun checkOnly(selected: MaterialButton, vararg all: MaterialButton)
    {
        all.forEach { it.isChecked = it == selected }
    }

    private fun bindPointsPreview(draft: DoppelkopfRoundDraft, countsExtraPoints: Boolean)
    {
        val points = draft.points(countsExtraPoints)
        val winner = getString(
            if (draft.winner == DoppelkopfParty.RE) R.string.label_re else R.string.label_kontra
        )

        binding.pointsForLabel.text = getString(R.string.format_points_for_party, winner)
        binding.pointsPreview.text = signed(points)
        binding.pointsPreview.setTextColor(
            MaterialColors.getColor(
                binding.pointsPreview,
                if (points < 0) R.attr.colorTertiary else R.attr.colorPrimary
            )
        )
    }

    companion object
    {
        const val TAG = "DoppelkopfRoundEntrySheet"
        const val REQUEST_KEY = "DoppelkopfRoundEntryRequest"

        fun newInstance(gameId: UUID, scoreId: UUID? = null) =
            DoppelkopfRoundEntrySheetFragment().apply {
                arguments = bundleOf(
                    DoppelkopfRoundEntryViewModel.ARG_GAME_ID to gameId.toString(),
                    DoppelkopfRoundEntryViewModel.ARG_SCORE_ID to scoreId?.toString(),
                )
            }
    }
}
