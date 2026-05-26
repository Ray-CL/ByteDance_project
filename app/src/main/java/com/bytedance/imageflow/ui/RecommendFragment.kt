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
import com.bytedance.imageflow.data.HistoryStore
import com.bytedance.imageflow.data.ImageRepository
import com.bytedance.imageflow.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendFragment : Fragment(R.layout.fragment_image_list) {
    private lateinit var repository: ImageRepository
    private lateinit var historyStore: HistoryStore
    private lateinit var favoriteStore: FavoriteStore
    private lateinit var adapter: ImageAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val items = mutableListOf<ImageItem>()
    private var page = 1
    private val limit = 20
    private var isLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ImageRepository.create(requireContext())
        historyStore = HistoryStore(requireContext())
        favoriteStore = FavoriteStore(requireContext())
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        adapter = ImageAdapter(
            repository,
            favoriteStore,
            viewLifecycleOwner.lifecycleScope,
            onClick = { item -> openDetail(item) },
            onFavoriteChanged = { _, isFavorite ->
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
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && lastVisible >= items.size - 4) {
                    loadPage(reset = false)
                }
            }
        })

        swipeRefresh.setOnRefreshListener {
            loadPage(reset = true)
        }

        if (items.isEmpty()) {
            progressBar.visibility = View.VISIBLE
            loadPage(reset = true)
        } else {
            adapter.submitList(items.toList())
        }
    }

    fun onClearCacheRequested() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearCache()
            }
            items.clear()
            adapter.submitList(emptyList())
            page = 1
            isLoading = false
            loadPage(reset = true)
            Toast.makeText(requireContext(), getString(R.string.toast_cache_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    fun onTabReselected() {
        recyclerView.scrollToPosition(0)
        loadPage(reset = true)
    }

    fun refreshFavorites() {
        adapter.notifyDataSetChanged()
    }

    fun onMouseWheelScroll(deltaY: Int): Boolean {
        recyclerView.scrollBy(0, deltaY)
        return true
    }

    private fun loadPage(reset: Boolean) {
        if (isLoading) return
        isLoading = true
        if (reset) {
            page = 1
            progressBar.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (reset) {
                swipeRefresh.isRefreshing = true
            }
            try {
                val newItems = repository.fetchImages(page, limit)
                if (reset) {
                    items.clear()
                }
                items.addAll(newItems)
                adapter.submitList(items.toList())
                page += 1
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), ex.message ?: "Load failed", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                isLoading = false
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun openDetail(item: ImageItem) {
        historyStore.add(item)
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_ITEM, item)
        startActivity(intent)
    }
}
