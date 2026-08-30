package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetNewDoppelkopfListBinding
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfRoundCount
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfScoringMode
import com.damhoe.skatscores.shared.fillWithChipRows
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Creates a Doppelkopf list from the home FAB: name, round count and scoring mode. Players are
 * chosen in the game view, so the sheet does not ask for them.
 */
@AndroidEntryPoint
class NewDoppelkopfListSheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: NewDoppelkopfListViewModel by viewModels()
    private lateinit var binding: SheetNewDoppelkopfListBinding

    private var roundCountChips: Map<DoppelkopfRoundCount, MaterialButton> = emptyMap()

    /** Set while pushing state into the controls, so their listeners do not echo back. */
    private var isBinding = false

    /** Errors only surface once the name has been touched, not on a freshly opened sheet. */
    private var titleTouched = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetNewDoppelkopfListBinding.inflate(inflater, container, false)
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

    /** One chip per value the domain accepts, in rows of three. */
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
            viewModel.setScoringMode(DoppelkopfScoringMode.SIMPLE)
        }
        binding.scoringDetailed.setOnClickListener {
            viewModel.setScoringMode(DoppelkopfScoringMode.DETAILED)
        }
    }

    private fun setupObservers()
    {
        viewModel.draft.observe(viewLifecycleOwner) { draft ->
            isBinding = true

            roundCountChips.forEach { (roundCount, chip) ->
                chip.isChecked = roundCount == draft.roundCount
            }

            val isSimple = draft.scoringMode == DoppelkopfScoringMode.SIMPLE
            binding.scoringSimple.isChecked = isSimple
            binding.scoringDetailed.isChecked = !isSimple
            binding.scoringHint.setText(
                if (isSimple) R.string.text_dk_scoring_simple_hint
                else R.string.text_dk_scoring_detailed_hint
            )

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
        val title = arguments?.getString(ARG_TITLE)
            ?: getString(R.string.default_doppelkopf_list_title)
        val rounds = arguments?.getInt(ARG_ROUND_COUNT, -1)?.takeIf { it > 0 }
            ?.let { runCatching { DoppelkopfRoundCount(it) }.getOrNull() }

        isBinding = true
        binding.listNameEditText.setText(title)
        isBinding = false

        viewModel.prefill(title, rounds)
    }

    companion object
    {
        const val TAG = "NewDoppelkopfListSheet"
        const val REQUEST_KEY = "NewDoppelkopfListRequest"
        const val RESULT_GAME_ID = "gameId"

        private const val ARG_TITLE = "title"
        private const val ARG_ROUND_COUNT = "roundCount"

        /**
         * @param suggestedTitle name to start from, usually derived from the last list
         * @param suggestedRoundCount round count to start from, usually the last list's
         */
        fun newInstance(
            suggestedTitle: String? = null,
            suggestedRoundCount: Int? = null,
        ) = NewDoppelkopfListSheetFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to suggestedTitle,
                ARG_ROUND_COUNT to (suggestedRoundCount ?: -1),
            )
        }
    }
}
