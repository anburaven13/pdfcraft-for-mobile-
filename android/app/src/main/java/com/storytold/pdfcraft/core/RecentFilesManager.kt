package com.storytold.pdfcraft.core

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class RecentFileItem(
    val uriString: String,
    val name: String,
    val sizeBytes: Long,
    val lastOpened: Long,
    val isStarred: Boolean = false
) {
    val uri: Uri get() = Uri.parse(uriString)

    fun formattedSize(): String {
        if (sizeBytes <= 0) return "PDF Document"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$sizeBytes B"
        }
    }

    fun formattedDate(): String {
        val now = System.currentTimeMillis()
        val diff = now - lastOpened
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> {
                val date = java.util.Date(lastOpened)
                val format = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                format.format(date)
            }
        }
    }
}

object RecentFilesManager {
    private const val PREFS_NAME = "pdfcraft_recent_files"
    private const val KEY_FILES = "recent_file_list"
    private const val MAX_RECENTS = 50

    fun getRecentFiles(context: Context): List<RecentFileItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_FILES, null) ?: return emptyList()
        val list = mutableListOf<RecentFileItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RecentFileItem(
                        uriString = obj.getString("uri"),
                        name = obj.getString("name"),
                        sizeBytes = obj.optLong("size", 0L),
                        lastOpened = obj.optLong("lastOpened", System.currentTimeMillis()),
                        isStarred = obj.optBoolean("isStarred", false)
                    )
                )
            }
        } catch (e: Exception) {
            // Corrupt data fallback
        }
        return list.sortedByDescending { it.lastOpened }
    }

    fun addOrUpdateFile(context: Context, uri: Uri, name: String, sizeBytes: Long) {
        val currentList = getRecentFiles(context).toMutableList()
        val existingIndex = currentList.indexOfFirst { it.uriString == uri.toString() }
        val isStarred = if (existingIndex >= 0) currentList[existingIndex].isStarred else false

        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
        }

        currentList.add(
            0,
            RecentFileItem(
                uriString = uri.toString(),
                name = name,
                sizeBytes = sizeBytes,
                lastOpened = System.currentTimeMillis(),
                isStarred = isStarred
            )
        )

        val trimmed = if (currentList.size > MAX_RECENTS) currentList.take(MAX_RECENTS) else currentList
        saveList(context, trimmed)
    }

    fun toggleStarred(context: Context, uriString: String): Boolean {
        val currentList = getRecentFiles(context).toMutableList()
        val index = currentList.indexOfFirst { it.uriString == uriString }
        if (index >= 0) {
            val item = currentList[index]
            val newStarred = !item.isStarred
            currentList[index] = item.copy(isStarred = newStarred)
            saveList(context, currentList)
            return newStarred
        }
        return false
    }

    fun removeFile(context: Context, uriString: String) {
        val currentList = getRecentFiles(context).toMutableList()
        currentList.removeAll { it.uriString == uriString }
        saveList(context, currentList)
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_FILES).apply()
    }

    private fun saveList(context: Context, list: List<RecentFileItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("uri", item.uriString)
                put("name", item.name)
                put("size", item.sizeBytes)
                put("lastOpened", item.lastOpened)
                put("isStarred", item.isStarred)
            }
            jsonArray.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FILES, jsonArray.toString()).apply()
    }
}
