package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreWhoWonBinding
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WhoWonScoreFragment :
    Fragment(R.layout.fragment_score_who_won)
{
    private lateinit var binding: FragmentScoreWhoWonBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreWhoWonBinding.bind(view)

        scoreViewModel.skatParticipants.observe(viewLifecycleOwner) { participants ->
            binding.participants = participants
        }

        binding.cardPasse.setOnClickListener {
            scoreViewModel.addPasseScore();
            scoreViewModel.dismiss()
        }

        binding.cardForeHand.setOnClickListener {
            scoreViewModel.soloPlayer.postValue(binding.participants!!.foreHand)
            val action = WhoWonScoreFragmentDirections.toResultScoreFragment(
                binding.participants!!.foreHand
            )
            findNavController().navigate(action)
        }

        binding.cardMiddleHand.setOnClickListener {
            scoreViewModel.soloPlayer.postValue(binding.participants!!.middleHand)
            val action = WhoWonScoreFragmentDirections.toResultScoreFragment(
                binding.participants!!.middleHand
            )
            findNavController().navigate(action)
        }

        binding.cardRearHand.setOnClickListener {
            scoreViewModel.soloPlayer.postValue(binding.participants!!.rearHand)
            val action = WhoWonScoreFragmentDirections.toResultScoreFragment(
                binding.participants!!.rearHand
            )
            findNavController().navigate(action)
        }
    }
}