package com.vayu.agenticbrowser.downloads

import android.net.Uri
import android.webkit.DownloadListener
import android.webkit.MimeTypeMap
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

class VayuDownloadListener(
    private val onDownloadStart: (url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) -> Unit
) : DownloadListener {

    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        if (url == null) {
            Logger.w("Download URL is null, ignoring")
            return
        }

        Logger.i("Download intercepted: url=$url, mimetype=$mimetype, size=$contentLength")
        onDownloadStart(
            url,
            userAgent ?: "",
            contentDisposition ?: "",
            mimetype ?: "application/octet-stream",
            contentLength
        )
    }

    companion object {
        fun parseFilename(contentDisposition: String, url: String): String {
            val filenameRegex = Regex("filename\\*?=(?:UTF-8''|\"?)([^\";]+)", RegexOption.IGNORE_CASE)
            val match = filenameRegex.find(contentDisposition)
            if (match != null) {
                return java.net.URLDecoder.decode(match.groupValues[1].trim(), "UTF-8")
            }

            val path = Uri.parse(url).path
            if (!path.isNullOrEmpty()) {
                val lastSegment = path.substringAfterLast('/')
                if (lastSegment.contains('.')) {
                    return lastSegment
                }
            }

            return "download_${System.currentTimeMillis()}"
        }
    }
}
