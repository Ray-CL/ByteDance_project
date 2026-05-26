package com.bytedance.imageflow.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.bytedance.imageflow.model.ImageItem
import com.bytedance.imageflow.util.FormatUtils
import com.bytedance.imageflow.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ImageMeta(
    val filePath: String,
    val fileSizeBytes: Long,
    val lastModified: Long
)

data class ImageLoadResult(
    val bitmap: Bitmap,
    val meta: ImageMeta,
    val description: String
)

class ImageRepository private constructor(
    private val context: Context,
    private val client: OkHttpClient,
    private val api: PicsumApi
) {
    private val imageDir = File(context.filesDir, "images").apply { mkdirs() }
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSizeKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    companion object {
        fun create(context: Context): ImageRepository {
            val cacheDir = File(context.cacheDir, "http_cache")
            val cache = Cache(cacheDir, 20L * 1024 * 1024)
            val client = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            return ImageRepository(context.applicationContext, client, PicsumApi(client))
        }
    }

    suspend fun fetchImages(page: Int, limit: Int): List<ImageItem> {
        return withContext(Dispatchers.IO) {
            api.fetchImages(page, limit)
        }
    }

    suspend fun loadImage(item: ImageItem): ImageLoadResult {
        return withContext(Dispatchers.IO) {
            val key = item.id
            val file = getImageFile(item)
            val cached = memoryCache.get(key)
            val bitmap = cached ?: run {
                if (!file.exists() || file.length() == 0L) {
                    if (!NetworkUtils.isOnline(context)) {
                        throw IOException("Offline and no cache")
                    }
                    downloadToFile(item.downloadUrl, file)
                }
                BitmapFactory.decodeFile(file.absolutePath)
            }
            if (bitmap == null) {
                file.delete()
                throw IOException("Decode failed")
            }
            memoryCache.put(key, bitmap)
            val meta = ImageMeta(file.absolutePath, file.length(), file.lastModified())
            ImageLoadResult(bitmap, meta, buildDescription(item, meta))
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            memoryCache.evictAll()
            imageDir.listFiles()?.forEach { it.delete() }
            client.cache?.evictAll()
        }
    }

    private fun getImageFile(item: ImageItem): File {
        return File(imageDir, "image_${item.id}.jpg")
    }

    private fun downloadToFile(url: String, target: File) {
        target.parentFile?.mkdirs()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("Empty body")
            target.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            target.setLastModified(System.currentTimeMillis())
        }
    }

    private fun buildDescription(item: ImageItem, meta: ImageMeta): String {
        val dateText = FormatUtils.formatDate(meta.lastModified)
        val sizeText = FormatUtils.formatBytes(meta.fileSizeBytes)
        return "Author: ${item.author}\n" +
            "Resolution: ${item.width}x${item.height}\n" +
            "Date: $dateText\n" +
            "Size: $sizeText\n" +
            "Path: ${meta.filePath}"
    }

    private fun cacheSizeKb(): Int {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemoryKb / 8
    }
}
