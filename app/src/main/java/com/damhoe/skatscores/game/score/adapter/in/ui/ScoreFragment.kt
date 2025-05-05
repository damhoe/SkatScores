package com.damhoe.skatscores.game.score.adapter.`in`.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreBinding
import com.damhoe.skatscores.game.adapter.presentation.GameActivity
import com.damhoe.skatscores.game.adapter.presentation.GameViewModelFactory
import com.damhoe.skatscores.game.adapter.presentation.SkatGameViewModel
import com.damhoe.skatscores.game.domain.score.ScoreRequest
import com.damhoe.skatscores.game.domain.score.SkatOutcome.LOST
import com.damhoe.skatscores.game.domain.score.SkatOutcome.OVERBID
import com.damhoe.skatscores.game.domain.score.SkatOutcome.PASSE
import com.damhoe.skatscores.game.domain.score.SkatOutcome.WON
import com.damhoe.skatscores.game.domain.score.SkatSuit.CLUBS
import com.damhoe.skatscores.game.domain.score.SkatSuit.DIAMONDS
import com.damhoe.skatscores.game.domain.score.SkatSuit.GRAND
import com.damhoe.skatscores.game.domain.score.SkatSuit.HEARTS
import com.damhoe.skatscores.game.domain.score.SkatSuit.INVALID
import com.damhoe.skatscores.game.domain.score.SkatSuit.NULL
import com.damhoe.skatscores.game.domain.score.SkatSuit.SPADES
import com.damhoe.skatscores.shared_ui.utils.InsetsManager
import com.damhoe.skatscores.shared_ui.utils.LayoutMargins
import javax.inject.Inject

class ScoreFragment : Fragment()
{
    /**
     * Score fragment is responsible for the creation and update process of scores.
     *
     * Two view model instances are used to manage the data:
     *      (i) Score view model manages the current score data that is
     *          subject to user interaction
     *      (ii) Game view model lives at the activity level and holds the current game
     *          It is responsible for persisting the score if accepted
     */
    companion object
    {
        const val SCORE_REQUEST_KEY: String = "ScoreRequest"
    }

    private val maxSpitzenValueTo = 11f
    private val normalSpitzenValueTo = 4f

