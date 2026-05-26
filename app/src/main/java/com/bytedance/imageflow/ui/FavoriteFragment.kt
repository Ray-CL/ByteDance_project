package com.bytedance.imageflow.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bytedance.imageflow.DetailActivity
import com.bytedance.imageflow.R
import com.bytedance.imageflow.data.FavoriteStore
import com.bytedance.imageflow.data.ImageRepository
import com.bytedance.imageflow.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteFragment : Fragment(R.layout.fragment_image_list) {
    private lateinit var repository: ImageRepository
    private lateinit var favoriteStore: FavoriteStore
    private lateinit var adapter: ImageAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val items = mutableListOf<ImageItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ImageRepository.create(requireContext())
        favoriteStore = FavoriteStore(requireContext())
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        adapter = ImageAdapter(
            repository,
            favoriteStore,
            viewLifecycleOwner.lifecycleScope,
            onClick = { item -> openDetail(item) },
            onFavoriteChanged = { item, isFavorite ->
                if (!isFavorite) {
                    items.removeAll { it.id == item.id }
                    adapter.submitList(items.toList())
                }
                val message = if (isFavorite) {
                    getString(R.string.toast_favorite_added)
                } else {
                    getString(R.string.toast_favorite_removed)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.isFocusableInTouchMode = true
        recyclerView.requestFocus()

        swipeRefresh.isEnabled = false
        progressBar.visibility = View.GONE
        refreshFavorites()
    }

    override fun onResume() {
        super.onResume()
        refreshFavorites()
    }

    fun refreshFavorites() {
        items.clear()
        items.addAll(favoriteStore.load())
        adapter.submitList(items.toList())
    }

    fun onClearCacheRequested() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearCache()
            }
            Toast.makeText(requireContext(), getString(R.string.toast_cache_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    fun onTabReselected() {
        recyclerView.scrollToPosition(0)
        refreshFavorites()
    }

    fun onMouseWheelScroll(deltaY: Int): Boolean {
        recyclerView.scrollBy(0, deltaY)
        return true
    }

    private fun openDetail(item: ImageItem) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_ITEM, item)
        startActivity(intent)
    }
}
