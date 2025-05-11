package com.damhoe.skatscores.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.app.HomeFragmentDirections
import com.damhoe.skatscores.databinding.FragmentLibraryBinding
import com.damhoe.skatscores.game.adapter.presentation.shared.GamePreviewAdapter
import com.damhoe.skatscores.game.adapter.presentation.shared.GamePreviewItemClickListener
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : Fragment(),
    GamePreviewItemClickListener
{
    @Inject
    lateinit var factory: LibraryViewModelFactory
    private val viewModel: LibraryViewModel by viewModels { factory }
    private lateinit var binding: FragmentLibraryBinding
    private lateinit var gamePreviewAdapter: GamePreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_library, container, false
        )

        // Setup recycler view
        gamePreviewAdapter = GamePreviewAdapter(this)
        binding.gamesRv.adapter = gamePreviewAdapter
        binding.gamesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.gamesRv.addItemDecoration(GamePreviewAdapter.ItemDecoration(16))
        gamePreviewAdapter.setGamePreviews(
            viewModel.games.value
        )
        binding.addButton.setOnClickListener { navigateToGameSetup() }
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    )
    {
        super.onViewCreated(view, savedInstanceState)

        // Add live data observers
        viewModel.games.observe(viewLifecycleOwner) { previews ->
            val showNoGamesInfo = previews.isEmpty()
            binding.gamesRv.visibility = if (showNoGamesInfo) View.GONE else View.VISIBLE
            gamePreviewAdapter.setGamePreviews(previews)
            binding.gamesRv.invalidate()
        }
    }

    override fun notifyDelete(skatGamePreview: SkatGamePreview?)
    {
        val result = viewModel.deleteGame(
            skatGamePreview!!.gameId)

        if (result.isFailure)
        {
            Log.d(
                "Unexpected behavior",
                result.message)
        }
    }

    private fun findNavController() = findNavController(
        requireActivity(),
        R.id.nav_host_fragment)

    private fun navigateToGameSetup() = findNavController().navigate(
        (HomeFragmentDirections.actionHomeToGame() as NavDirections))

    private fun navigateToGame(gameId: Long) = findNavController().navigate(
        (HomeFragmentDirections.actionHomeToGame().setGameId(gameId) as NavDirections))

    override fun notifySelect(
        skatGamePreview: SkatGamePreview
    )
    {
        navigateToGame(
            skatGamePreview.gameId)
    }

    companion object
    {
        @JvmStatic
        fun newInstance(): LibraryFragment
        {
            return LibraryFragment()
        }
    }
}