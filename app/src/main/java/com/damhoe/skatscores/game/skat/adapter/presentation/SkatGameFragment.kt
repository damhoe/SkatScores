package com.damhoe.skatscores.game.skat.adapter.presentation

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.DialogGameSettingsBinding
import com.damhoe.skatscores.databinding.FragmentGameBinding
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.adapter.presentation.EditParticipantsDialogFragment.Companion.REQUEST_KEY
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkatGameFragment :
    Fragment(R.layout.fragment_game),
    IScoreActionListener
{
    private val viewModel: SharedSkatGameViewModel by hiltNavGraphViewModels(R.id.skat_game_nav_graph)

    private lateinit var binding: FragmentGameBinding
    private lateinit var scoreAdapter: SkatScoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGameBinding.bind(view)
        binding.viewModel = viewModel

        InsetsManager.applySystemBarInsets(binding.content)
        InsetsManager.applyNavigationBarInsets(binding.bottomAppBar)
        InsetsManager.applyNavigationBarInsets(binding.editScoreButton)

        setupNavigation()
        setupObservers()
        addMenu()
        setUpRecyclerView()
        setUpEditPlayersButton()
        setUpEditScoreButton()
    }

    private fun setUpEditPlayersButton()
    {
        binding.playerNames.buttonEdit.setOnClickListener { showEditPlayerDialog() }
    }

    private fun setupObservers()
    {
        setupDealerObserver()
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.skatGame.observe(viewLifecycleOwner)
        {
            val visibilityWinLossBonus =
                if (it.settings.scoringMode == SkatScoringMode.TOURNAMENT)
                    View.VISIBLE
                else
                    View.GONE

            binding.bottomSumView.apply {
                lostContainer.visibility = visibilityWinLossBonus
                soloContainer.visibility = visibilityWinLossBonus
                divider.visibility = visibilityWinLossBonus
            }

            scoreAdapter.submitList(it.scores)
        }

        setupPointsSummaryObserver()

        viewModel.skatParticipants.observe(viewLifecycleOwner)
        {
            scoreAdapter.setParticipants(it)
        }

        // Observer for navigation event
        viewModel.navigateUpEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { // Only proceed if the event has not been handled
                Snackbar.make(requireView(), "Failed to load game details.", Snackbar.LENGTH_LONG)
                    .show()
                findNavController().navigateUp() // Navigate back
            }
        }
    }

    private fun setupPointsSummaryObserver()
    {
        viewModel.totalPoints.observe(viewLifecycleOwner) { points ->
            binding.bottomSumView.apply {
                points1Text.text = pointsText(points, 0)
                points2Text.text = pointsText(points, 1)
                points3Text.text = pointsText(points, 2)
            }
        }
        viewModel.winBonus.observe(viewLifecycleOwner) { points ->
            binding.bottomSumView.apply {
                solo1Text.text = pointsText(points, 0)
                solo2Text.text = pointsText(points, 1)
                solo3Text.text = pointsText(points, 2)
            }
        }
        viewModel.lossOfOthersBonus.observe(viewLifecycleOwner) { points ->
            binding.bottomSumView.apply {
                lost1Text.text = pointsText(points, 0)
                lost2Text.text = pointsText(points, 1)
                lost3Text.text = pointsText(points, 2)
            }
        }
    }

    private fun pointsText(points: IntArray, seat: Int) =
        points.getOrNull(seat)?.toString() ?: "-"

    private fun setupDealerObserver()
    {
        viewModel.dealerPosition.observe(viewLifecycleOwner) {
            binding.playerNames.apply {
                when (it)
                {
                    0 ->
                    {
                        indicator1.alpha = 1.0f
                        indicator2.alpha = 0.0f
                        indicator3.alpha = 0.0f
                    }

                    1 ->
                    {
                        indicator1.alpha = 0.0f
                        indicator2.alpha = 1.0f
                        indicator3.alpha = 0.0f
                    }

                    2 ->
                    {
                        indicator1.alpha = 0.0f
                        indicator2.alpha = 0.0f
                        indicator3.alpha = 1.0f
                    }
                }
            }
        }
    }

    private fun addMenu()
    {
        binding.bottomAppBar.addMenuProvider(
            object : MenuProvider
            {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
                {
                    menu.clear()
                    menuInflater.inflate(R.menu.game_menu, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean
                {
                    return when (item.itemId)
                    {
                        R.id.game_edit ->
                        {
                            showGameSettingsDialog()
                            true
                        }

                        R.id.game_show_chart ->
                        {
                            navigateToGameGraph()
                            true
                        }

                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    private fun navigateToGameGraph()
    {
        findNavController().navigate(SkatGameFragmentDirections.actionSkatGameFragmentToSkatGameGraphFragment())
    }

    private fun showGameSettingsDialog()
    {
        val dialogBinding = DialogGameSettingsBinding.inflate(layoutInflater)
        viewModel.skatGame.value?.let { skatGame ->
            dialogBinding.listNameEditText.setText(skatGame.title.value)
            dialogBinding.roundCountText.text = skatGame.settings.roundCount.value.toString()
            dialogBinding.scoringSettingsRg.check(
                if (skatGame.settings.scoringMode == SkatScoringMode.TOURNAMENT)
                    R.id.tournament_scoring_rb
                else R.id.simple_scoring_rb
            )
        } ?: return // If skatGame is null, don't show dialog

        val dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setPositiveButton(getString(R.string.dialog_title_button_save)) { _, _ ->
                    val title = Title(dialogBinding.listNameEditText.text.toString())
                    val settings = viewModel.skatGame.value?.settings?.copy(
                        scoringMode = if (dialogBinding.simpleScoringRb.isSelected)
                            SkatScoringMode.CLASSIC
                        else
                            SkatScoringMode.TOURNAMENT
                    ) ?: return@setPositiveButton
                    viewModel.updateGameSettings(title, settings)
                }
                .setNegativeButton(getString(R.string.dialog_title_button_cancel)) { d, _ -> d.cancel() }
                .create()

        dialogBinding.listNameEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable?)
            {
                val maxLength = dialogBinding.listNameInput.counterMaxLength
                if ((s?.length ?: 0) > maxLength)
                {
                    s?.delete(maxLength, s.length)
                }
                val title = s.toString().trim()
                dialogBinding.listNameInput.error =
                    if (title.isEmpty()) getString(R.string.error_valid_title_required) else null
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled =
                    dialogBinding.listNameInput.error == null
            }
        })
        dialog.show()
    }

    @SuppressLint("InflateParams")
    private fun showEditPlayerDialog()
    {
        val dialogFragment = EditParticipantsDialogFragment.newInstance(
            viewModel.allRegisteredPlayers.value,
            viewModel.skatParticipants.value?.foreHand!!,
            viewModel.skatParticipants.value?.middleHand!!,
            viewModel.skatParticipants.value?.rearHand!!,
        )

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this,
        ) { _, bundle ->
            val forehand: SkatParticipant
            val middlehand: SkatParticipant
            val rearhand: SkatParticipant
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                forehand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_FOREHAND,
                    SkatParticipant::class.java
                )!!
                middlehand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_MIDDLEHAND,
                    SkatParticipant::class.java
                )!!
                rearhand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_REARHAND,
                    SkatParticipant::class.java
                )!!
            } else
            {
                @Suppress("DEPRECATION")
                forehand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_FOREHAND
                )!!
                @Suppress("DEPRECATION")
                middlehand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_MIDDLEHAND
                )!!
                @Suppress("DEPRECATION")
                rearhand = bundle.getParcelable(
                    EditParticipantsDialogFragment.RESULT_REARHAND
                )!!
            }
            viewModel.updateParticipants(
                forehand,
                middlehand,
                rearhand
            )
        }

        dialogFragment.show(
            parentFragmentManager,
            EditParticipantsDialogFragment.TAG
        )
    }

    override fun notifyDelete()
    { /* ... */
    }

    override fun notifyDetails(skatScore: SkatScore)
    { /* ... */
    }

    override fun notifyEdit(skatScore: SkatScore)
    { /* ... */
    }

    private fun setupNavigation()
    {
        binding.returnButton.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setUpRecyclerView()
    {
        binding.scoresRv.layoutManager = LinearLayoutManager(requireContext())
        scoreAdapter = SkatScoreAdapter(this)
        binding.scoresRv.adapter = scoreAdapter
    }

    private fun setUpEditScoreButton()
    {
        binding.editScoreButton.setOnClickListener {
            val action = SkatGameFragmentDirections.toScoreNavGraph(viewModel.skatGameId.toString())
            findNavController().navigate(action)
        }
    }
}