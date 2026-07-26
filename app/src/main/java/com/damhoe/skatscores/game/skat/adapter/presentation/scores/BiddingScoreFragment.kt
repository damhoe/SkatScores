package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreBiddingBinding
import com.damhoe.skatscores.game.skat.domain.SkatBid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BiddingScoreFragment :
    Fragment(R.layout.fragment_score_bidding)
{
    private lateinit var binding: FragmentScoreBiddingBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )
    private val args: BiddingScoreFragmentArgs by navArgs()

    private val biddingValues = SkatBid.BiddingValues.toList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreBiddingBinding.bind(view)

        // The dialog saves from the view model, so make sure it holds what this step shows.
        scoreViewModel.soloPlayer.value = args.soloPlayer
        args.skatSuit?.let { scoreViewModel.suit.value = it }

        setupNumberPicker()
    }

    private fun setupNumberPicker() {
        val displayValues = biddingValues.map { it.toString() }.toTypedArray()

        binding.biddingNumberPicker.apply {
            minValue = 0
            maxValue = displayValues.size - 1
            displayedValues = displayValues
            wrapSelectorWheel = false
            // The picker only reports changes, so the initial selection has to match the
            // bid the view model already holds.
            value = biddingValues
                .indexOf(scoreViewModel.bid.value?.value)
                .coerceAtLeast(0)
            setOnValueChangedListener { _, _, selectedIndex ->
                scoreViewModel.bid.value = SkatBid(biddingValues[selectedIndex])
            }
        }
    }
}