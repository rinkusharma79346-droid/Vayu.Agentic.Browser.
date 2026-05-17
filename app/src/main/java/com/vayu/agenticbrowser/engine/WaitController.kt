package com.vayu.agenticbrowser.engine

import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.tabs.TabManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class WaitController(
    private val tabManager: TabManager,
    private val downloadManager: VayuDownloadManager
) {

    suspend fun waitForSelector(
        selector: String,
        tabId: Int? = null,
        timeoutMs: Long = 30_000
    ): String {
        val effectiveTabId = tabId ?: tabManager.getActiveTabIdValue()
        if (effectiveTabId == -1) return """{"found":false,"error":"No active tab"}"""

        return try {
            val found = withTimeoutOrNull(timeoutMs) {
                var result = false
                while (!result) {
                    result = checkSelectorExists(effectiveTabId, selector)
                    if (!result) delay(500)
                }
                true
            }

            if (found == true) {
                """{"found":true,"selector":"$selector"}"""
            } else {
                """{"found":false,"error":"Timeout waiting for selector: $selector"}"""
            }
        } catch (e: Exception) {
            Logger.e("waitForSelector error", e)
            """{"found":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun waitForText(
        text: String,
        tabId: Int? = null,
        timeoutMs: Long = 30_000
    ): String {
        val effectiveTabId = tabId ?: tabManager.getActiveTabIdValue()
        if (effectiveTabId == -1) return """{"found":false,"error":"No active tab"}"""

        return try {
            val found = withTimeoutOrNull(timeoutMs) {
                var result = false
                while (!result) {
                    result = checkTextExists(effectiveTabId, text)
                    if (!result) delay(500)
                }
                true
            }

            if (found == true) {
                """{"found":true,"text":"${text.replace("\"", "\\\"")}"}"""
            } else {
                """{"found":false,"error":"Timeout waiting for text"}"""
            }
        } catch (e: Exception) {
            Logger.e("waitForText error", e)
            """{"found":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun waitForNavigation(
        tabId: Int? = null,
        timeoutMs: Long = 30_000
    ): String {
        val effectiveTabId = tabId ?: tabManager.getActiveTabIdValue()
        if (effectiveTabId == -1) return """{"navigated":false,"error":"No active tab"}"""

        return try {
            val initialUrl = withContext(Dispatchers.Main) {
                tabManager.getTab(effectiveTabId)?.url ?: ""
            }

            val navigated = withTimeoutOrNull(timeoutMs) {
                var currentUrl = initialUrl
                while (currentUrl == initialUrl) {
                    delay(500)
                    currentUrl = withContext(Dispatchers.Main) {
                        tabManager.getTab(effectiveTabId)?.url ?: ""
                    }
                }
                currentUrl
            }

            if (navigated != null) {
                """{"navigated":true,"url":"${navigated.replace("\"", "\\\"")}"}"""
            } else {
                """{"navigated":false,"error":"Timeout waiting for navigation"}"""
            }
        } catch (e: Exception) {
            Logger.e("waitForNavigation error", e)
            """{"navigated":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun waitForUrlContains(
        substring: String,
        tabId: Int? = null,
        timeoutMs: Long = 30_000
    ): String {
        val effectiveTabId = tabId ?: tabManager.getActiveTabIdValue()
        if (effectiveTabId == -1) return """{"found":false,"error":"No active tab"}"""

        return try {
            val found = withTimeoutOrNull(timeoutMs) {
                var currentUrl = withContext(Dispatchers.Main) {
                    tabManager.getTab(effectiveTabId)?.url ?: ""
                }
                while (!currentUrl.contains(substring)) {
                    delay(500)
                    currentUrl = withContext(Dispatchers.Main) {
                        tabManager.getTab(effectiveTabId)?.url ?: ""
                    }
                }
                currentUrl
            }

            if (found != null) {
                """{"found":true,"url":"${found.replace("\"", "\\\"")}"}"""
            } else {
                """{"found":false,"error":"Timeout waiting for URL containing: $substring"}"""
            }
        } catch (e: Exception) {
            Logger.e("waitForUrlContains error", e)
            """{"found":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun waitForDownload(timeoutMs: Long = 30_000): String {
        return try {
            val record = withTimeoutOrNull(timeoutMs) {
                downloadManager.newDownloadEvent.first()
            }

            if (record != null) {
                """{"found":true,"downloadId":"${record.id}","filename":"${record.filename}","url":"${record.url.replace("\"", "\\\"")}"}"""
            } else {
                """{"found":false,"error":"Timeout waiting for download"}"""
            }
        } catch (e: Exception) {
            Logger.e("waitForDownload error", e)
            """{"found":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    /**
     * Check if a selector exists by evaluating JS on the WebView.
     * Must run on Main thread since it calls WebView.evaluateJavascript().
     */
    private suspend fun checkSelectorExists(tabId: Int, selector: String): Boolean {
        return withContext(Dispatchers.Main) {
            val wv = tabManager.getTab(tabId) ?: return@withContext false
            suspendCancellableCoroutine { cont ->
                val jsCode = """
                    (function() {
                        try {
                            var el = document.querySelector('${selector.replace("'", "\\'")}');
                            return el !== null ? 'true' : 'false';
                        } catch(e) {
                            return 'false';
                        }
                    })()
                """.trimIndent()
                wv.evaluateJavascript(jsCode) { result ->
                    cont.resume(result?.trim() == "true") {}
                }
            }
        }
    }

    /**
     * Check if text exists on the page by evaluating JS on the WebView.
     * Must run on Main thread since it calls WebView.evaluateJavascript().
     */
    private suspend fun checkTextExists(tabId: Int, text: String): Boolean {
        return withContext(Dispatchers.Main) {
            val wv = tabManager.getTab(tabId) ?: return@withContext false
            val escapedText = text.replace("\\", "\\\\").replace("'", "\\'")
            suspendCancellableCoroutine { cont ->
                val jsCode = """
                    (function() {
                        try {
                            return document.body.innerText.includes('$escapedText') ? 'true' : 'false';
                        } catch(e) {
                            return 'false';
                        }
                    })()
                """.trimIndent()
                wv.evaluateJavascript(jsCode) { result ->
                    cont.resume(result?.trim() == "true") {}
                }
            }
        }
    }
}
