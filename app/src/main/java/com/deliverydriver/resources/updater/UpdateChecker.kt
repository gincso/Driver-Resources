package com.deliverydriver.resources.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val available: Boolean,
    val latestVersion: String = "",
    val downloadUrl: String = "",
    val releaseNotes: String = ""
)

suspend fun checkForUpdate(): UpdateInfo = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/${UpdateConfig.githubOwner}/${UpdateConfig.githubRepo}/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            return@withContext UpdateInfo(false)
        }

        val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(jsonText)
        val tagName = json.optString("tag_name", "").removePrefix("v")
        val body = json.optString("body", "")

        if (tagName.isBlank()) return@withContext UpdateInfo(false)

        val assets = json.optJSONArray("assets")
        var downloadUrl = ""
        if (assets != null && assets.length() > 0) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk")) {
                    downloadUrl = asset.optString("browser_download_url", "")
                    break
                }
            }
        }

        if (downloadUrl.isBlank()) return@withContext UpdateInfo(false)

        val currentParts = UpdateConfig.currentVersionName.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = tagName.split(".").map { it.toIntOrNull() ?: 0 }

        val isNewer = compareVersionLists(latestParts, currentParts) > 0

        UpdateInfo(
            available = isNewer,
            latestVersion = tagName,
            downloadUrl = downloadUrl,
            releaseNotes = body
        )
    } catch (e: Exception) {
        e.printStackTrace()
        UpdateInfo(false)
    }
}

private fun compareVersionLists(a: List<Int>, b: List<Int>): Int {
    val maxLen = maxOf(a.size, b.size)
    for (i in 0 until maxLen) {
        val va = a.getOrElse(i) { 0 }
        val vb = b.getOrElse(i) { 0 }
        if (va != vb) return va - vb
    }
    return 0
}
