package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreMultipliersBinding
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultipliersScoreFragment :
    Fragment(R.layout.fragment_score_multipliers)
{
    private lateinit var binding: FragmentScoreMultipliersBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreMultipliersBinding.bind(view)

        binding.handChip.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
            {
                if (binding.ouvertChip.isChecked)
                {
                    scoreViewModel.grandOrSuitOptions.postValue(SkatScore.GrandOrSuit.GrandOrSuitOptions.HAND)
                }
                else
                {
                    scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.HAND)
                }
            }
        }

        binding.ouvertChip.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
            {

            }
        }
    }
}