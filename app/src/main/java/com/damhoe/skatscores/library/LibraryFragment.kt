package com.damhoe.skatscores.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentLibraryBinding
import com.damhoe.skatscores.library.GamePreviewItemClickListener
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.shared.utils.InsetsManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class LibraryFragment :
    Fragment(R.layout.fragment_library),
    GamePreviewItemClickListener
{
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var binding: FragmentLibraryBinding
    private lateinit var gamePreviewAdapter: GamePreviewAdapter

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    )
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryBinding.bind(view)

        addMenu()

        InsetsManager.applyStatusBarInsets(binding.toolbar)
        InsetsManager.applyNavigationBarInsets(binding.content)

        // Setup recycler view
        gamePreviewAdapter = GamePreviewAdapter(this)
        binding.gamesRv.adapter = gamePreviewAdapter
        binding.gamesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.gamesRv.addItemDecoration(GamePreviewAdapter.ItemDecoration(16))
        setupShadow()

        binding.addButton.setOnClickListener { navigateToGameSetup() }
        binding.statisticsButton.setOnClickListener { navigateToPlayers() }

        // Add live data observers
        viewModel.games.observe(viewLifecycleOwner) { previews ->
            val showNoGamesInfo = previews.isEmpty()
            binding.gamesRv.visibility = if (showNoGamesInfo) View.GONE else View.VISIBLE
            gamePreviewAdapter.submitList(previews)
            binding.gamesRv.invalidate()
        }
    }

    private fun setupShadow()
    {
        var isHeaderElevated = false

        binding.gamesRv.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                val scrollOffset = recyclerView.computeVerticalScrollOffset()
                val shouldElevate = scrollOffset > 0

                if (shouldElevate != isHeaderElevated)
                {
                    isHeaderElevated = shouldElevate

                    // Animate the shadow alpha
                    binding.headerShadow.animate()
                        .alpha(if (shouldElevate) 0.2f else 0f)
                        .setDuration(250) // Smooth duration in ms
                        .start()

                    // Optional: Animate the actual elevation for a real Material 3 feel
//                    binding.headerContainer.animate()
//                        .z(if (shouldElevate) 8f else 0f)
//                        .setDuration(250)
//                        .start()
                }
            }
        })
    }

    private fun addMenu()
    {
        binding.toolbar.addMenuProvider(object : MenuProvider
        {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
            {
                menu.clear()
                menuInflater.inflate(R.menu.options_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean
            {
                val itemId = item.itemId
                if (itemId == R.id.menu_settings)
                {
                    showAppSettingsDialog()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showAppSettingsDialog()
    {
        findNavController().navigate(
            LibraryFragmentDirections.actionHomeToAppSettings()
        )
    }

    override fun notifyDelete(skatGamePreview: SkatGamePreview)
    {
        val result = viewModel.deleteGame(skatGamePreview.gameId)
    }

    private fun navigateToGameSetup() = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryToGameSetup()
    )

    private fun navigateToGame(gameId: UUID) = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryFragmentToSkatGameNavGraph(gameId.toString())
    )

    private fun navigateToPlayers() = findNavController().navigate(
        LibraryFragmentDirections.actionLibraryToPlayersFragment()
    )

    override fun notifySelect(
        skatGamePreview: SkatGamePreview
    )
    {
        navigateToGame(skatGamePreview.gameId)
    }
}