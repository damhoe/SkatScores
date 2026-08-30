package com.damhoe.skatscores.game.skat.adapter.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetGameSettingsBinding
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.shared.confirmListDeletion
import com.damhoe.skatscores.shared.fillWithChipRows
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

/**
 * Everything about a running list that is not a round and not who is playing it: its name,
 * its round count and its scoring mode. The seats are next door in PlayerSeatsSheetFragment,
 * which the game screen opens from a button of its own.
 */
@AndroidEntryPoint
class GameSettingsSheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: GameSettingsViewModel by viewModels()
    private lateinit var binding: SheetGameSettingsBinding

    private var roundCountChips: Map<SkatRoundCount, MaterialButton> = emptyMap()

    /** Set while pushing state into the controls, so their listeners do not echo back. */
    private var isBinding = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetGameSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        buildRoundCountChips()
        setupListeners()
        setupObservers()
    }

    /**
     * Its sections are taller than the collapsed peek height, and a sheet that opens
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

    private fun buildRoundCountChips()
    {
        roundCountChips = binding.roundCountRows.fillWithChipRows(
            values = viewModel.roundCountOptions,
            label = { it.value.toString() },
            onClick = { viewModel.setRoundCount(it) },
        )
    }

    private fun setupListeners()
    {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { viewModel.save() }
        binding.deleteButton.setOnClickListener {
            val draft = viewModel.draft.value ?: return@setOnClickListener
            requireContext().confirmListDeletion(
                listName = draft.title,
                roundsPlayed = draft.roundsPlayed,
            ) { viewModel.deleteGame() }
        }

        binding.listNameEditText.doAfterTextChanged {
            if (!isBinding) viewModel.setTitle(it?.toString().orEmpty())
        }

        binding.scoringSimple.setOnClickListener {
            viewModel.setScoringMode(SkatScoringMode.CLASSIC)
        }
        binding.scoringTournament.setOnClickListener {
            viewModel.setScoringMode(SkatScoringMode.TOURNAMENT)
        }
    }

    private fun setupObservers()
    {
        // Only the initial load writes into the text fields; later edits come from the user.
        viewModel.loaded.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { draft ->
                isBinding = true
                binding.listNameEditText.setText(draft.title)
                isBinding = false
            }
        }

        viewModel.draft.observe(viewLifecycleOwner) { draft ->
            roundCountChips.forEach { (roundCount, chip) ->
                chip.isChecked = roundCount == draft.roundCount
                chip.isEnabled = viewModel.isSelectable(roundCount)
            }

            val hasBlockedOptions = roundCountChips.keys.any { !viewModel.isSelectable(it) }
            binding.roundCountHint.visibility =
                if (hasBlockedOptions) View.VISIBLE else View.GONE
            binding.roundCountHint.text =
                resources.getQuantityString(
                    R.plurals.label_rounds_already_played,
                    draft.roundsPlayed,
                    draft.roundsPlayed
                )

            binding.scoringSimple.isChecked = draft.scoringMode == SkatScoringMode.CLASSIC
            binding.scoringTournament.isChecked = draft.scoringMode == SkatScoringMode.TOURNAMENT
        }

        viewModel.titleError.observe(viewLifecycleOwner) { hasError ->
            binding.listNameInput.error =
                if (hasError) getString(R.string.error_valid_title_required) else null
        }

        viewModel.canSave.observe(viewLifecycleOwner) { binding.saveButton.isEnabled = it }

        viewModel.dismissEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                setFragmentResult(REQUEST_KEY, bundleOf())
                dismiss()
            }
        }

        // The list is gone, so the screen behind this sheet has nothing to show any more.
        viewModel.deletedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_DELETED to true))
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object
    {
        const val TAG = "GameSettingsSheetFragment"
        const val REQUEST_KEY = "GameSettingsRequest"

        /** Set on the result when the list was deleted rather than saved. */
        const val RESULT_DELETED = "deleted"

        fun newInstance(gameId: UUID) = GameSettingsSheetFragment().apply {
            arguments = bundleOf(GameSettingsViewModel.ARG_GAME_ID to gameId.toString())
        }
    }
}
