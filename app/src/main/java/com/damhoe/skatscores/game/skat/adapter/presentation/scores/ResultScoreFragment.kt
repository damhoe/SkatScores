package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreResultBinding
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.adapter.presentation.scores.SkatResult
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ResultScoreFragment :
    Fragment(R.layout.fragment_score_result)
{
    private lateinit var binding: FragmentScoreResultBinding
    private val args: ResultScoreFragmentArgs by navArgs()
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreResultBinding.bind(view)

        binding.cardWon.setOnClickListener {
            scoreViewModel.wonOrLost.postValue(WonOrLost.WON)
            navigate(SkatResult.Won())
        }

        binding.cardLost.setOnClickListener {
            scoreViewModel.wonOrLost.postValue(WonOrLost.LOST)
            navigate(SkatResult.Lost())
        }

        binding.cardOverbid.setOnClickListener {
            navigate(SkatResult.Overbid())
        }
    }

    fun navigate(result: SkatResult)
    {
        val action = ResultScoreFragmentDirections.toSuitScoreFragment(
            args.soloPlayer,
            result,
        )
        findNavController().navigate(action)
    }
}