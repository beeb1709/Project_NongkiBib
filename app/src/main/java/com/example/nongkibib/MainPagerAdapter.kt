package com.example.nongkibib

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> AcaraFragment()
            2 -> MapFragment()
            3 -> InboxFragment()
            4 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}
