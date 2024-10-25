package com.damhoe.skatscores.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentHomeBinding
import com.damhoe.skatscores.library.LibraryFragment
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayersFragment
import com.damhoe.skatscores.shared.shared_ui.utils.InsetsManager
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment()
{
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeFragmentsAdapter: HomeFragmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View
    {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false)

        addMenu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        InsetsManager.applyStatusBarInsets(binding.toolbar)
        InsetsManager.applyNavigationBarInsets(binding.viewPager)

        homeFragmentsAdapter = HomeFragmentsAdapter(this)
        binding.viewPager.adapter = homeFragmentsAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            when (pos)
            {
                0 ->
                {
                    tab.text = "Listen"
                    tab.icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_library_24dp, null)
                }

                1 ->
                {
                    tab.text = "Spieler"
                    tab.icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_people_24dp, null)
                }
            }
        }.attach()
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
            HomeFragmentDirections.actionHomeToAppSettings()
        )
    }

    private fun findNavController() = Navigation.findNavController(
        requireActivity(),
        R.id.nav_host_fragment);


    class HomeFragmentsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment)
    {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment
        {
            return when (position)
            {
                0 -> LibraryFragment.newInstance()
                1 -> PlayersFragment()
                else -> throw NotImplementedError()
            }
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}