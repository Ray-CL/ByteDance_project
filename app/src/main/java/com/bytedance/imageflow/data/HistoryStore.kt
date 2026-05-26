package com.bytedance.imageflow.data

import android.content.Context
import com.bytedance.imageflow.model.ImageItem
import org.json.JSONArray
import org.json.JSONObject

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("history_store", Context.MODE_PRIVATE)
    private val key = "history_items"
    private val maxItems = 100

    fun load(): List<ImageItem> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val items = ArrayList<ImageItem>(array.length())
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                val id = obj.optString("id")
                val url = obj.optString("url")
                val downloadUrl = obj.optString("downloadUrl")
                if (id.isBlank() || url.isBlank() || downloadUrl.isBlank()) continue
                items.add(
                    ImageItem(
                        id = id,
                        author = obj.optString("author", "Unknown"),
                        width = obj.optInt("width", 0),
                        height = obj.optInt("height", 0),
                        url = url,
                        downloadUrl = downloadUrl
                    )
                )
            }
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(item: ImageItem): List<ImageItem> {
        val items = load().toMutableList()
        val existingIndex = items.indexOfFirst { it.id == item.id }
        if (existingIndex >= 0) {
            items.removeAt(existingIndex)
        }
        items.add(0, item)
        if (items.size > maxItems) {
            items.subList(maxItems, items.size).clear()
        }
        save(items)
        return items
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }

    private fun save(items: List<ImageItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("author", item.author)
            obj.put("width", item.width)
            obj.put("height", item.height)
            obj.put("url", item.url)
            obj.put("downloadUrl", item.downloadUrl)
            array.put(obj)
        }
        prefs.edit().putString(key, array.toString()).apply()
    }
}
