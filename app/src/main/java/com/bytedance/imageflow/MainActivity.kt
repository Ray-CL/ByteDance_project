package com.bytedance.imageflow

import android.os.Bundle
import android.view.InputDevice
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.imageflow.ui.MainPagerAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        setSupportActionBar(toolbar)

        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.tab_recommend
                    1 -> R.string.tab_history
                    else -> R.string.tab_favorite
                }
            )
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> pagerAdapter.recommendFragment.refreshFavorites()
                    1 -> pagerAdapter.historyFragment.refreshHistory()
                    2 -> pagerAdapter.favoriteFragment.refreshFavorites()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // No-op.
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> pagerAdapter.recommendFragment.onTabReselected()
                    1 -> pagerAdapter.historyFragment.onTabReselected()
                    else -> pagerAdapter.favoriteFragment.onTabReselected()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_cache -> {
                when (viewPager.currentItem) {
                    0 -> pagerAdapter.recommendFragment.onClearCacheRequested()
                    1 -> pagerAdapter.historyFragment.onClearCacheRequested()
                    else -> pagerAdapter.favoriteFragment.onClearCacheRequested()
                }
                true
            }
            R.id.action_clear_history -> {
                pagerAdapter.historyFragment.onClearHistoryRequested()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
            val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (vScroll != 0f) {
                val scrollFactor = 48f * resources.displayMetrics.density
                val delta = (-vScroll * scrollFactor).toInt()
                val handled = when (viewPager.currentItem) {
                    0 -> pagerAdapter.recommendFragment.onMouseWheelScroll(delta)
                    1 -> pagerAdapter.historyFragment.onMouseWheelScroll(delta)
                    else -> pagerAdapter.favoriteFragment.onMouseWheelScroll(delta)
                }
                if (handled) return true
            }
        }
        return super.dispatchGenericMotionEvent(event)
    }
}
