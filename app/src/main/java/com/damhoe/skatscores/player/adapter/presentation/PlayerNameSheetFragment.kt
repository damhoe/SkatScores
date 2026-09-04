package com.damhoe.skatscores.player.adapter.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetPlayerNameBinding
import com.damhoe.skatscores.player.domain.PlayerName
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Asks for a player name, for a new player or a rename.
 *
 * The sheet only validates and reports back; the caller owns the player, so it decides what
 * the name is for. Names are unique, which is why the taken-name check is passed in rather
 * than done here.
 */
class PlayerNameSheetFragment : BottomSheetDialogFragment()
{
    private lateinit var binding: SheetPlayerNameBinding

    /** Names already in use, excluding the one being edited. */
    private val takenNames: List<String>
        get() = arguments?.getStringArrayList(ARG_TAKEN_NAMES).orEmpty()

    private val initialName: String
        get() = arguments?.getString(ARG_INITIAL_NAME).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetPlayerNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.sheetTitle.setText(
            arguments?.getInt(ARG_TITLE_RES)?.takeIf { it != 0 }
                ?: R.string.dialog_title_create_player
        )

        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { save() }

        binding.nameEditText.setText(initialName)
        binding.nameEditText.setSelection(initialName.length)
        binding.nameEditText.doAfterTextChanged { validate(it?.toString().orEmpty()) }

        // Nothing typed yet is not an error, but it is not saveable either.
        binding.saveButton.isEnabled = false
    }

    private fun validate(raw: String)
    {
        val name = raw.trim()

        val error = when
        {
            name.isEmpty() -> null
            PlayerName.create(name).isFailure -> getString(R.string.error_name_has_wrong_size)
            takenNames.any { it.equals(name, ignoreCase = true) } ->
                getString(R.string.error_name_exists_already)

            else -> null
        }

        binding.nameInput.error = error
        binding.saveButton.isEnabled =
            error == null && name.isNotEmpty() && !name.equals(initialName, ignoreCase = true)
    }

    private fun save()
    {
        val name = binding.nameEditText.text?.toString()?.trim().orEmpty()
        if (PlayerName.create(name).isFailure) return

        setFragmentResult(REQUEST_KEY, bundleOf(RESULT_NAME to name))
        dismiss()
    }

    companion object
    {
        const val TAG = "PlayerNameSheetFragment"
        const val REQUEST_KEY = "PlayerNameRequest"
        const val RESULT_NAME = "name"

        private const val ARG_TITLE_RES = "titleRes"
        private const val ARG_INITIAL_NAME = "initialName"
        private const val ARG_TAKEN_NAMES = "takenNames"

        fun newInstance(
            titleRes: Int,
            initialName: String = "",
            takenNames: List<String> = emptyList(),
        ) = PlayerNameSheetFragment().apply {
            arguments = bundleOf(
                ARG_TITLE_RES to titleRes,
                ARG_INITIAL_NAME to initialName,
                ARG_TAKEN_NAMES to ArrayList(takenNames),
            )
        }
    }
}
