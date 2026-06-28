package com.deliverydriver.resources.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class UpdateManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1L
    private var downloadedFileName: String = ""

    fun downloadAndInstall(downloadUrl: String, versionName: String) {
        downloadedFileName = "SortAssist-v$versionName.apk"

        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val previousFile = File(destDir, downloadedFileName)
        previousFile.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("Sort Assist Update")
            setDescription("Downloading v$versionName...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadedFileName)
            setMimeType("application/vnd.android.package-archive")
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setRequiresCharging(false)
        }

        downloadId = downloadManager.enqueue(request)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (receivedId != downloadId) return

            context.unregisterReceiver(this)

            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (!cursor.moveToFirst()) {
                cursor.close()
                return
            }

            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusIndex)
            cursor.close()

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                installApk()
            } else {
                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun installApk() {
        try {
            val apkFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                downloadedFileName
            )
            if (!apkFile.exists()) {
                Toast.makeText(context, "Update file not found", Toast.LENGTH_LONG).show()
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open installer: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
