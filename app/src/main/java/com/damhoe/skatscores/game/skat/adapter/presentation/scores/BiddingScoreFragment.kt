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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreBiddingBinding.bind(view)
        setupNumberPicker()
    }

    private fun setupNumberPicker() {
        val displayValues = SkatBid.BiddingValues.map { it.toString() }.toTypedArray()

        binding.biddingNumberPicker.apply {
            minValue = 0
            maxValue = displayValues.size - 1
            displayedValues = displayValues
            wrapSelectorWheel = false
        }
    }
}