package com.damhoe.skatscores.game.game_home.adapter.`in`.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.base.Result
import com.damhoe.skatscores.databinding.DialogGameSettingsBinding
import com.damhoe.skatscores.databinding.FragmentGameBinding
import com.damhoe.skatscores.game.game_home.GameActivity
import com.damhoe.skatscores.game.game_home.application.PlayerSelectionValidator
import com.damhoe.skatscores.game.game_home.domain.SkatGame
import com.damhoe.skatscores.game.game_home.domain.SkatSettings
import com.damhoe.skatscores.game.game_setup.domain.SkatGameCommand
import com.damhoe.skatscores.game.score.adapter.`in`.ui.ScoreFragment
import com.damhoe.skatscores.game.score.domain.ScoreRequest
import com.damhoe.skatscores.game.score.domain.SkatScore
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.shared_ui.utils.InsetsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.util.Arrays
import java.util.Locale
import java.util.stream.Collectors
import javax.inject.Inject

class GameFragment : Fragment(), IScoreActionListener
{
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory
    private val viewModel: SkatGameViewModel by viewModels(
            { requireActivity() }) { viewModelFactory }

    @Inject
    lateinit var selectPlayerVMFactory: SelectPlayerVMFactory;
    private val selectPlayerViewModel: SelectPlayerViewModel by viewModels(
            { requireActivity() }) { selectPlayerVMFactory }

    private lateinit var binding: FragmentGameBinding
    private val playerValidator = PlayerSelectionValidator()
    private lateinit var scoreAdapter: SkatScoreAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Get arguments and initialize the game
        val command: SkatGameCommand? =
                GameFragmentArgs.fromBundle(requireArguments())
                    .getGameCommand()
        val gameId: Long =
                GameFragmentArgs.fromBundle(requireArguments())
                    .getGameId()

