package com.damhoe.skatscores.app.about

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentAboutBinding

class AboutFragment : Fragment(R.layout.fragment_about)
{
    private lateinit var binding: FragmentAboutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAboutBinding.bind(view)

        applyInsets()

        // The header is this screen's own, not a toolbar wired to the navigation graph: a
        // NavController-driven toolbar adopts the next destination's label while the pop
        // animation is still running, which flashed that label over this screen.
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
    }

    private fun applyInsets()
    {
        val contentBottomPadding = binding.content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            binding.content.setPadding(
                binding.content.paddingLeft,
                binding.content.paddingTop,
                binding.content.paddingRight,
                contentBottomPadding + bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun findNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
}
