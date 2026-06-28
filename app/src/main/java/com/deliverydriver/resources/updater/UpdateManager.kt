package com.deliverydriver.resources.updater

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun downloadAndInstall(downloadUrl: String, versionName: String) {
        Toast.makeText(context, "Downloading v$versionName...", Toast.LENGTH_SHORT).show()

        scope.launch {
            try {
                val apkFile = File(context.cacheDir, "update-$versionName.apk")
                apkFile.delete()

                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "SortAssist-Updater")
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Download failed (HTTP ${connection.responseCode})", Toast.LENGTH_LONG).show()
                    }
                    connection.disconnect()
                    return@launch
                }

                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(apkFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                if (!apkFile.exists() || totalBytes == 0L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Downloaded file is empty", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )

                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(installIntent)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update error: ${e.localizedMessage ?: "unknown"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
