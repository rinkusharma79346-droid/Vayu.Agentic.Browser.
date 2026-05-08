package com.vayu.agenticbrowser.downloads

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.engine.AgenticBridge
import com.vayu.agenticbrowser.tabs.TabManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class VayuDownloadManager private constructor() {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _downloads = MutableStateFlow<Map<String, DownloadRecord>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadRecord>> = _downloads.asStateFlow()

    private var appContext: Context? = null
    private var pollingJob: Job? = null
    private var downloadManager: DownloadManager? = null

    private val _newDownloadEvent = MutableSharedFlow<DownloadRecord>(extraBufferCapacity = 16)
    val newDownloadEvent: SharedFlow<DownloadRecord> = _newDownloadEvent.asSharedFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    suspend fun triggerDownload(selectorOrUrl: String, tabId: Int?): String {
        val ctx = appContext ?: return """{"error":"DownloadManager not initialized"}"""
        val dm = downloadManager ?: return """{"error":"DownloadManager not available"}"""

        return try {
            val isUrl = selectorOrUrl.startsWith("http://") || selectorOrUrl.startsWith("https://")
            val downloadId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            if (isUrl) {
                val filename = Uri.parse(selectorOrUrl).lastPathSegment
                    ?: "download_${System.currentTimeMillis()}"

                val request = DownloadManager.Request(Uri.parse(selectorOrUrl))
                    .setTitle(filename)
                    .setDescription("VAYU Browser download")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(ctx, "Downloads", filename)

                val systemDownloadId = dm.enqueue(request)

                val record = DownloadRecord(
                    id = downloadId,
                    filename = filename,
                    localPath = getDownloadPath(filename),
                    url = selectorOrUrl,
                    mimeType = "application/octet-stream",
                    size = 0L,
                    status = DownloadStatus.PENDING,
                    progress = 0,
                    startedAt = now,
                    triggeredBy = DownloadTrigger.AGENT
                )

                _downloads.update { it + (downloadId to record) }
                _newDownloadEvent.tryEmit(record)
                startPolling(downloadId, systemDownloadId)

                json.encodeToString(TriggerDownloadResult(downloadId = downloadId))
            } else {
                val effectiveTabId = tabId ?: TabManager.getInstance().getActiveTabIdValue()
                if (effectiveTabId == -1) {
                    return """{"error":"No active tab"}"""
                }

                val tabManager = TabManager.getInstance()
                val wv = tabManager.getTab(effectiveTabId)
                    ?: return """{"error":"Tab $effectiveTabId not found"}"""

                val clickResult = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                    val jsCode = """
                        (function() {
                            try {
                                var el = document.querySelector('${selectorOrUrl.replace("'", "\\'")}');
                                if (!el) return JSON.stringify({success: false, error: 'ELEMENT_NOT_FOUND'});
                                var href = el.href || el.getAttribute('href');
                                if (href) return JSON.stringify({success: true, url: href});
                                el.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
                                el.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true}));
                                el.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true}));
                                return JSON.stringify({success: true, url: null});
                            } catch(e) {
                                return JSON.stringify({success: false, error: e.message});
                            }
                        })()
                    """.trimIndent()
                    wv.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
                }

                val resultJson = clickResult ?: """{"success":false,"error":"No result"}"""
                val parsed = json.parseToJsonElement(resultJson).jsonObject
                val success = parsed["success"]?.jsonPrimitive?.booleanOrNull ?: false

                if (!success) {
                    return """{"error":"Failed to click download element: ${parsed["error"]?.jsonPrimitive?.contentOrNull ?: "unknown"}"}"""
                }

                val downloadUrl = parsed["url"]?.jsonPrimitive?.contentOrNull
                if (downloadUrl != null) {
                    return triggerDownload(downloadUrl, tabId)
                }

                val record = DownloadRecord(
                    id = downloadId,
                    filename = "click_download_${System.currentTimeMillis()}",
                    localPath = getDownloadPath("click_download_${System.currentTimeMillis()}"),
                    url = selectorOrUrl,
                    mimeType = "application/octet-stream",
                    size = 0L,
                    status = DownloadStatus.PENDING,
                    progress = 0,
                    startedAt = now,
                    triggeredBy = DownloadTrigger.AGENT
                )

                _downloads.update { it + (downloadId to record) }
                _newDownloadEvent.tryEmit(record)

                json.encodeToString(TriggerDownloadResult(downloadId = downloadId))
            }
        } catch (e: Exception) {
            Logger.e("triggerDownload error", e)
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun listDownloads(): String {
        return json.encodeToString(_downloads.value.values.toList())
    }

    suspend fun waitForDownload(downloadId: String, timeoutMs: Long = 60_000L): String {
        val record = _downloads.value[downloadId]
            ?: return """{"completed":false,"error":"Download $downloadId not found"}"""

        return try {
            withTimeoutOrNull(timeoutMs) {
                _downloads
                    .map { it[downloadId] }
                    .first { it?.status == DownloadStatus.COMPLETED || it?.status == DownloadStatus.FAILED || it?.status == DownloadStatus.CANCELLED }
            }?.let { r ->
                when (r.status) {
                    DownloadStatus.COMPLETED -> json.encodeToString(
                        WaitForDownloadResult(
                            completed = true,
                            path = r.localPath,
                            size = r.size
                        )
                    )
                    DownloadStatus.FAILED -> """{"completed":false,"error":"Download failed"}"""
                    DownloadStatus.CANCELLED -> """{"completed":false,"error":"Download cancelled"}"""
                    else -> """{"completed":false,"error":"Unexpected status: ${r.status}"}"""
                }
            } ?: """{"completed":false,"error":"Timeout waiting for download"}"""
        } catch (e: Exception) {
            Logger.e("waitForDownload error", e)
            """{"completed":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun getPath(downloadId: String): String {
        val record = _downloads.value[downloadId]
            ?: return """{"error":"Download $downloadId not found"}"""
        return json.encodeToString(GetPathResult(path = record.localPath, status = record.status.name))
    }

    suspend fun cancelDownload(downloadId: String): String {
        val record = _downloads.value[downloadId]
            ?: return """{"error":"Download $downloadId not found"}"""

        val now = System.currentTimeMillis()
        _downloads.update { current ->
            current + (downloadId to record.copy(
                status = DownloadStatus.CANCELLED,
                completedAt = now
            ))
        }

        Logger.i("Cancelled download: $downloadId")
        return """{"success":true}"""
    }

    fun handleWebViewDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        val ctx = appContext ?: return
        val dm = downloadManager ?: return
        val downloadId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val filename = VayuDownloadListener.parseFilename(contentDisposition, url)

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("VAYU Browser download")
                .setMimeType(mimetype)
                .addRequestHeader("User-Agent", userAgent)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(ctx, "Downloads", filename)

            val systemDownloadId = dm.enqueue(request)

            val record = DownloadRecord(
                id = downloadId,
                filename = filename,
                localPath = getDownloadPath(filename),
                url = url,
                mimeType = mimetype,
                size = contentLength,
                status = DownloadStatus.PENDING,
                progress = 0,
                startedAt = now,
                triggeredBy = DownloadTrigger.USER
            )

            _downloads.update { it + (downloadId to record) }
            _newDownloadEvent.tryEmit(record)
            startPolling(downloadId, systemDownloadId)

            Logger.i("WebView download started: $filename")
        } catch (e: Exception) {
            Logger.e("handleWebViewDownload error", e)
        }
    }

    private fun startPolling(downloadId: String, systemDownloadId: Long) {
        val dm = downloadManager ?: return
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            while (isActive) {
                delay(500)

                val query = DownloadManager.Query().setFilterById(systemDownloadId)
                val cursor: Cursor? = dm.query(query)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val localUriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                        if (statusIndex >= 0) {
                            val status = it.getInt(statusIndex)
                            val bytesDownloaded = if (bytesIndex >= 0) it.getLong(bytesIndex) else 0L
                            val bytesTotal = if (totalIndex >= 0) it.getLong(totalIndex) else 0L
                            val localUri = if (localUriIndex >= 0) it.getString(localUriIndex) else null

                            val progress = if (bytesTotal > 0) ((bytesDownloaded * 100) / bytesTotal).toInt() else 0

                            _downloads.update { current ->
                                val record = current[downloadId] ?: return@update current
                                val newRecord = when (status) {
                                    DownloadManager.STATUS_RUNNING -> record.copy(
                                        status = DownloadStatus.DOWNLOADING,
                                        progress = progress,
                                        size = bytesTotal
                                    )
                                    DownloadManager.STATUS_SUCCESSFUL -> record.copy(
                                        status = DownloadStatus.COMPLETED,
                                        progress = 100,
                                        size = bytesTotal,
                                        completedAt = System.currentTimeMillis(),
                                        localPath = localUri ?: record.localPath
                                    )
                                    DownloadManager.STATUS_FAILED -> record.copy(
                                        status = DownloadStatus.FAILED,
                                        completedAt = System.currentTimeMillis()
                                    )
                                    DownloadManager.STATUS_PAUSED -> record.copy(
                                        status = DownloadStatus.PENDING,
                                        progress = progress
                                    )
                                    else -> record
                                }
                                current + (downloadId to newRecord)
                            }

                            val currentRecord = _downloads.value[downloadId]
                            if (currentRecord?.status == DownloadStatus.COMPLETED ||
                                currentRecord?.status == DownloadStatus.FAILED ||
                                currentRecord?.status == DownloadStatus.CANCELLED
                            ) {
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDownloadPath(filename: String): String {
        val ctx = appContext ?: return filename
        return File(ctx.getExternalFilesDir("Downloads"), filename).absolutePath
    }

    @kotlinx.serialization.Serializable
    data class TriggerDownloadResult(val downloadId: String)

    @kotlinx.serialization.Serializable
    data class WaitForDownloadResult(
        val completed: Boolean,
        val path: String = "",
        val size: Long = 0L,
        val error: String? = null
    )

    @kotlinx.serialization.Serializable
    data class GetPathResult(val path: String, val status: String)

    companion object {
        @Volatile
        private var instance: VayuDownloadManager? = null

        fun getInstance(): VayuDownloadManager {
            return instance ?: synchronized(this) {
                instance ?: VayuDownloadManager().also { instance = it }
            }
        }
    }
}
