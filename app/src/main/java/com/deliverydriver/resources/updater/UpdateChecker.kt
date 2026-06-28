package com.deliverydriver.resources.updater

import android.content.Context
import android.content.pm.PackageManager
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

suspend fun checkForUpdate(context: Context): UpdateInfo = withContext(Dispatchers.IO) {
    try {
        val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        val currentVersionName = packageInfo.versionName ?: "1.0"

        val url = URL("https://api.github.com/repos/${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("User-Agent", "SortAssist-UpdateChecker")
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

        val currentParts = currentVersionName.split(".").map { it.toIntOrNull() ?: 0 }
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