    private val scoreRequest: ScoreRequest by lazy {
        arguments?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                it.getParcelable(SCORE_REQUEST_KEY, ScoreRequest::class.java)
            } else
            {
                it.getParcelable(SCORE_REQUEST_KEY)
            }
        } ?: throw IllegalArgumentException("Score fragment is missing score request.")
    }

    @Inject
    lateinit var scoreViewModelFactory: ScoreViewModelFactory
    private val scoreViewModel: ScoreViewModel by viewModels {
        scoreViewModelFactory.also { it.scoreRequest = scoreRequest }
    }

    // Game view model lives at activity scope
    @Inject
    lateinit var gameViewModelFactory: GameViewModelFactory
    private val gameViewModel: SkatGameViewModel
            by viewModels({ requireActivity() }) { gameViewModelFactory }

    private lateinit var binding: FragmentScoreBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        (requireActivity() as GameActivity).appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    private fun addListenersForToggleButtons() = binding.apply {
        toggleGroupSoloPlayer.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
            {
                handlePlayerButtonCheck(checkedId)
            }
        }

        toggleGroupResult.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
            {
                handleResultButtonCheck(checkedId)
            }
        }

        toggleGroupSuit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
            {
                // Also clear special suits
                toggleGroupSpecialSuit.clearChecked()
                handleSuitButtonCheck(checkedId)
            }
        }

        toggleGroupSpecialSuit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked)
            {
                // Also clear special suits
                toggleGroupSuit.clearChecked()
                handleSuitButtonCheck(checkedId)
            }
        }
    }

    private fun handleResultButtonCheck(checkedId: Int)
    {
        when (checkedId)
        {
            R.id.buttonOverbid -> scoreViewModel.setResult(OVERBID)
            R.id.buttonWon -> scoreViewModel.setResult(WON)
            R.id.buttonLost -> scoreViewModel.setResult(LOST)
        }
    }

    private fun addListenersForChips() = binding.apply {
        announcementsChips.setOnCheckedStateChangeListener { _, checkedIds: List<Int> ->
            content.requestLayout()
            handleAnnouncementChips(checkedIds)
        }

        winLevelChips.setOnCheckedStateChangeListener { _, checkedIds: List<Int> ->
            content.requestLayout()
            handleWinLevelChips(checkedIds)
        }
    }

    private fun updateSpitzenButtons(spitzen: Float)
    {
        with(binding) {
            spitzenAddButton.isEnabled = spitzen < spitzenSlider.valueTo
            spitzenRemoveButton.isEnabled = spitzen > spitzenSlider.valueFrom
        }
    }

    private fun addListenersForSpitzen() = binding.apply {
        spitzenSlider.addOnChangeListener { _, value, _ ->
            spitzenLabel.text = String.format("%.0f", value)
            scoreViewModel.setSpitzen(value.toInt())
            updateSpitzenButtons(value)
        }
        spitzenRemoveButton.setOnClickListener { spitzenSlider.value -= 1 }
        spitzenAddButton.setOnClickListener { spitzenSlider.value += 1 }

        spitzenSwitch.setOnCheckedChangeListener { _, isChecked ->
            spitzenSlider.valueTo = if (isChecked)
            {
                maxSpitzenValueTo
            } else
            {
                if (spitzenSlider.value > normalSpitzenValueTo)
                {
                    spitzenSlider.value = normalSpitzenValueTo
                }
                normalSpitzenValueTo
            }
            updateSpitzenButtons(spitzenSlider.value)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_score, container, false
        )

        addListenersForToggleButtons()
        addListenersForChips()
        addListenersForSpitzen()

        binding.scoreDoneButton.setOnClickListener {
            saveScore()
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun handleAnnouncementChips(checkedIds: List<Int>)
    {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_announced_chip) &&
            !checkedIds.contains(R.id.schneider_announced_chip)
        )
        {
            binding.announcementsChips.check(R.id.schneider_announced_chip)
            return
        }

        // Set viewModel data
        scoreViewModel.setSchneiderAnnounced(checkedIds.contains(R.id.schneider_announced_chip))
        scoreViewModel.setSchwarzAnnounced(checkedIds.contains(R.id.schwarz_announced_chip))
    }

    private fun handleWinLevelChips(checkedIds: List<Int>)
    {
        // Schwarz is checked only if Schneider is also checked
        if (checkedIds.contains(R.id.schwarz_chip) &&
            !checkedIds.contains(R.id.schneider_chip)
        )
        {
            binding.winLevelChips.check(R.id.schneider_chip)
            return
        }
        scoreViewModel.setHand(checkedIds.contains(R.id.hand_chip))
        scoreViewModel.setOuvert(checkedIds.contains(R.id.ouvert_chip))
        scoreViewModel.setSchneider(checkedIds.contains(R.id.schneider_chip))
        scoreViewModel.setSchwarz(checkedIds.contains(R.id.schwarz_chip))
    }

    private fun findNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

    private fun handlePlayerButtonCheck(buttonId: Int) = with(binding) {
        scoreViewModel.apply {
            when (buttonId)
            {
                buttonPasse.id -> setPasse()
                buttonPlayer1.id -> selectPlayer(position = 0)
                buttonPlayer2.id -> selectPlayer(position = 1)
                buttonPlayer3.id -> selectPlayer(position = 2)
            }
        }
    }

    private fun handleSuitButtonCheck(buttonId: Int) = with(binding) {
        scoreViewModel.apply {
            when (buttonId)
            {
                buttonClubs.id -> setSuit(CLUBS)
                buttonSpades.id -> setSuit(SPADES)
                buttonHearts.id -> setSuit(HEARTS)
                buttonDiamonds.id -> setSuit(DIAMONDS)
                buttonGrand.id -> setSuit(GRAND)
                buttonNull.id -> setSuit(NULL)
            }
        }
    }

    private fun writePlayerNames() = binding.also {
        with(scoreViewModel.playerNames) {
            it.buttonPlayer1.text = this[0]
            it.buttonPlayer2.text = this[1]
            it.buttonPlayer3.text = this[2]
        }
    }

    private fun checkPlayer(position: Int) = binding.apply {
        when (position)
        {
            0 -> buttonPlayer1.isChecked = true
            1 -> buttonPlayer2.isChecked = true
            2 -> buttonPlayer3.isChecked = true
        }
    }

    /** @noinspection DataFlowIssue
     */
    private fun initializeUI()
    {
        writePlayerNames()

        val positionMap = scoreViewModel.playerPositions

        scoreViewModel.scoreCommand.value?.let { cmd ->
            binding.apply {

                if (cmd.isPasse)
                {
                    toggleGroupSoloPlayer.check(R.id.buttonPasse)
                    return
                }

                val playerPosition = positionMap[cmd.playerPosition]
                checkPlayer(playerPosition)

                toggleGroupResult.apply {
                    when
                    {
                        cmd.isWon -> check(R.id.buttonWon)
                        cmd.isLost -> check(R.id.buttonLost)
                        cmd.isOverbid -> check(R.id.buttonOverbid)
                    }
                }

                // Init slider
                spitzenSwitch.isChecked = cmd.spitzen > normalSpitzenValueTo
                spitzenSlider.valueTo = if (cmd.spitzen > normalSpitzenValueTo)
                {
                    maxSpitzenValueTo
                } else
                {
                    normalSpitzenValueTo
                }
                spitzenSlider.value = cmd.spitzen.toFloat()

                // Init win level chips
                handChip.isChecked = cmd.isHand
                ouvertChip.isChecked = cmd.isOuvert
                schneiderChip.isChecked = cmd.isSchneider
                schwarzChip.isChecked = cmd.isSchwarz
                schneiderAnnouncedChip.isChecked = cmd.isSchneiderAnnounced
                schwarzAnnouncedChip.isChecked = cmd.isSchwarzAnnounced
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
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

        with(scoreViewModel) {
            isSuitsEnabled
                .observe(viewLifecycleOwner) { changeSuitButtonAccessibility(it) }
            isHandEnabled
                .observe(viewLifecycleOwner) { binding.handChip.isEnabled = it }
            isSchneiderSchwarzEnabled
                .observe(viewLifecycleOwner) { changeAnnouncementsChipsAccessibility(it) }
            isOuvertEnabled
                .observe(viewLifecycleOwner) { binding.ouvertChip.isEnabled = it }
            isSpitzenEnabled
                .observe(viewLifecycleOwner) {
                    binding.spitzenSlider.isEnabled = it
                    binding.spitzenSwitch.isEnabled = it
                }

            isResultsEnabled.observe(viewLifecycleOwner) { b ->
                binding.apply {
                    listOf(buttonWon, buttonLost, buttonOverbid).forEach { it.isEnabled = b }
                }
            }

            skatResult.observe(viewLifecycleOwner) { outcome ->
                outcome?.let {
                    when (it)
                    {
                        WON -> binding.toggleGroupResult.check(R.id.buttonWon)
                        LOST -> binding.toggleGroupResult.check(R.id.buttonLost)
                        OVERBID -> binding.toggleGroupResult.check(R.id.buttonOverbid)
                        PASSE -> binding.toggleGroupResult.clearChecked()
                    }
                }
            }

            suit.observe(viewLifecycleOwner) { skatSuit ->
                skatSuit?.let {
                    when (it)
                    {
                        INVALID -> binding.apply {
                            announcementsChips.clearCheck()
                            winLevelChips.clearCheck()
                            spitzenSlider.value = spitzenSlider.valueFrom
                            toggleGroupSuit.clearChecked()
                        }

                        CLUBS -> binding.toggleGroupSuit.check(R.id.buttonClubs)
                        SPADES -> binding.toggleGroupSuit.check(R.id.buttonSpades)
                        HEARTS -> binding.toggleGroupSuit.check(R.id.buttonHearts)
                        DIAMONDS -> binding.toggleGroupSuit.check(R.id.buttonDiamonds)
                        NULL -> binding.toggleGroupSuit.check(R.id.buttonNull)
                        GRAND -> binding.toggleGroupSuit.check(R.id.buttonGrand)
                    }
                }
            }
        }

        initializeUI()
    }

    private fun changeAnnouncementsChipsAccessibility(isEnabled: Boolean) = with(binding) {
        listOf(schneiderChip, schwarzChip, schneiderAnnouncedChip, schwarzAnnouncedChip)
            .forEach { it.isEnabled = isEnabled }
    }

    private fun changeSuitButtonAccessibility(isEnabled: Boolean) = with(binding) {
        listOf(buttonClubs, buttonSpades, buttonHearts, buttonDiamonds, buttonNull, buttonGrand)
            .forEach { it.isEnabled = isEnabled }
    }

    private fun saveScore()
    {
        when (scoreRequest)
        {
            is ScoreRequest.CreateScoreRequest ->
            {
                val score = scoreViewModel.createScore()
                gameViewModel.addScore(score)
            }

            is ScoreRequest.UpdateScoreRequest ->
            {
                val score = scoreViewModel.updateScore()
                gameViewModel.updateScore(score)
            }
        }
    }
}