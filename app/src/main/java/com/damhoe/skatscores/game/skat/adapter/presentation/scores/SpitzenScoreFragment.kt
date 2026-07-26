package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreSpitzenBinding
import com.damhoe.skatscores.game.skat.domain.Spitzen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpitzenScoreFragment :
    Fragment(R.layout.fragment_score_spitzen)
{
    private lateinit var binding: FragmentScoreSpitzenBinding
    private val scoreViewModel: SharedScoreViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )
    private val args: SpitzenScoreFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScoreSpitzenBinding.bind(view)
        binding.viewModel = scoreViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Initial setup
        scoreViewModel.setSpitzen(Spitzen(1)) // Assuming args has this

        scoreViewModel.spitzen.observe(viewLifecycleOwner) { spitzen ->
            updateUI(spitzen)
        }

        binding.spitzenSlider.addOnChangeListener { _, value, fromUser ->
            val spitzen = createSpitzenSafe(value.toInt())
            if (fromUser) scoreViewModel.setSpitzen(spitzen)
        }

        binding.spitzenAddButton.setOnClickListener {
            val current = scoreViewModel.spitzen.value ?: Spitzen(1)
            if (current.value == 4 &&
                binding.spitzenSlider.valueTo == 4f
            )
            {
                // Expand range to 11
                binding.spitzenSlider.valueTo = 11f
            }
            scoreViewModel.setSpitzen(createSpitzenSafe(current.value + 1))
        }

        binding.spitzenRemoveButton.setOnClickListener {
            val current = scoreViewModel.spitzen.value ?: Spitzen(1)
            scoreViewModel.setSpitzen(createSpitzenSafe(current.value - 1))
        }
    }

    private fun updateUI(spitzen: Spitzen)
    {
        binding.spitzenSlider.value = spitzen.value.toFloat()

        binding.spitzenRemoveButton.isEnabled = spitzen.value > 1
        binding.spitzenAddButton.isEnabled = spitzen.value < 11
    }

    private fun createSpitzenSafe(value: Int): Spitzen
    {
        return Spitzen(value.coerceIn(1, 11))
    }
}