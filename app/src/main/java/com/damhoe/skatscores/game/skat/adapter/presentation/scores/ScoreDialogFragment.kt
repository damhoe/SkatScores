package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.NavHostFragment
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.DialogScoreBinding
import com.damhoe.skatscores.game.skat.adapter.presentation.SharedSkatGameViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ScoreDialogFragment :
    DialogFragment(R.layout.dialog_score)
{
    private lateinit var binding: DialogScoreBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels()
    private val skatGameViewModel: SharedSkatGameViewModel by hiltNavGraphViewModels(R.id.skat_game_nav_graph)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = DialogScoreBinding.inflate(layoutInflater)

        val gameId = arguments?.getString("gameId")
        if (gameId == null)
        {
            dismiss()
            return super.onCreateDialog(savedInstanceState)
        }

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.score_dialog_nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        val startArgs = Bundle().apply {
            putString("gameId", gameId)
        }

        navController.setGraph(R.navigation.score_nav_graph, startArgs)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.title_next, null)
            .setNegativeButton(R.string.dialog_title_button_cancel) { _, _ ->
                dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.score_dialog_nav_host) as NavHostFragment
            val navController = navHostFragment.navController

            positiveButton.setOnClickListener {
                when (navController.currentDestination?.id)
                {
                    R.id.whoWonScoreFragment ->
                    {
                        navController.navigate(R.id.toResultScoreFragment)
                    }

                    R.id.biddingScoreFragment ->
                    {
                        dismiss()
                    }

                    R.id.spitzenScoreFragment ->
                    {
                        val action = SpitzenScoreFragmentDirections.toMultipliersScoreFragment()
                        navController.navigate(action)
                    }

                    R.id.multipliersScoreFragment ->
                    {
                        scoreViewModel.addGrandOrSuitScore()
                        dismiss()
                    }

                    R.id.nullMultipliersScoreFragment ->
                    {
                        scoreViewModel.addNullScore()
                        dismiss()
                    }
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id)
                {
                    R.id.whoWonScoreFragment ->
                    {
                        positiveButton.visibility = View.GONE
                    }

                    R.id.resultScoreFragment ->
                    {
                        positiveButton.visibility = View.GONE
                    }

                    R.id.suitScoreFragment ->
                    {
                        positiveButton.visibility = View.GONE
                    }

                    R.id.biddingScoreFragment ->
                    {
                        positiveButton.visibility = View.VISIBLE
                        positiveButton.setText(R.string.dialog_title_button_save)
                    }

                    R.id.spitzenScoreFragment ->
                    {
                        positiveButton.visibility = View.VISIBLE
                        positiveButton.setText(R.string.title_next)
                    }

                    R.id.multipliersScoreFragment ->
                    {
                        positiveButton.visibility = View.VISIBLE
                        positiveButton.setText(R.string.dialog_title_button_save)
                    }

                    R.id.nullMultipliersScoreFragment ->
                    {
                        positiveButton.visibility = View.VISIBLE
                        positiveButton.setText(R.string.dialog_title_button_save)
                    }
                }
            }
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        scoreViewModel.dismissEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface)
    {
        skatGameViewModel.refresh()
        super.onDismiss(dialog)
    }
}