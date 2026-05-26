package com.bytedance.imageflow.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object FormatUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Unknown"
        return dateFormat.format(Date(timestamp))
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val unit = 1024.0
        val exp = (ln(bytes.toDouble()) / ln(unit)).toInt()
        val prefix = "KMGTPE"[exp - 1]
        val value = bytes / unit.pow(exp.toDouble())
        return String.format(Locale.US, "%.1f %sB", value, prefix)
    }
}
