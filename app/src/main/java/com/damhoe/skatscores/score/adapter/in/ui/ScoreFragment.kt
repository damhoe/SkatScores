package com.damhoe.skatscores.score.adapter.`in`.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreBinding
import com.damhoe.skatscores.game.GameActivity
import com.damhoe.skatscores.score.domain.CreateScoreRequest
import com.damhoe.skatscores.score.domain.SkatResult
import com.damhoe.skatscores.score.domain.SkatScoreCommand
import com.damhoe.skatscores.score.domain.SkatSuit
import com.damhoe.skatscores.score.domain.SkatSuit.CLUBS
import com.damhoe.skatscores.score.domain.SkatSuit.DIAMONDS
import com.damhoe.skatscores.score.domain.SkatSuit.GRAND
import com.damhoe.skatscores.score.domain.SkatSuit.HEARTS
import com.damhoe.skatscores.score.domain.SkatSuit.INVALID
import com.damhoe.skatscores.score.domain.SkatSuit.NULL
import com.damhoe.skatscores.score.domain.SkatSuit.SPADES
import com.damhoe.skatscores.score.domain.UpdateScoreRequest
import com.damhoe.skatscores.shared_ui.utils.InsetsManager
import com.damhoe.skatscores.shared_ui.utils.LayoutMargins
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import java.util.EnumMap
import javax.inject.Inject

class ScoreFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ScoreViewModelFactory
    lateinit var viewModel: ScoreViewModel
    lateinit var sharedViewModel: SharedScoreResponseViewModel
    lateinit var binding: FragmentScoreBinding

    // Action maps
    private lateinit var buttonSkatSuitMap: Map<Int, SkatSuit>
    private lateinit var buttonPlayerActionMap: Map<Int, Runnable>
    private lateinit var playerPositionCheckButtonMap: Map<Int, Runnable>
    private var suitCheckButtonMap: Map<SkatSuit, Runnable> = EnumMap(SkatSuit::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as GameActivity).appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load score requests from arguments
        // and init the view model with it
        requireArguments().getParcelable<UpdateScoreRequest>("UpdateScoreRequest")
            ?.let { viewModelFactory.setUpdateRequest(it) }
        requireArguments().getParcelable<CreateScoreRequest>("CreateScoreRequest")
            ?.let { viewModelFactory.setCreateRequest(it) }

        viewModel = ViewModelProvider(this, viewModelFactory)[ScoreViewModel::class.java]
        sharedViewModel =
            ViewModelProvider(requireActivity())[SharedScoreResponseViewModel::class.java]

        // Reset the shared view model state
        // because its lifecycle is bound to the activity
        sharedViewModel.reset()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_score, container, false)
        initializeButtonSkatSuitMap()
        initializeButtonPlayerActionMap()
        binding.toggleGroupSoloPlayer.addOnButtonCheckedListener { group: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                handlePlayerButtonCheck(checkedId)
            }
        }
        binding.toggleGroupResult.addOnButtonCheckedListener {
                group: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                if (checkedId == R.id.buttonOverbid) {
                    viewModel!!.setResult(SkatResult.OVERBID)
                } else if (checkedId == R.id.buttonWon) {
                    viewModel!!.setResult(SkatResult.WON)
                } else {
                    viewModel!!.setResult(SkatResult.LOST)
                }
            }
        }
        binding.toggleGroupSuit.addOnButtonCheckedListener {
                group: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                binding.toggleGroupSpecialSuit.clearChecked()
                handleSuitButtonCheck(checkedId)
            }
        }
        binding.toggleGroupSpecialSuit.addOnButtonCheckedListener {
                group: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                binding!!.toggleGroupSuit.clearChecked()
                handleSuitButtonCheck(checkedId)
            }
        }
        binding.announcementsChips.setOnCheckedStateChangeListener {
                group: ChipGroup?, checkedIds: List<Int> ->
            binding.content.requestLayout()
            handleAnnouncementChips(checkedIds)
        }
        binding.winLevelChips.setOnCheckedStateChangeListener {
                group: ChipGroup?, checkedIds: List<Int> ->
            binding.content.requestLayout()
            handleWinLevelChips(checkedIds)
        }
        binding.spitzenSlider.addOnChangeListener {
                slider: Slider?, value: Float, fromUser: Boolean ->
            binding.spitzenLabel.text = value.toInt()
                .toString()
            viewModel.setSpitzen(value.toInt())
        }
        binding.spitzenRemoveButton.setOnClickListener {
                view: View? ->
            binding.spitzenSlider.value = binding.spitzenSlider.value - 1
        }
        binding.spitzenAddButton.setOnClickListener { view: View? ->
            binding.spitzenSlider.value = binding.spitzenSlider.value + 1
        }
        binding.scoreDoneButton.setOnClickListener {
                view: View? ->
            saveScore()
            findNavController().navigateUp()
        }
        return binding!!.root
    }

    private fun handleAnnouncementChips(checkedIds: List<Int>) {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_announced_chip) &&
            !checkedIds.contains(R.id.schneider_announced_chip)
        ) {
            binding.announcementsChips.check(R.id.schneider_announced_chip)
            return
        }

        // Set viewModel data
        viewModel.setSchneiderAnnounced(checkedIds.contains(R.id.schneider_announced_chip))
        viewModel.setSchwarzAnnounced(checkedIds.contains(R.id.schwarz_announced_chip))
    }

    private fun handleWinLevelChips(checkedIds: List<Int>) {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_chip) &&
            !checkedIds.contains(R.id.schneider_chip)
        ) {
            binding.winLevelChips.check(R.id.schneider_chip)
            return
        }
        viewModel.setHand(checkedIds.contains(R.id.hand_chip))
        viewModel.setOuvert(checkedIds.contains(R.id.ouvert_chip))
        viewModel.setSchneider(checkedIds.contains(R.id.schneider_chip))
        viewModel.setSchwarz(checkedIds.contains(R.id.schwarz_chip))
    }

    private fun findNavController(): NavController {
        return findNavController(requireActivity(), R.id.nav_host_fragment)
    }

    private fun handlePlayerButtonCheck(buttonId: Int) {
        val action = buttonPlayerActionMap[buttonId]
        action?.run()
    }

    private fun handleSuitButtonCheck(buttonId: Int) {
        buttonSkatSuitMap[buttonId]?.let { viewModel.setSuit(it) }
    }

    // Action if the button gets checked
    private fun initializeButtonPlayerActionMap() {
        binding.run {
            buttonPlayerActionMap = mapOf(
                buttonPasse.id to Runnable { viewModel.setPasse() },
                buttonPlayer1.id to Runnable { viewModel.setPlayerPosition(1) },
                buttonPlayer2.id to Runnable { viewModel.setPlayerPosition(2) },
                buttonPlayer3.id to Runnable { viewModel.setPlayerPosition(3) },
            )

            playerPositionCheckButtonMap = mapOf(
                viewModel.positions[0] to Runnable {
                    toggleGroupSoloPlayer.check(buttonPlayer1.id)
                },
                viewModel.positions[1] to Runnable {
                    toggleGroupSoloPlayer.check(buttonPlayer1.id)
                },
                viewModel.positions[2] to Runnable {
                    toggleGroupSoloPlayer.check(buttonPlayer1.id)
                }
            )
        }
    }

    private fun initializeButtonSkatSuitMap() {
        binding.run {
            buttonSkatSuitMap = mapOf(
                buttonClubs.id to CLUBS,
                buttonClubs.id to SPADES,
                buttonClubs.id to HEARTS,
                buttonClubs.id to DIAMONDS,
                buttonClubs.id to GRAND,
                buttonClubs.id to NULL,
            )

            suitCheckButtonMap = mapOf(
                CLUBS to Runnable { toggleGroupSuit.check(buttonClubs.id) },
                SPADES to Runnable { toggleGroupSuit.check(buttonSpades.id) },
                HEARTS to Runnable { toggleGroupSuit.check(buttonHearts.id) },
                DIAMONDS to Runnable { toggleGroupSuit.check(buttonDiamonds.id) },
                GRAND to Runnable { toggleGroupSpecialSuit.check(buttonGrand.id) },
                NULL to Runnable { toggleGroupSpecialSuit.check(buttonNull.id) },
                INVALID to Runnable { toggleGroupSpecialSuit.clearChecked() }
            )
        }
    }

    /** @noinspection DataFlowIssue
     */
    private fun initializeUI() {
        val names = viewModel.names
        if (names.size != 3) {
            throw RuntimeException(
                "Expected 3 player names" +
                        " in observed LiveData but got " + names.size
            )
        }
        binding.buttonPlayer1.text = names[0]
        binding.buttonPlayer2.text = names[1]
        binding.buttonPlayer3.text = names[2]
        val command: SkatScoreCommand? = viewModel.getScoreCommand().value
        if (command != null) {
            if (command.isPasse) {
                binding.toggleGroupSoloPlayer.check(R.id.buttonPasse)
                binding.spitzenSlider.value = 1f
                return
            }
            if (command.isLost) {
                binding.toggleGroupResult.check(R.id.buttonLost)
            } else if (command.isWon) {
                binding.toggleGroupResult.check(R.id.buttonWon)
            } else {
                binding.toggleGroupResult.check(R.id.buttonOverbid)
            }
            playerPositionCheckButtonMap[command.playerPosition]?.run()
            suitCheckButtonMap[command.suit]?.run()
            binding.spitzenSlider.value = command.spitzen.toFloat()
            binding.handChip.isChecked = command.isHand
            binding.ouvertChip.isChecked = command.isOuvert
            binding.schneiderChip.isChecked = command.isSchneider
            binding.schwarzChip.isChecked = command.isSchwarz
            binding.schneiderAnnouncedChip.isChecked = command.isSchneiderAnnounced
            binding.schwarzAnnouncedChip.isChecked = command.isSchwarzAnnounced
            binding.spitzenSlider.value = command.spitzen.toFloat()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup navigation
        val navController: NavController = findNavController()
        setupWithNavController(binding.toolbar, navController)

        // Set insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        val marginRight = resources.getDimensionPixelSize(R.dimen.fab_margin_right)
        val marginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
        val defaultMargins = LayoutMargins(0, 0, marginRight, marginBottom)
        InsetsManager.applyNavigationBarInsets(binding.scoreDoneButton, defaultMargins)
        InsetsManager.applyNavigationBarInsets(binding.content)
        viewModel.isSuitsEnabled.observe(viewLifecycleOwner) { isEnabled: Boolean ->
            enableSuitButtons(
                isEnabled
            )
        }
        viewModel.isHandEnabled.observe(viewLifecycleOwner) { isEnabled: Boolean? ->
            binding.handChip.isEnabled = isEnabled!!
        }
        viewModel.isSchneiderSchwarzEnabled.observe(viewLifecycleOwner) { isEnabled: Boolean ->
            enableSchneiderSchwarzChips(
                isEnabled
            )
        }
        viewModel.isOuvertEnabled.observe(
            viewLifecycleOwner
        ) { isEnabled: Boolean? -> binding.ouvertChip.isEnabled = isEnabled!! }
        viewModel.isSpitzenEnabled.observe(
            viewLifecycleOwner
        ) { isEnabled: Boolean? -> binding.spitzenSlider.isEnabled = isEnabled!! }
        viewModel.isIncreaseSpitzenEnabled.observe(
            viewLifecycleOwner
        ) { isEnabled: Boolean? -> binding.spitzenAddButton.isEnabled = isEnabled!! }
        viewModel.isDecreaseSpitzenEnabled.observe(
            viewLifecycleOwner
        ) { isEnabled: Boolean? -> binding.spitzenRemoveButton.isEnabled = isEnabled!! }
        viewModel.isResultsEnabled.observe(viewLifecycleOwner) { isEnabled: Boolean? ->
            binding.buttonWon.isEnabled = isEnabled!!
            binding.buttonLost.isEnabled = isEnabled
            binding.buttonOverbid.isEnabled = isEnabled
        }
        viewModel.skatResult.observe(
            viewLifecycleOwner,
            Observer<SkatResult> { skatResult: SkatResult ->
                if (skatResult === SkatResult.WON) {
                    binding.toggleGroupResult.check(R.id.buttonWon)
                } else if (skatResult === SkatResult.LOST) {
                    binding.toggleGroupResult.check(R.id.buttonLost)
                } else if (skatResult === SkatResult.OVERBID) {
                    binding.toggleGroupResult.check(R.id.buttonOverbid)
                } else {
                    binding.toggleGroupResult.clearChecked()
                }
            })
        viewModel.suit.observe(viewLifecycleOwner, Observer<SkatSuit> { skatSuit: SkatSuit ->
            if (skatSuit === INVALID) {
                binding.announcementsChips.clearCheck()
                binding.winLevelChips.clearCheck()
                binding.spitzenSlider.value = binding!!.spitzenSlider.valueFrom
                binding.toggleGroupSuit.clearChecked()
            } else if (skatSuit === CLUBS) {
                binding.toggleGroupSuit.check(R.id.buttonClubs)
            } else if (skatSuit === SPADES) {
                binding.toggleGroupSuit.check(R.id.buttonSpades)
            } else if (skatSuit === HEARTS) {
                binding.toggleGroupSuit.check(R.id.buttonHearts)
            } else if (skatSuit === DIAMONDS) {
                binding.toggleGroupSuit.check(R.id.buttonDiamonds)
            } else if (skatSuit === NULL) {
                binding.toggleGroupSuit.check(R.id.buttonNull)
            } else if (skatSuit === GRAND) {
                binding.toggleGroupSuit.check(R.id.buttonGrand)
            }
        })
        initializeUI()
    }

    private fun enableSchneiderSchwarzChips(isEnabled: Boolean) {
        binding.run {
            schneiderChip.isEnabled = isEnabled
            schwarzChip.isEnabled = isEnabled
            schneiderAnnouncedChip.isEnabled = isEnabled
            schwarzAnnouncedChip.isEnabled = isEnabled
        }
    }

    private fun enableSuitButtons(isEnabled: Boolean) {
        binding.run {
            buttonClubs.isEnabled = isEnabled
            buttonHearts.isEnabled = isEnabled
            buttonDiamonds.isEnabled = isEnabled
            buttonSpades.isEnabled = isEnabled
            buttonNull.isEnabled = isEnabled
            buttonGrand.isEnabled = isEnabled

            buttonHearts.alpha = if (isEnabled) 1f else 0.38f
            buttonDiamonds.alpha = if (isEnabled) 1f else 0.38f
        }
    }


    private fun saveScore() {
        viewModel.saveScore().also { sharedViewModel.setScoreResult(it) }
    }
}