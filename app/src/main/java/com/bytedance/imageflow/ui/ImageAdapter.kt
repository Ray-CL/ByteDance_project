package com.bytedance.imageflow.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.imageflow.R
import com.bytedance.imageflow.data.FavoriteStore
import com.bytedance.imageflow.data.ImageRepository
import com.bytedance.imageflow.model.ImageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageAdapter(
    private val repository: ImageRepository,
    private val favoriteStore: FavoriteStore,
    private val scope: CoroutineScope,
    private val onClick: (ImageItem) -> Unit,
    private val onFavoriteChanged: (ImageItem, Boolean) -> Unit
) : ListAdapter<ImageItem, ImageAdapter.ImageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        private val descView: TextView = itemView.findViewById(R.id.itemDesc)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.itemFavorite)

        fun bind(item: ImageItem) {
            val context = itemView.context
            imageView.setImageResource(R.drawable.ic_placeholder)
            descView.text = "Author: ${item.author}\n${context.getString(R.string.loading)}"
            val requestKey = item.id
            imageView.tag = requestKey

            updateFavoriteIcon(favoriteStore.isFavorite(item.id))

            itemView.setOnClickListener { onClick(item) }
            favoriteButton.setOnClickListener {
                val isFavorite = favoriteStore.toggle(item)
                updateFavoriteIcon(isFavorite)
                onFavoriteChanged(item, isFavorite)
            }

            scope.launch {
                val result = try {
                    repository.loadImage(item)
                } catch (ex: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    if (imageView.tag != requestKey) return@withContext
                    if (result != null) {
                        imageView.setImageBitmap(result.bitmap)
                        descView.text = result.description
                    } else {
                        descView.text = "Author: ${item.author}\n${context.getString(R.string.load_failed)}"
                    }
                }
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                favoriteButton.contentDescription = itemView.context.getString(R.string.unfavorite)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                favoriteButton.contentDescription = itemView.context.getString(R.string.favorite)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem == newItem
        }
    }
}
