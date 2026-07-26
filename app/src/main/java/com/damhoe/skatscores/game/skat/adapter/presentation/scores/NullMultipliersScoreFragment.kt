package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreNullMultipliersBinding
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NullMultipliersScoreFragment :
    Fragment(R.layout.fragment_score_null_multipliers)
{
    private lateinit var binding: FragmentScoreNullMultipliersBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreNullMultipliersBinding.bind(view)

        binding.handChip.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
            {
                if (binding.ouvertChip.isChecked)
                {
                    scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.HAND_OUVERT)
                }
                else
                {
                    scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.HAND)
                }
            }

            if (binding.ouvertChip.isChecked)
            {
                scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.OUVERT)
            }
            else
            {
                scoreViewModel.nullOptions.postValue(null)
            }
        }

        binding.ouvertChip.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
            {
                if (binding.handChip.isChecked)
                {
                    scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.HAND_OUVERT)
                }
                else
                {
                    scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.OUVERT)
                }
            }

            if (binding.handChip.isChecked)
            {
                scoreViewModel.nullOptions.postValue(SkatScore.Null.NullOptions.HAND)
            }
            else
            {
                scoreViewModel.nullOptions.postValue(null)
            }
        }
    }
}