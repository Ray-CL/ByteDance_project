package com.bytedance.imageflow.data

import com.bytedance.imageflow.model.ImageItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PicsumApi(private val client: OkHttpClient) {
    fun fetchImages(page: Int, limit: Int): List<ImageItem> {
        val url = "https://api.thecatapi.com/v1/images/search?limit=$limit&page=$page&order=Desc"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response")
            val array = JSONArray(body)
            val result = ArrayList<ImageItem>(array.length())
            for (index in 0 until array.length()) {
                val obj = array.getJSONObject(index)
                val imageUrl = obj.optString("url")
                if (imageUrl.isBlank()) continue
                result.add(
                    ImageItem(
                        id = obj.optString("id", "cat_${page}_$index"),
                        author = extractAuthor(obj),
                        width = obj.optInt("width", 0),
                        height = obj.optInt("height", 0),
                        url = imageUrl,
                        downloadUrl = imageUrl
                    )
                )
            }
            return result
        }
    }

    private fun extractAuthor(obj: JSONObject): String {
        val breeds = obj.optJSONArray("breeds") ?: return "TheCatAPI"
        if (breeds.length() == 0) return "TheCatAPI"
        val breed = breeds.optJSONObject(0) ?: return "TheCatAPI"
        val name = breed.optString("name")
        return if (name.isNotBlank()) name else "TheCatAPI"
    }
}
