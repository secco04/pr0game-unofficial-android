package de.lobianco.pr0gameunofficial

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlanetPagerAdapter(
    activity: FragmentActivity,
    private val planets: List<Planet>
) : FragmentStateAdapter(activity) {

    private val fragments = mutableMapOf<Int, PlanetWebViewFragment>()

    override fun getItemCount(): Int = planets.size

    override fun createFragment(position: Int): Fragment {
        val fragment = PlanetWebViewFragment.newInstance(planets[position])
        fragments[position] = fragment
        return fragment
    }

    fun getFragmentAtPosition(position: Int): PlanetWebViewFragment? {
        return fragments[position]
    }
}