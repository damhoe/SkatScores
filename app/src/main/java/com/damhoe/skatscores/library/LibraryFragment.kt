package com.damhoe.skatscores.library

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.MainActivity
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentLibraryBinding
import com.damhoe.skatscores.game.game_home.adapter.`in`.ui.shared.GamePreviewAdapter
import com.damhoe.skatscores.game.game_home.adapter.`in`.ui.shared.GamePreviewItemClickListener
import com.damhoe.skatscores.game.game_home.domain.SkatGamePreview
import com.damhoe.skatscores.shared_ui.utils.InsetsManager
import com.damhoe.skatscores.shared_ui.utils.LayoutMargins
import javax.inject.Inject

class LibraryFragment: Fragment(),
    GamePreviewItemClickListener {

    @Inject
    lateinit var factory: LibraryViewModelFactory

    private val viewModel: LibraryViewModel by viewModels { factory }

    private lateinit var binding: FragmentLibraryBinding
    private lateinit var gamePreviewAdapter: GamePreviewAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_library, container, false)

        setupNewGameButton()

        // Setup recycler view
        gamePreviewAdapter =
            GamePreviewAdapter(
                this
            )
        binding.gamesRv.adapter = gamePreviewAdapter
        binding.gamesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.gamesRv.addItemDecoration(GamePreviewAdapter.ItemDecoration(16))
        gamePreviewAdapter.setGamePreviews(viewModel.games.value)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set insets to account for status and navigation bars
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView)

        // Load default button margins for resource values
        val bottomMargin =
            resources.getDimension(R.dimen.fab_above_bottom_nav_margin_bottom).toInt()
        val rightMargin = resources.getDimension(R.dimen.fab_margin_right).toInt()
        val margins = LayoutMargins(0, 0, rightMargin, bottomMargin)
        InsetsManager.applyNavigationBarInsets(binding.addButton, margins)

        // Add live data observers
        viewModel.games.observe(viewLifecycleOwner) { previews ->
            val showNoGamesInfo = previews.isEmpty()
            binding.gamesRv.visibility = if (showNoGamesInfo) View.GONE else View.VISIBLE
            gamePreviewAdapter.setGamePreviews(previews)
            binding.gamesRv.invalidate()
        }

        addMenu()
    }

    private fun findNavController(): NavController {
        return findNavController(requireActivity(), R.id.nav_host_fragment)
    }

    private fun setupNewGameButton() {
        binding.addButton.setOnClickListener { navigateToGameSetup() }
    }

    private fun navigateToGameSetup() = findNavController().navigate(
        (LibraryFragmentDirections.actionLibraryToGameActivity() as NavDirections)
    )

    private fun navigateToGame(gameId: Long) = findNavController().navigate(
        (LibraryFragmentDirections.actionLibraryToGameActivity().setGameId(gameId) as NavDirections)
    )

    private fun showAppSettingsDialog() {
        findNavController().navigate(LibraryFragmentDirections.actionLibraryToAppSettingsDialogFragment())
        // AppSettingsDialogFragment().show(parentFragmentManager, "APP_SETTINGS_DIALOG")
    }

    private fun addMenu() {
        binding.toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.options_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                val itemId = item.itemId
                if (itemId == R.id.menu_settings) {
                    showAppSettingsDialog()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun notifyDelete(skatGamePreview: SkatGamePreview?) {
        val result = viewModel.deleteGame(
            skatGamePreview!!.gameId
        )
        if (result.isFailure) {
            Log.d("Unexpected behavior", result.message)
        }
    }

    override fun notifySelect(skatGamePreview: SkatGamePreview) {
        navigateToGame(skatGamePreview.gameId)
    }
}