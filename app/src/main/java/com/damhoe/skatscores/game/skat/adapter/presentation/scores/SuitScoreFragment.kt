package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreSuitBinding
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuitScoreFragment :
    Fragment(R.layout.fragment_score_suit)
{
    private lateinit var binding: FragmentScoreSuitBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )
    private val args: SuitScoreFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreSuitBinding.bind(view)

        binding.cardNull.setOnClickListener {
            navigateNext(null)
        }

        binding.cardClubs.setOnClickListener {
            scoreViewModel.suit.postValue(SkatSuit.CLUBS)
            navigateNext(SkatSuit.CLUBS)
        }

        binding.cardSpades.setOnClickListener {
            scoreViewModel.suit.postValue(SkatSuit.SPADES)
            navigateNext(SkatSuit.SPADES)
        }

        binding.cardHearts.setOnClickListener {
            scoreViewModel.suit.postValue(SkatSuit.HEARTS)
            navigateNext(SkatSuit.HEARTS)
        }

        binding.cardDiamonds.setOnClickListener {
            scoreViewModel.suit.postValue(SkatSuit.DIAMONDS)
            navigateNext(SkatSuit.DIAMONDS)
        }

        binding.cardGrand.setOnClickListener {
            scoreViewModel.suit.postValue(SkatSuit.GRAND)
            navigateNext(SkatSuit.GRAND)
        }
    }

    fun navigateNext(suitOrNull: SkatSuit?)
    {
        findNavController().navigate(getAction(suitOrNull))
    }

    fun getAction(
        suitOrNull: SkatSuit?
    ): NavDirections
    {
        if (suitOrNull == null)
        {
            return SuitScoreFragmentDirections.toNullMultipliersScoreFragment()
        }

        return when (args.skatResult)
        {
            is SkatResult.Won ->
            {
                SuitScoreFragmentDirections.toSpitzenScoreFragment(
                    args.soloPlayer,
                    args.skatResult,
                    suitOrNull,
                )
            }

            is SkatResult.Lost ->
            {
                SuitScoreFragmentDirections.toSpitzenScoreFragment(
                    args.soloPlayer,
                    args.skatResult,
                    suitOrNull,
                )
            }

            is SkatResult.Overbid ->
            {
                SuitScoreFragmentDirections.toBiddingScoreFragment(
                    args.soloPlayer,
                    suitOrNull,
                )
            }
        }
    }
}