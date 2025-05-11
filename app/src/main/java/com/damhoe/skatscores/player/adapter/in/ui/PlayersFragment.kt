package com.damhoe.skatscores.player.adapter.`in`.ui

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.app.HomeFragmentDirections
import com.damhoe.skatscores.databinding.FragmentPlayersBinding
import com.damhoe.skatscores.player.domain.Player
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayersFragment :
    Fragment(),
    NotifyItemClickListener
{
    private lateinit var binding: FragmentPlayersBinding

    @Inject
    lateinit var playerVMFactory: PlayerViewModelFactory
    private val viewModel: PlayerViewModel by viewModels(
        { requireActivity() }) { playerVMFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_players,
            container,
            false
        )

        binding.playerRecyclerView.apply {
            adapter = PlayerAdapter(
                this@PlayersFragment
            )
            layoutManager = LinearLayoutManager(
                context
            )
            addItemDecoration(
                PlayerAdapter.ItemDecoration()
            )
        }

        return binding.root
    }

    fun findNavController() = findNavController(
        requireActivity(),
        R.id.nav_host_fragment
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        binding.addPlayerButton.setOnClickListener { showAddPlayerDialog() }

        viewModel.players.observe(viewLifecycleOwner) {
            with(binding.playerRecyclerView) {
                (adapter as PlayerAdapter)
                    .setPlayers(
                        it
                    )
                invalidate()
            }
        }
    }

    private fun showAddPlayerDialog()
    {
        val layout = layoutInflater.inflate(
            R.layout.dialog_edit_name,
            null
        )
        val editText: TextInputEditText = layout.findViewById(
            R.id.edit_name
        )
        val inputLayout: TextInputLayout = layout.findViewById(
            R.id.input_name
        )

        val dialog: AlertDialog =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    getString(R.string.dialog_title_create_player)
                )
                .setView(
                    layout
                )
                .setBackground(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.background_dialog_fragment,
                        requireActivity().theme
                    )
                )
                .setNegativeButton(
                    getString(R.string.dialog_title_button_cancel)
                ) { d: DialogInterface, _: Int -> d.cancel() }
                .setPositiveButton(
                    getString(R.string.dialog_title_button_create)
                ) { d: DialogInterface, _: Int ->
                    if (inputLayout.error != null)
                    {
                        return@setPositiveButton
                    }
                    val s = editText.getText()
                        ?: return@setPositiveButton
                    val name = s.toString().trim { it <= ' ' }
                    viewModel.addPlayer(
                        name
                    )
                    d.dismiss()
                }
                .create()

        editText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore
            }

            override fun afterTextChanged(
                s: Editable
            )
            {
                inputLayout.error = null

                if (s.toString()
                        .trim { it <= ' ' }
                        .isEmpty()
                )
                {
                    inputLayout.error = getString(R.string.error_name_required)
                }

                if (viewModel.isExistentName(
                        s.toString()
                            .trim { it <= ' ' })
                )
                {
                    inputLayout.error = getString(R.string.error_name_exists_already)
                }

                val maxLength: Int = inputLayout.counterMaxLength
                val currentLength: Int = s.length

                if (currentLength > maxLength)
                {
                    // Trim the text to the maximum length
                    s.replace(
                        maxLength,
                        currentLength,
                        ""
                    )
                }

                dialog.getButton(
                    DialogInterface.BUTTON_POSITIVE
                )
                    .isEnabled = inputLayout.error == null
            }
        })

        dialog.setOnShowListener {
            dialog.getButton(
                DialogInterface.BUTTON_POSITIVE
            )
                .isEnabled = false
        }
        dialog.show()
    }

    override fun notifyItemClick(
        player: Player,
        position: Int
    )
    {
        viewModel.selectPlayer(
            player
        )
        navigateToPlayerDetails()
    }

    private fun navigateToPlayerDetails() = findNavController().navigate(
        HomeFragmentDirections.actionHomeToPlayerDetails()
    )
}