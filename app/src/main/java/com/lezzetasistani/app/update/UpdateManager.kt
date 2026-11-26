package com.lezzetasistani.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.lezzetasistani.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("name") val releaseName: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("assets") val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    @SerialName("name") val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = "",
    @SerialName("size") val size: Long = 0L
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpdateAvailable(
        val newVersion: String,
        val releaseNotes: String,
        val downloadUrl: String
    ) : UpdateState
    data class Downloading(val progress: Float) : UpdateState // 0.0f .. 1.0f
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

class UpdateManager(
    private val repoOwner: String = "mczarali",
    private val repoName: String = "LezzetAsistan"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    suspend fun checkForUpdates() = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _updateState.value = UpdateState.Idle
                    return@withContext
                }

                val body = response.body.string()
                if (body.isBlank()) {
                    _updateState.value = UpdateState.Idle
                    return@withContext
                }

                val release = json.decodeFromString<GitHubRelease>(body)
                val cleanTag = release.tagName.removePrefix("v").removePrefix("V").trim()
                val currentVersion = BuildConfig.VERSION_NAME.trim()

                if (isNewerVersion(currentVersion = currentVersion, latestVersion = cleanTag)) {
                    val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                        ?: release.assets.firstOrNull()

                    val downloadUrl = apkAsset?.downloadUrl
                    if (!downloadUrl.isNullOrBlank()) {
                        _updateState.value = UpdateState.UpdateAvailable(
                            newVersion = cleanTag,
                            releaseNotes = release.body.ifBlank { release.releaseName },
                            downloadUrl = downloadUrl
                        )
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Idle
        }
    }

    suspend fun startDownloadAndInstall(context: Context, downloadUrl: String) = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateState.Downloading(0f)

            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }

            val destinationFile = File(updateDir, "LezzetAsistan_update.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = Request.Builder().url(downloadUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _updateState.value = UpdateState.Error("İndirme başarısız oldu (HTTP ${response.code})")
                    return@withContext
                }

                val responseBody = response.body
                val contentLength = responseBody.contentLength()
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                        _updateState.value = UpdateState.Downloading(progress.coerceIn(0f, 1f))
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _updateState.value = UpdateState.ReadyToInstall(destinationFile)
                installApk(context, destinationFile)
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Güncelleme indirilemedi: ${e.message}")
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Paket yükleyici başlatılamadı: ${e.message}")
        }
    }

    fun dismissUpdate() {
        _updateState.value = UpdateState.Idle
    }

    private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        try {
            val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
            val latestParts = latestVersion.split(".").mapNotNull { it.toIntOrNull() }

            val maxParts = maxOf(currentParts.size, latestParts.size)
            for (i in 0 until maxParts) {
                val currentPart = currentParts.getOrElse(i) { 0 }
                val latestPart = latestParts.getOrElse(i) { 0 }
                if (latestPart > currentPart) return true
                if (latestPart < currentPart) return false
            }
        } catch (_: Exception) {
            return latestVersion != currentVersion
        }
        return false
    }
}
