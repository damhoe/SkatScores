package com.damhoe.skatscores.game.skat.adapter.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetPlayerSeatsBinding
import com.damhoe.skatscores.game.common.SeatError
import com.damhoe.skatscores.shared.setPlayerSuggestions
import com.damhoe.skatscores.shared.setSeatName
import com.damhoe.skatscores.shared.setupAsPlayerField
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Who sits at the three seats of a running list, reached from the game screen's app bar.
 *
 * The seats were a section of the settings sheet for a while, which buried the edit that
 * happens most often - somebody arrives, somebody swaps out - under the ones that happen once.
 */
@AndroidEntryPoint
class PlayerSeatsSheetFragment : BottomSheetDialogFragment()
{
    private val viewModel: PlayerSeatsViewModel by viewModels()
    private lateinit var binding: SheetPlayerSeatsBinding

    /** Set while pushing state into the controls, so their listeners do not echo back. */
    private var isBinding = false

    /** Seats the user has actually edited; errors only surface for those. */
    private val touchedSeats = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetPlayerSeatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupPlayerSuggestions()
        setupObservers()
    }

    /** Three fields plus their dropdowns want the whole sheet, not a peek. */
    override fun onStart()
    {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun setupListeners()
    {
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { viewModel.save() }

        seatFields().forEachIndexed { seat, field ->
            field.setupAsPlayerField()
            field.doAfterTextChanged {
                if (isBinding) return@doAfterTextChanged
                touchedSeats += seat
                viewModel.setSeatName(seat, it?.toString().orEmpty())
            }
        }
    }

    /** Registered players are offered as suggestions; any other text becomes a guest. */
    private fun setupPlayerSuggestions()
    {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allRegisteredPlayers.collect { players ->
                val names = players.map { it.name.value }
                seatFields().forEach { it.setPlayerSuggestions(names) }
            }
        }
    }

    private fun setupObservers()
    {
        // Only the initial load writes into the text fields; later edits come from the user.
        viewModel.loaded.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { names ->
                isBinding = true
                seatFields().forEachIndexed { seat, field ->
                    field.setSeatName(names.getOrNull(seat).orEmpty())
                }
                isBinding = false
            }
        }

        viewModel.seatErrors.observe(viewLifecycleOwner) { problems ->
            seatInputs().forEachIndexed { seat, input ->
                input.error = when
                {
                    seat !in touchedSeats -> null
                    problems[seat] == SeatError.NAME_REQUIRED ->
                        getString(R.string.error_name_required)

                    problems[seat] == SeatError.NAME_DUPLICATE ->
                        getString(R.string.error_duplicate_players)

                    else -> null
                }
            }
        }

        viewModel.canSave.observe(viewLifecycleOwner) { binding.saveButton.isEnabled = it }

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

    private fun seatFields(): List<MaterialAutoCompleteTextView> =
        listOf(binding.player1EditText, binding.player2EditText, binding.player3EditText)

    private fun seatInputs(): List<TextInputLayout> =
        listOf(binding.player1Input, binding.player2Input, binding.player3Input)

    companion object
    {
        const val TAG = "PlayerSeatsSheetFragment"
        const val REQUEST_KEY = "PlayerSeatsRequest"

        fun newInstance(gameId: UUID) = PlayerSeatsSheetFragment().apply {
            arguments = bundleOf(PlayerSeatsViewModel.ARG_GAME_ID to gameId.toString())
        }
    }
}