        if (gameId != -1L)
        {
            viewModel.initialize(gameId)
        }
        else {
            viewModel.initialize(command)
        }
    }

    override fun onAttach(context: Context)
    {
        (requireActivity() as GameActivity).appComponent.inject(this)
        super.onAttach(context)
    }

    private fun findNavController() = findNavController(
            requireActivity(),
            R.id.nav_host_fragment)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_game,
                container,
                false)
        setUpEditScoreButton()
        setUpEditPlayersButton()
        setUpRecyclerView()
        return binding.root
    }

    private fun setUpEditPlayersButton()
    {
        binding.titleView.buttonEdit.setOnClickListener { showEditPlayerDialog() }
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?)
    {
        super.onViewCreated(
                view,
                savedInstanceState)

        // Set insets
        InsetsManager.applySystemBarInsets(binding.content)
        InsetsManager.applyNavigationBarInsets(binding.editScoreButton)
        InsetsManager.applyNavigationBarInsets(binding.bottomAppBar)

        setupNavigation()
        setupObservers()
        addMenu()
    }

    private fun setupObservers()
    {
        val titleObserver = Observer { title: String -> this.setTitle(title) }
        viewModel.title.observe(
                viewLifecycleOwner,
                titleObserver)

        val gameObserver = Observer { game: SkatGame -> this.updatePoints(game) }
        viewModel.game.observe(
                viewLifecycleOwner,
                gameObserver)

        viewModel.gameRunStateInfo.observe(
                viewLifecycleOwner) {
            val text = if (it.isFinished)
            {
                "Finished"
            }
            else
            {
                String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        it.currentRound,
                        it.roundsCount
                )
            }
            binding.currentRoundText.text = text
        }

        viewModel.dealerPosition.observe(viewLifecycleOwner) {
            binding.titleView.apply {
                when (it)
                {
                    0 ->
                    {
                        indicator1.alpha = 1.0f
                        indicator2.alpha = 0.0f
                        indicator3.alpha = 0.0f
                    }
                    1 ->
                    {
                        indicator1.alpha = 0.0f
                        indicator2.alpha = 1.0f
                        indicator3.alpha = 0.0f
                    }
                    2 ->
                    {
                        indicator1.alpha = 0.0f
                        indicator2.alpha = 0.0f
                        indicator3.alpha = 1.0f
                    }
                }
            }
        }

        viewModel.players.observe(
                viewLifecycleOwner) {
            binding.titleView.apply {
                name1Text.text = it[0].name
                name2Text.text = it[1].name
                name3Text.text = it[2].name
            }
        }

        viewModel.settings.observe(
                viewLifecycleOwner
        ) { skatSettings: SkatSettings ->
            val visibilityWinLossBonus =
                    if (skatSettings.isTournamentScoring)
                        View.VISIBLE
                    else
                        View.GONE
            binding.bottomSumView.apply {
                lostContainer.visibility = visibilityWinLossBonus
                soloContainer.visibility = visibilityWinLossBonus
                divider.visibility = visibilityWinLossBonus
            }

            val game = viewModel.game.value
            if (game != null) {
                updatePoints(
                        game)
            }
        }

        viewModel.totalPoints.observe(viewLifecycleOwner) { this.displayTotalPoints(it) }

        viewModel.winBonus.observe(viewLifecycleOwner) {
            binding.bottomSumView.apply{
                solo1Text.text = it[0].toString()
                solo2Text.text = it[1].toString()
                solo3Text.text = it[2].toString()
            }
        }
        viewModel.lossOfOthersBonus.observe(
                viewLifecycleOwner) {
            binding.bottomSumView.apply{
                lost1Text.text = it[0].toString()
                lost2Text.text = it[1].toString()
                lost3Text.text = it[2].toString()
            }
        }
    }

    private fun displayTotalPoints(
            totalPoints: IntArray)
    {
        binding.bottomSumView.apply {
            points1Text.text = totalPoints[0].toString()
            points2Text.text = totalPoints[1].toString()
            points3Text.text = totalPoints[2].toString()
        }
    }

    private fun addMenu()
    {
        binding.bottomAppBar.addMenuProvider(
                object : MenuProvider
                {
                    override fun onCreateMenu(
                            menu: Menu,
                            menuInflater: MenuInflater)
                    {
                        menu.clear()
                        menuInflater.inflate(
                                R.menu.game_menu,
                                menu)
                    }

                    override fun onMenuItemSelected(
                            item: MenuItem): Boolean
                    {
                        val itemId = item.itemId
                        if (itemId == R.id.game_edit)
                        {
                            showGameSettingsDialog()
                            return true
                        }
                        else if (itemId == R.id.game_show_chart)
                        {
                            navigateToGameChart()
                        }
                        return true
                    }
                },
                viewLifecycleOwner,
                Lifecycle.State.RESUMED)
    }

    private fun navigateToGameChart()
    {
        findNavController().navigate(R.id.action_game_to_graph)
    }

    private fun showGameSettingsDialog()
    {
        val dialogBinding: DialogGameSettingsBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.dialog_game_settings,
                null,
                false)

        dialogBinding.listNameEditText.setText(
                viewModel.title.value)

        val players = viewModel.players.value
        if (players != null &&
                players.size == 3)
        {
            dialogBinding.buttonPlayer1.text = players[0].name
            dialogBinding.buttonPlayer2.text = players[1].name
            dialogBinding.buttonPlayer3.text = players[2].name
        }

        val skatGame = viewModel.game.value ?: return

        // Create a map for (position, buttonId)
        val startDealerMap = SparseIntArray()
        startDealerMap.put(
                0,
                R.id.buttonPlayer1)
        startDealerMap.put(
                1,
                R.id.buttonPlayer2)
        startDealerMap.put(
                2,
                R.id.buttonPlayer3)

        dialogBinding.toggleGroupStartDealer.check(

                startDealerMap[
                        skatGame.startDealerPosition,
                        R.id.buttonPlayer1])

        val settings: SkatSettings = skatGame.settings

        dialogBinding.numberOfRoundsText.text = settings.numberOfRounds.toString()
        dialogBinding.roundsSlider.value = settings.numberOfRounds.toFloat()

        dialogBinding.scoringSettingsRg.check(if (settings.isTournamentScoring()) R.id.tournament_scoring_rb else R.id.simple_scoring_rb)

        val dialog: AlertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.message_scoring_dialog)
            .setView(dialogBinding.root)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.background_dialog_fragment,
                    requireActivity().theme))
            .setPositiveButton(
                    getString(R.string.dialog_title_button_save)) {
                dialogInterface: DialogInterface, _: Int ->
                val editable = dialogBinding.listNameEditText.text
                val title = editable.toString()

                settings.numberOfRounds = dialogBinding.roundsSlider.value.toInt()
                settings.isTournamentScoring = dialogBinding.tournamentScoringRb.isChecked

                val checkedButton = dialogBinding.toggleGroupStartDealer.checkedButtonId
                val startDealerPosition = startDealerMap
                    .keyAt(startDealerMap.indexOfValue(checkedButton))

                skatGame.startDealerPosition = startDealerPosition
                skatGame.title = title
                skatGame.settings = settings
                viewModel.updateGame(skatGame)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                    getString(R.string.dialog_title_button_cancel),
                    (DialogInterface.OnClickListener {
                        d: DialogInterface, _: Int -> d.cancel() }))
            .create()

        // Add listeners
        dialogBinding.listNameEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore.
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore.
            }

            override fun afterTextChanged(s: Editable)
            {
                val counterMaxLength = dialogBinding.listNameTextInput.counterMaxLength
                val length: Int = s.length
                if (length > counterMaxLength)
                {
                    s.replace(
                            counterMaxLength,
                            length,
                            "")
                }

                val title: String =
                        s.toString()
                            .trim { it <= ' ' }

                // Set error if invalid title
                dialogBinding.listNameTextInput.error =
                        if (title.isEmpty()) getString(R.string.error_valid_title_required) else null

                // Disable positive button if error exists
                val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                buttonPositive.isEnabled = dialogBinding.listNameTextInput.error == null
            }
        })

        dialogBinding.roundsSlider.addOnChangeListener(
                Slider.OnChangeListener {
                    _: Slider?, value: Float, _: Boolean ->
            dialogBinding.numberOfRoundsText.text = value.toInt()
                .toString()
            val currentRound = viewModel.gameRunStateInfo.value!!.currentRound

            // Disable positive button if error exists
            val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            buttonPositive.isEnabled = value.toInt() >= currentRound - 1
        })

        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePoints(game: SkatGame)
    {
        // Set adapter items
        val scores: List<SkatScore> = game.scores
        if (scoreAdapter.scores !== scores)
        {
            scoreAdapter.scores = scores
        }

        val playerPoints = ArrayList<Int>()
        playerPoints.add(0) //game.getIndividualPoints();
        playerPoints.add(0) //game.getIndividualPoints();
        playerPoints.add(0) //game.getIndividualPoints();

        // Bottom sum
        binding.bottomSumView.points1Text.text =
                playerPoints[0]
                    .toString()
        binding.bottomSumView.points2Text.text =
                playerPoints[1]
                    .toString()
        binding.bottomSumView.points3Text.text =
                playerPoints[2]
                    .toString()
    }

    override fun notifyDelete()
    {
        val deleteResult: Result<SkatScore> = viewModel.removeLastScore()
        if (deleteResult.isFailure())
        {
            Snackbar.make(
                    requireView(),
                    deleteResult.message,
                    Snackbar.LENGTH_SHORT)
                .setAction(
                        "GOT IT",
                        View.OnClickListener { view: View? -> onDestroy() })
                .show()
            return
        }
        val position = scoreAdapter.getPosition(deleteResult.value.id)
        scoreAdapter.notifyItemRemoved(position)
    }

    override fun notifyDetails(skatScore: SkatScore)
    {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_score_details))
            .setMessage(
                    SkatScore.TextMaker(requireContext())
                        .setupWithSkatScore(skatScore)
                        .make()
            )
            .setPositiveButton(
                    getString(R.string.dialog_title_button_got_it),
                    (DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }))
            .create()
            .show()
    }

    override fun notifyEdit(
            skatScore: SkatScore)
    {
        val players = viewModel!!.players.value
                ?: throw RuntimeException("Players are null when score request is created.")

        val names: MutableList<String> = ArrayList()
        names.add(players[0].name)
        names.add(players[1].name)
        names.add(players[2].name)

        val positions: MutableList<Int> = ArrayList()
        positions.add(0)
        positions.add(1)
        positions.add(2)

        val scoreRequest: ScoreRequest.UpdateScoreRequest =
                ScoreRequest.UpdateScoreRequest(
                        skatScore.id,
                        names,
                        positions)
        val bundle = Bundle()
        bundle.putParcelable(
                ScoreFragment.Companion.SCORE_REQUEST_KEY,
                scoreRequest)
        findNavController().navigate(
                R.id.action_game_to_score,
                bundle)
    }

    private fun setupNavigation()
    {
        // Back navigation
        binding.returnButton.setOnClickListener { view: View? -> requireActivity().finish() }

        requireActivity().onBackPressedDispatcher
            .addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true)
                    {
                        override fun handleOnBackPressed()
                        {
                            requireActivity().finish()
                        }
                    })
    }

    @SuppressLint("InflateParams")
    private fun showEditPlayerDialog()
    {
        // Get current players and registered players
        val currentPlayers = viewModel.players.value!!
        val allPlayers = selectPlayerViewModel.allPlayers

        val contentView: View =
                layoutInflater.inflate(
                        R.layout.dialog_select_players,
                        null)
        val input1: TextInputLayout = contentView.findViewById(
                R.id.player1_input)
        val input2: TextInputLayout = contentView.findViewById(
                R.id.player2_input)
        val input3: TextInputLayout = contentView.findViewById(
                R.id.player3_input)
        val editPlayer1: MaterialAutoCompleteTextView =
                contentView.findViewById(
                        R.id.player1_edit_text)
        val editPlayer2: MaterialAutoCompleteTextView =
                contentView.findViewById(
                        R.id.player2_edit_text)
        val editPlayer3: MaterialAutoCompleteTextView =
                contentView.findViewById(
                        R.id.player3_edit_text)

        // Set current player names
        editPlayer1.setText(currentPlayers[0].name)
        editPlayer1.setSelection(editPlayer1.getText().length)
        editPlayer2.setText(currentPlayers[1].name)
        editPlayer2.setSelection(editPlayer2.getText().length)
        editPlayer3.setText(currentPlayers[2].name)
        editPlayer3.setSelection(editPlayer3.getText().length)

        val adapter: ArrayAdapter<Player> = ArrayAdapter<Player>(
                requireContext(),
                R.layout.item_popup_list,
                allPlayers)
        editPlayer1.setAdapter<ArrayAdapter<Player>>(adapter)
        editPlayer2.setAdapter<ArrayAdapter<Player>>(adapter)
        editPlayer3.setAdapter<ArrayAdapter<Player>>(adapter)

        val dialog: AlertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(contentView)
            .setTitle(getString(R.string.dialog_title_edit_players))
            .setNegativeButton(
                    getString(R.string.dialog_title_button_cancel)) {
                d: DialogInterface, _: Int -> d.cancel() }
            .setPositiveButton(
                    getString(R.string.dialog_title_button_save)
            ) { d: DialogInterface, _: Int ->
                val name1: String =
                        editPlayer1.getText()
                            .toString()
                            .trim { it <= ' ' }
                val name2: String =
                        editPlayer2.getText()
                            .toString()
                            .trim { it <= ' ' }
                val name3: String =
                        editPlayer3.getText()
                            .toString()
                            .trim { it <= ' ' }

                // Map names to players
                val player1 = mapToPlayer(name1)
                val player2 = mapToPlayer(name2)
                val player3 = mapToPlayer(name3)

                val newPlayers =
                        Arrays.asList(
                                player1,
                                player2,
                                player3)
                viewModel.updatePlayers(newPlayers)
                d.dismiss()
            }
            .create()

        // Add listeners
        addTextChangeListener(
                editPlayer1,
                input1,
                dialog,
                0)
        addTextChangeListener(
                editPlayer2,
                input2,
                dialog,
                1)
        addTextChangeListener(
                editPlayer3,
                input3,
                dialog,
                2)

        playerValidator.initialize(
                allPlayers,
                currentPlayers.stream()
                    .map { obj: Player -> obj.name }
                    .collect(Collectors.toList())
        )

        dialog.show()
    }

    private fun mapToPlayer(name: String): Player
    {
        val playerResult = selectPlayerViewModel.findOrCreatePlayer(name)
        if (playerResult.isFailure)
        {
            return Player.createDummy(0)
        }
        return playerResult.value
    }

    private fun addTextChangeListener(
            textView: AutoCompleteTextView,
            inputLayout: TextInputLayout,
            dialog: AlertDialog,
            position: Int
    )
    {
        textView.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore.
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
            {
                // Ignore.
            }

            override fun afterTextChanged(s: Editable)
            {
                // Reset error
                inputLayout.error = null
                inputLayout.helperText = null

                // Check if the current length exceeds the maximum length
                val maxLength: Int = inputLayout.getCounterMaxLength()
                val currentLength: Int = s.length
                if (currentLength > maxLength)
                {
                    // Trim the text to the maximum length
                    s.replace(
                            maxLength,
                            currentLength,
                            "")
                }

                val name: String =
                        s.toString()
                            .trim { it <= ' ' }
                playerValidator.select(
                        position,
                        name)
                val messages: List<Pair<PlayerSelectionValidator.MessageType, String>> =
                        playerValidator.validate()
                val type: PlayerSelectionValidator.MessageType = messages[position].first
                val message = messages[position].second

                if (type == PlayerSelectionValidator.MessageType.Error)
                {
                    inputLayout.setError(message)
                }
                else if (message != null)
                {
                    inputLayout.setHelperText(message)
                }

                val buttonSave = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                buttonSave.isEnabled = messages
                    .stream()
                    .noneMatch { it.first == PlayerSelectionValidator.MessageType.Error }
            }
        })
    }

    private fun setUpRecyclerView()
    {
        binding.scoresRv.layoutManager = LinearLayoutManager(
                requireContext())
        scoreAdapter = SkatScoreAdapter(
                this)
        binding.scoresRv.adapter = scoreAdapter
        binding.scoresRv.addItemDecoration(
                DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL))
    }

    private fun setTitle(
            title: String)
    {
        binding.gameTitle.text = title
    }

    private fun setUpEditScoreButton()
    {
        binding.editScoreButton.setOnClickListener { view: View? ->
            Log.d(
                    "Event",
                    "Edit score button clicked.")
            val players = viewModel!!.players.value!!
            val request: ScoreRequest.CreateScoreRequest = getCreateScoreRequest(players)
            val bundle = Bundle()
            bundle.putParcelable(
                    ScoreFragment.Companion.SCORE_REQUEST_KEY,
                    request)
            findNavController().navigate(
                    R.id.action_game_to_score,
                    bundle)
        }
    }

    private fun getCreateScoreRequest(
            players: List<Player>?): ScoreRequest.CreateScoreRequest
    {
        val game = viewModel!!.game.value
        val gameId = game?.id ?: -1L

        val names: MutableList<String> = ArrayList()
        names.add(players!![0].name)
        names.add(players[1].name)
        names.add(players[2].name)

        val positions: MutableList<Int> = ArrayList()
        positions.add(0)
        positions.add(1)
        positions.add(2)

        return ScoreRequest.CreateScoreRequest(
                gameId,
                names,
                positions)
    }
}