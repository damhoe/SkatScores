package com.damhoe.skatscores.game.skat.adapter.presentation

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.DialogEditParticipantsBinding
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditParticipantsDialogFragment :
    DialogFragment(R.layout.dialog_edit_participants)
{
    private lateinit var binding: DialogEditParticipantsBinding
    private var allRegisteredPlayers: List<SkatParticipant.Registered> = emptyList()
    private var forehand: SkatParticipant? = null
    private var middlehand: SkatParticipant? = null
    private var rearhand: SkatParticipant? = null

    private val guest by lazy {
        SkatParticipant.Guest(
            name = PlayerName.create("Guest")
                .getOrThrow() // Ensure PlayerName can be created like this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        arguments?.let {
            // Load data from arguments, NOT from a ViewModel instance
            val playersList: ArrayList<Player> =
                it.getParcelableArrayList(ARG_ALL_PLAYERS) ?: ArrayList()
            allRegisteredPlayers =
                playersList.map { player -> SkatParticipant.Registered.from(player) }
            forehand = BundleCompat.getParcelable(it, ARG_FOREHAND, SkatParticipant::class.java)!!
            middlehand =
                BundleCompat.getParcelable(it, ARG_MIDDLEHAND, SkatParticipant::class.java)!!
            rearhand = BundleCompat.getParcelable(it, ARG_REARHAND, SkatParticipant::class.java)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = DialogEditParticipantsBinding.inflate(layoutInflater)

        val adapterSourceList = listOf(guest) + allRegisteredPlayers
        val playerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            adapterSourceList // This works if SkatParticipant subtypes override toString()
        )

        setupAutoCompleteTextView(
            binding.player1EditText,
            playerAdapter,
            0,
            forehand
        )
        setupAutoCompleteTextView(
            binding.player2EditText,
            playerAdapter,
            1,
            middlehand
        )
        setupAutoCompleteTextView(
            binding.player3EditText,
            playerAdapter,
            2,
            rearhand
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.dialog_title_button_save) { _, _ ->

                forehand = SkatParticipant.Guest(
                    PlayerName.create(binding.player1EditText.text.toString()).getOrThrow()
                )
                middlehand = SkatParticipant.Guest(
                    PlayerName.create(binding.player2EditText.text.toString()).getOrThrow()
                )
                rearhand = SkatParticipant.Guest(
                    PlayerName.create(binding.player3EditText.text.toString()).getOrThrow()
                )

                setFragmentResult(
                    REQUEST_KEY, bundleOf(
                        RESULT_FOREHAND to forehand,
                        RESULT_MIDDLEHAND to middlehand,
                        RESULT_REARHAND to rearhand
                    )
                )
                dismiss()
            }
            .setNegativeButton(R.string.dialog_title_button_cancel) { _, _ -> dismiss() }
            .create()

        dialog.setOnShowListener {
            updateSaveButtonStateAndErrors(dialog)
        }
        return dialog
    }

    private fun setupAutoCompleteTextView(
        autocompleteTextView: AutoCompleteTextView,
        adapter: ArrayAdapter<SkatParticipant>,
        positionIndex: Int, // 0 for FH, 1 for MH, 2 for RH
        initialParticipant: SkatParticipant?
    )
    {
        autocompleteTextView.setAdapter(adapter)

        val participant = initialParticipant
        when (positionIndex)
        {
            0 -> forehand = participant
            1 -> middlehand = participant
            2 -> rearhand = participant
        }
        autocompleteTextView.setText(participant?.displayName)

        autocompleteTextView.setOnItemClickListener { _, _, positionInAdapter, _ ->
            val selected = adapter.getItem(positionInAdapter)!!
            when (positionIndex)
            {
                0 -> forehand = selected
                1 -> middlehand = selected
                2 -> rearhand = selected
            }
            (dialog as? AlertDialog)?.let { updateSaveButtonStateAndErrors(it) }
        }

        autocompleteTextView.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable?)
            {
                val currentText = s?.toString().orEmpty()
                val createNameResult = PlayerName.create(currentText)

                val currentInputLayout = when (positionIndex)
                {
                    0 -> binding.player1Input
                    1 -> binding.player2Input
                    else -> binding.player3Input
                }
                currentInputLayout.error = null

                createNameResult
                    .onSuccess { validatedName ->
                        val participant = SkatParticipant.Guest(validatedName)
                        updateParticipant(positionIndex, participant)
                    }
                    .onFailure {
                        updateParticipant(positionIndex, null)
                        currentInputLayout.error = "Enter 1 - 20 chars"
                    }

                (dialog as? AlertDialog)?.let { updateSaveButtonStateAndErrors(it) }
            }
        })
    }

    private fun updateParticipant(
        index: Int,
        participant: SkatParticipant?
    )
    {
        when (index)
        {
            0 -> forehand = participant
            1 -> middlehand = participant
            else -> rearhand = participant
        }
    }

    private fun updateSaveButtonStateAndErrors(dialog: AlertDialog)
    {
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val registeredSelections = listOfNotNull(forehand, middlehand, rearhand)

        val areValidParticipants = 3 == registeredSelections.map { it.displayName }.toSet().size

        saveButton.isEnabled = areValidParticipants

        if (!areValidParticipants)
        {
            Log.w("EditParticipantsDialog", "Duplicate registered players selected.")
            // Optionally show an error on the input fields
        }
    }

    companion object
    {
        const val TAG = "EditParticipantsDialogFragment"
        const val REQUEST_KEY = "EditParticipantsDialogRequest"
        const val RESULT_FOREHAND = "result_forehand"
        const val RESULT_MIDDLEHAND = "result_middlehand"
        const val RESULT_REARHAND = "result_rearhand"

        private const val ARG_ALL_PLAYERS = "arg_all_players"
        private const val ARG_FOREHAND = "arg_current_forehand"
        private const val ARG_MIDDLEHAND = "arg_current_middlehand"
        private const val ARG_REARHAND = "arg_current_rearhand"

        fun newInstance(
            allPlayers: List<Player>,
            forehand: SkatParticipant,
            middlehand: SkatParticipant,
            rearhand: SkatParticipant
        ): EditParticipantsDialogFragment
        {
            val args = Bundle().apply {
                putParcelableArrayList(
                    ARG_ALL_PLAYERS,
                    ArrayList(allPlayers)
                )
                putParcelable(
                    ARG_FOREHAND,
                    forehand
                )
                putParcelable(
                    ARG_MIDDLEHAND,
                    middlehand
                )
                putParcelable(
                    ARG_REARHAND,
                    rearhand
                )
            }
            return EditParticipantsDialogFragment().apply {
                arguments = args
            }
        }
    }
}