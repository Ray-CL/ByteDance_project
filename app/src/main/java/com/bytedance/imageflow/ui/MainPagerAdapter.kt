package com.bytedance.imageflow.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    val recommendFragment = RecommendFragment()
    val historyFragment = HistoryFragment()
    val favoriteFragment = FavoriteFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> recommendFragment
            1 -> historyFragment
            else -> favoriteFragment
        }
    }
}
