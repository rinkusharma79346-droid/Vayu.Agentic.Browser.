package com.vayu.agenticbrowser.downloads

import kotlinx.serialization.Serializable

@Serializable
data class DownloadRecord(
    val id: String,
    val filename: String,
    val localPath: String,
    val url: String,
    val mimeType: String,
    val size: Long,
    val status: DownloadStatus,
    val progress: Int,
    val startedAt: Long,
    val completedAt: Long? = null,
    val triggeredBy: DownloadTrigger
)

@Serializable
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
enum class DownloadTrigger {
    USER,
    AGENT
}
