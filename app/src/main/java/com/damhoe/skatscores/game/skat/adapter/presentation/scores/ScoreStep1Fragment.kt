package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreStep1Binding
import com.damhoe.skatscores.game.skat.adapter.presentation.SharedSkatGameViewModel
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.damhoe.skatscores.shared.utils.LayoutMargins
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreStep1Fragment :
    Fragment(R.layout.fragment_score_step1)
{
    private val scoreViewModel: SharedScoreViewModel by viewModels()
    private val skatGameViewModel: SharedSkatGameViewModel by hiltNavGraphViewModels(R.id.skat_game_nav_graph)

    private val args: ScoreStep1FragmentArgs by navArgs()

    private lateinit var binding: FragmentScoreStep1Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreStep1Binding.bind(view)

        InsetsManager.applyStatusBarInsets(binding.toolbar)
        val marginRight = resources.getDimensionPixelSize(R.dimen.fab_margin_right)
        val marginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
        val defaultMargins = LayoutMargins(0, 0, marginRight, marginBottom)
        InsetsManager.applyNavigationBarInsets(binding.nextStepButton, defaultMargins)
        InsetsManager.applyNavigationBarInsets(binding.content)

        binding.toggleGroupResult.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
            {
                handleResultButtonCheck(checkedId)
            }
        }

        addSoloPlayerCardOnClickListeners()
        initNavigationListeners()

        skatGameViewModel.skatParticipants.observe(viewLifecycleOwner) { participants ->
            binding.run {
                textPlayer1.text = participants.foreHand.name.value
                textPlayer2.text = participants.middleHand.name.value
                textPlayer3.text = participants.rearHand.name.value
            }
        }
    }

    private fun addSoloPlayerCardOnClickListeners()
    {
        val soloPlayerCardsWithPositions = mapOf(
            binding.cardPlayer1 to 1,
            binding.cardWon to 2,
            binding.cardPlayer3 to 3,
        )

        soloPlayerCardsWithPositions.forEach { card, position ->
            card.setOnClickListener {
                clearCheckedSoloPlayer() // You'd still need this function
                card.isChecked = true
            }
        }

        binding.cardPasse.setOnClickListener {
            clearCheckedSoloPlayer()
            binding.cardPasse.apply { isChecked = true }

            //scoreViewModel.createPasseScore(args.scoreRequest)

            findNavController().navigateUp()
        }
    }

    private fun clearCheckedSoloPlayer() = binding.run {
        cardPlayer1.isChecked = false
        cardWon.isChecked = false
        cardPlayer3.isChecked = false
        cardPasse.isChecked = false
    }

    private fun findNavController() = Navigation.findNavController(
        requireActivity(), R.id.nav_host_fragment
    )


    private fun writePlayerNames() = binding.also {
    }

    private fun checkPlayer(position: Int) = binding.apply {
        when (position)
        {
            0 -> cardPlayer1.isChecked = true
            1 -> cardWon.isChecked = true
            2 -> cardPlayer3.isChecked = true
        }
    }


    private fun initNavigationListeners()
    {
        binding.nextStepButton.setOnClickListener {
            findNavController().navigate(ScoreStep1FragmentDirections.actionScoreStep1FragmentToScoreStep2DetailsFragment())
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun handleResultButtonCheck(checkedId: Int)
    {

    }

    private fun saveScore()
    {

    }
}