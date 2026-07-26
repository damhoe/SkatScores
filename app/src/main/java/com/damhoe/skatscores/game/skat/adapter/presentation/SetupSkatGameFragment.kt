package com.damhoe.skatscores.game.skat.adapter.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentSetupSkatGameBinding
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.damhoe.skatscores.shared.utils.LayoutMargins
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupSkatGameFragment : Fragment(R.layout.fragment_setup_skat_game)
{
    private val viewModel: SetupSkatGameViewModel by viewModels()
    private lateinit var binding: FragmentSetupSkatGameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupSkatGameBinding.bind(view)

        setupInsets()
        setupNavigation()
        setupUIListeners()
        observeViewModel()
    }

    private fun setupInsets()
    {
        InsetsManager.applyStatusBarInsets(binding.toolbar)
        val buttonMargin = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
        val defaultMargins = LayoutMargins(
            buttonMargin,
            buttonMargin,
            buttonMargin,
            buttonMargin
        )
        InsetsManager.applyNavigationBarInsets(binding.startButton, defaultMargins)
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView)
    }

    private fun setupNavigation()
    {
        binding.startButton.setOnClickListener {
            viewModel.createSkatGameCommand.value?.let { command ->
                viewModel.handle(command)
            } ?: Log.e("SkatGameSetup", "CreateSkatGameCommand was null, cannot navigate.")
        }
    }


    private fun setupUIListeners()
    {
        binding.listNameEditText.doAfterTextChanged { editable ->
            viewModel.title.postValue(editable.toString())
        }

        binding.roundCountNumberPicker.apply {
            val displayValues = SkatRoundCount.ALLOWED_VALUES.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = displayValues.size - 1
            displayedValues = displayValues
        }

        binding.roundCountNumberPicker.setOnValueChangedListener { _, _, newValIndex ->
            val selectedRoundCount = SkatRoundCount(SkatRoundCount.ALLOWED_VALUES[newValIndex])
            viewModel.roundCount.postValue(selectedRoundCount)
        }

        binding.scoringSettingsRg.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked)
            {
                val useTournamentScoring = checkedId == R.id.tournament_scoring_rb
                viewModel.useTournamentScoring.postValue(useTournamentScoring)
            }
        }
    }

    private fun observeViewModel()
    {
        viewModel.canCreateSkatGame.observe(viewLifecycleOwner) { canCreate ->
            binding.startButton.isEnabled = canCreate
        }

        viewModel.roundCount.observe(viewLifecycleOwner) { roundCount ->
            val selectedRoundCount =
                SkatRoundCount.ALLOWED_VALUES[binding.roundCountNumberPicker.value]
            if (roundCount.value != selectedRoundCount)
            {
                binding.roundCountNumberPicker.value =
                    SkatRoundCount.ALLOWED_VALUES.indexOf(roundCount.value)
            }
            Log.d("SkatGameSetup", "Rounds observed: ${roundCount.value}")
        }

        viewModel.useTournamentScoring.observe(viewLifecycleOwner) { useTournamentScoring ->
            val checkedId =
                if (useTournamentScoring == true)
                    R.id.tournament_scoring_rb
                else R.id.simple_scoring_rb
            if (binding.scoringSettingsRg.checkedButtonId != checkedId)
            {
                binding.scoringSettingsRg.check(checkedId)
            }
            Log.d("SkatGameSetup", "Tournament scoring observed: $useTournamentScoring")
        }

        viewModel.navigateToGame.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { game ->
                Toast.makeText(context, "Created game", Toast.LENGTH_LONG)
                    .show()
                val action = SetupSkatGameFragmentDirections
                    .actionSetupToSkatGame(game.id.toString())
                findNavController().navigate(action)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}