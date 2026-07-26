package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreStep2DetailsBinding
import com.damhoe.skatscores.game.skat.adapter.presentation.SharedSkatGameViewModel
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit.*
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreType
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.damhoe.skatscores.shared.utils.LayoutMargins
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreStep2DetailsFragment : Fragment(R.layout.fragment_score_step2_details)
{
    private val maxSpitzenValueTo = 11f
    private val normalSpitzenValueTo = 4f


    private val scoreViewModel: SharedScoreViewModel by navGraphViewModels(R.id.skat_game_nav_graph)

    private val gameViewModel: SharedSkatGameViewModel by viewModels({ requireActivity() })
    private lateinit var binding: FragmentScoreStep2DetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(
            view, savedInstanceState
        )

        binding = FragmentScoreStep2DetailsBinding.bind(view)

        // Setup navigation
        val navController: NavController = findNavController()
        setupWithNavController(
            binding.toolbar, navController
        )

        // Set insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        val marginRight = resources.getDimensionPixelSize(R.dimen.fab_margin_right)
        val marginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
        val defaultMargins = LayoutMargins(
            0, 0, marginRight, marginBottom
        )
        InsetsManager.applyNavigationBarInsets(
            binding.scoreDoneButton, defaultMargins
        )
        InsetsManager.applyNavigationBarInsets(binding.content)


        addListenersForChips()
        addListenersForSpitzen()
        addSuitOnClickListeners()


        binding.scoreDoneButton.setOnClickListener {
            saveScore()
        }

        initializeUI()
    }


    private fun addListenersForChips() = binding.apply {
        announcementsChips.setOnCheckedStateChangeListener { _, checkedIds: List<Int> ->
            content.requestLayout()
            handleAnnouncementChips(checkedIds)
        }

        winLevelChips.setOnCheckedStateChangeListener { _, checkedIds: List<Int> ->
            content.requestLayout()
            handleWinLevelChips(checkedIds)
        }
    }

    private fun updateSpitzenButtons(spitzen: Float)
    {
        with(binding) {
            spitzenAddButton.isEnabled = spitzen < spitzenSlider.valueTo
            spitzenRemoveButton.isEnabled = spitzen > spitzenSlider.valueFrom
        }
    }

    private fun addListenersForSpitzen() = binding.apply {
        spitzenSlider.addOnChangeListener { _, value, _ ->
            spitzenLabel.text = value.toString()

            updateSpitzenButtons(value)
        }
        spitzenRemoveButton.setOnClickListener { spitzenSlider.value -= 1 }
        spitzenAddButton.setOnClickListener { spitzenSlider.value += 1 }

        spitzenSwitch.setOnCheckedChangeListener { _, isChecked ->
            spitzenSlider.valueTo = if (isChecked)
            {
                maxSpitzenValueTo
            } else
            {
                if (spitzenSlider.value > normalSpitzenValueTo)
                {
                    spitzenSlider.value = normalSpitzenValueTo
                }
                normalSpitzenValueTo
            }
            updateSpitzenButtons(spitzenSlider.value)
        }
    }

    private fun handleAnnouncementChips(checkedIds: List<Int>)
    {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_announced_chip) && !checkedIds.contains(R.id.schneider_announced_chip))
        {
            binding.announcementsChips.check(R.id.schneider_announced_chip)
            return
        }

        // Set viewModel data

    }

    private fun handleWinLevelChips(checkedIds: List<Int>)
    {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_chip) && !checkedIds.contains(R.id.schneider_chip))
        {
            binding.winLevelChips.check(R.id.schneider_chip)
            return
        }

    }

    private fun findNavController() = Navigation.findNavController(
        requireActivity(), R.id.nav_host_fragment
    )

    private fun handleSuitButtonCheck(buttonId: Int) = with(binding) {
        scoreViewModel.apply {
            when (buttonId)
            {

            }
        }
    }

    /** @noinspection DataFlowIssue
     */
    private fun initializeUI()
    {

    }

    private fun addSuitOnClickListeners()
    {
        val suitCardsWithSuits = mapOf(
            binding.cardClubs to CLUBS,
            binding.cardHearts to HEARTS,
            binding.cardDiamonds to HEARTS,
            binding.cardSpades to SPADES,
            binding.cardNull to SkatScoreType.NULL,
            binding.cardGrand to GRAND,
        )

        suitCardsWithSuits.forEach { card, suit ->
            card.setOnClickListener {
                clearCheckedSuits()
                card.isChecked = true
                //scoreViewModel.setSuit(suit)
            }
        }
    }

    private fun clearCheckedSuits() = binding.run {
        cardClubs.isChecked = false
        cardSpades.isChecked = false
        cardHearts.isChecked = false
        cardDiamonds.isChecked = false
        cardNull.isChecked = false
        cardGrand.isChecked = false
    }

    private fun changeAnnouncementsChipsAccessibility(isEnabled: Boolean) = with(binding) {
        listOf(
            schneiderChip, schwarzChip, schneiderAnnouncedChip, schwarzAnnouncedChip
        ).forEach { it.isEnabled = isEnabled }
    }

    private fun saveScore()
    {

    }
}