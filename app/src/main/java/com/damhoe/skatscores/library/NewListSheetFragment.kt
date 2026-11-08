package com.damhoe.skatscores.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetNewListBinding
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.shared.fillWithChipRows
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewListSheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: NewListViewModel by viewModels()
    private lateinit var binding: SheetNewListBinding

    private var roundCountChips: Map<SkatRoundCount, MaterialButton> = emptyMap()

    /** Suppresses control listeners while state is pushed into the controls. */
    private var isBinding = false

    private var titleTouched = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetNewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        buildRoundCountChips()
        setupListeners()
        setupObservers()
        prefillFromArguments()
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
        binding.startButton.setOnClickListener { viewModel.start() }

        binding.listNameEditText.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            titleTouched = true
            viewModel.setTitle(it?.toString().orEmpty())
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
        viewModel.draft.observe(viewLifecycleOwner) { draft ->
            isBinding = true

            roundCountChips.forEach { (roundCount, chip) ->
                chip.isChecked = roundCount == draft.roundCount
            }
            binding.scoringSimple.isChecked = draft.scoringMode == SkatScoringMode.CLASSIC
            binding.scoringTournament.isChecked = draft.scoringMode == SkatScoringMode.TOURNAMENT

            isBinding = false
        }

        viewModel.titleError.observe(viewLifecycleOwner) { hasError ->
            binding.listNameInput.error =
                if (hasError && titleTouched) getString(R.string.error_valid_title_required)
                else null
        }

        viewModel.canStart.observe(viewLifecycleOwner) { binding.startButton.isEnabled = it }

        viewModel.navigateToGame.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { gameId ->
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_GAME_ID to gameId.toString()))
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun prefillFromArguments()
    {
        val title = arguments?.getString(ARG_TITLE) ?: getString(R.string.default_list_title)
        val rounds = arguments?.getInt(ARG_ROUND_COUNT, -1)?.takeIf { it > 0 }
            ?.let { runCatching { SkatRoundCount(it) }.getOrNull() }

        isBinding = true
        binding.listNameEditText.setText(title)
        isBinding = false

        viewModel.prefill(title, rounds)
    }

    companion object
    {
        const val TAG = "NewListSheetFragment"
        const val REQUEST_KEY = "NewListRequest"
        const val RESULT_GAME_ID = "gameId"

        private const val ARG_TITLE = "title"
        private const val ARG_ROUND_COUNT = "roundCount"

        fun newInstance(
            suggestedTitle: String? = null,
            suggestedRoundCount: Int? = null,
        ) = NewListSheetFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to suggestedTitle,
                ARG_ROUND_COUNT to (suggestedRoundCount ?: -1),
            )
        }
    }
}
