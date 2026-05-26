package com.bytedance.imageflow

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bytedance.imageflow.data.FavoriteStore
import com.bytedance.imageflow.data.ImageRepository
import com.bytedance.imageflow.model.ImageItem
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ITEM = "extra_item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        title = getString(R.string.detail_title)

        val item = intent.getParcelableExtra<ImageItem>(EXTRA_ITEM)
        if (item == null) {
            finish()
            return
        }

        val imageView = findViewById<ImageView>(R.id.detailImage)
        val textView = findViewById<TextView>(R.id.detailText)
        val favoriteButton = findViewById<ImageButton>(R.id.detailFavorite)
        textView.text = getString(R.string.loading)

        val repository = ImageRepository.create(this)
        val favoriteStore = FavoriteStore(this)
        updateFavoriteIcon(favoriteButton, favoriteStore.isFavorite(item.id))
        favoriteButton.setOnClickListener {
            val isFavorite = favoriteStore.toggle(item)
            updateFavoriteIcon(favoriteButton, isFavorite)
            val message = if (isFavorite) {
                getString(R.string.toast_favorite_added)
            } else {
                getString(R.string.toast_favorite_removed)
            }
            Toast.makeText(this@DetailActivity, message, Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            val result = try {
                repository.loadImage(item)
            } catch (ex: Exception) {
                null
            }
            if (result != null) {
                imageView.setImageBitmap(result.bitmap)
                textView.text = result.description
            } else {
                textView.text = getString(R.string.load_failed)
                Toast.makeText(this@DetailActivity, textView.text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_favorite_filled)
            button.contentDescription = getString(R.string.unfavorite)
        } else {
            button.setImageResource(R.drawable.ic_favorite_border)
            button.contentDescription = getString(R.string.favorite)
        }
    }
}
