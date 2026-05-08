package com.vayu.agenticbrowser.agent

import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.ScreenshotUtil
import com.vayu.agenticbrowser.engine.WaitController
import com.vayu.agenticbrowser.tabs.TabManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.util.UUID

class McpServer(
    private val domController: DomController,
    private val tabManager: TabManager,
    private val downloadManager: VayuDownloadManager,
    private val waitController: WaitController
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var server: ApplicationEngine? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val pairingCode = "vayu1234"

    fun start() {
        if (server != null) {
            Logger.w("MCP Server already running")
            return
        }

        Logger.i("Starting MCP WebSocket server on port 8765")

        server = embeddedServer(CIO, port = 8765) {
            install(WebSockets)

            routing {
                webSocket("/mcp") {
                    Logger.i("New MCP client connection")
                    val sessionId = UUID.randomUUID().toString()

                    try {
                        // Step 1: Auth challenge
                        val nonce = UUID.randomUUID().toString()
                        val challengeMsg = json.encodeToString(
                            AuthChallenge(type = "auth_challenge", nonce = nonce)
                        )
                        send(Frame.Text(challengeMsg))
                        Logger.d("Sent auth_challenge with nonce: $nonce")

                        // Step 2: Read auth response
                        val authFrame = incoming.receive()
                        val authText = (authFrame as? Frame.Text)?.readText() ?: run {
                            Logger.w("Invalid auth frame received")
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid frame"))
                            return@webSocket
                        }

                        Logger.d("Received auth response: $authText")

                        val authPayload = json.parseToJsonElement(authText).jsonObject
                        val clientHash = authPayload["hash"]?.jsonPrimitive?.content ?: ""

                        // Verify SHA-256(pairingCode + nonce)
                        val expectedHash = sha256Hex(pairingCode + nonce)
                        if (clientHash != expectedHash) {
                            Logger.w("Auth failed: hash mismatch")
                            val failMsg = json.encodeToString(
                                AuthResult(type = "auth_failure", error = "Invalid pairing code")
                            )
                            send(Frame.Text(failMsg))
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Auth failed"))
                            return@webSocket
                        }

                        // Step 3: Auth success
                        Logger.i("Auth successful for session: $sessionId")
                        val successMsg = json.encodeToString(
                            AuthSuccess(type = "auth_success", sessionId = sessionId)
                        )
                        send(Frame.Text(successMsg))

                        // Step 4: Main message loop
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            Logger.d("Received message: $text")
                            val response = handleMessage(text)
                            send(Frame.Text(response))
                        }

                    } catch (e: Exception) {
                        Logger.e("MCP WebSocket error", e)
                    } finally {
                        Logger.i("MCP client disconnected: $sessionId")
                    }
                }
            }
        }.start(wait = false)

        _isRunning.value = true
        Logger.i("MCP Server started on port 8765")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        _isRunning.value = false
        Logger.i("MCP Server stopped")
    }

    private suspend fun handleMessage(rawMessage: String): String {
        return try {
            val message = json.parseToJsonElement(rawMessage).jsonObject
            val type = message["type"]?.jsonPrimitive?.content ?: ""
            val id = message["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()

            when (type) {
                "tool/list" -> {
                    val toolsJson = ToolRegistry.toJson()
                    json.encodeToString(
                        ToolListResponse(
                            type = "tool/list_response",
                            id = id,
                            tools = json.parseToJsonElement(toolsJson).jsonArray
                        )
                    )
                }

                "tool/call" -> {
                    val tool = message["tool"]?.jsonPrimitive?.content ?: ""
                    val args = message["args"]?.jsonObject ?: buildJsonObject {}
                    handleToolCall(id, tool, args)
                }

                else -> {
                    json.encodeToString(
                        ErrorResponse(
                            type = "error",
                            id = id,
                            error = ErrorDetail(code = "UNKNOWN_TYPE", message = "Unknown message type: $type")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e("Message handling error", e)
            json.encodeToString(
                ErrorResponse(
                    type = "error",
                    id = "",
                    error = ErrorDetail(code = "PARSE_ERROR", message = e.message ?: "Failed to parse message")
                )
            )
        }
    }

    private suspend fun handleToolCall(id: String, tool: String, args: JsonObject): String {
        return try {
            val result: String = when (tool) {
                // ===== Phase 1: Browser DOM Tools =====
                "browser_navigate" -> {
                    val url = args["url"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    domController.navigate(url, tabId)
                }

                "browser_query_selector" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val all = args["all"]?.jsonPrimitive?.booleanOrNull ?: false
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    domController.querySelector(selector, all, tabId)
                }

                "browser_click" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val index = args["index"]?.jsonPrimitive?.intOrNull ?: 0
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    domController.click(selector, index, tabId)
                }

                "browser_type" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val text = args["text"]?.jsonPrimitive?.content ?: ""
                    val clearFirst = args["clearFirst"]?.jsonPrimitive?.booleanOrNull ?: false
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    domController.type(selector, text, clearFirst, tabId)
                }

                "browser_evaluate" -> {
                    val script = args["script"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    domController.evaluate(script, tabId)
                }

                // ===== Phase 2: Tab Tools =====
                "tab_new" -> {
                    val url = args["url"]?.jsonPrimitive?.content ?: ""
                    val background = args["background"]?.jsonPrimitive?.booleanOrNull ?: false
                    val tabState = tabManager.newTab(url, background)
                    json.encodeToString(tabState)
                }

                "tab_close" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull ?: -1
                    val closed = tabManager.closeTab(tabId)
                    """{"success":$closed}"""
                }

                "tab_switch" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull ?: -1
                    val tabState = tabManager.switchTab(tabId)
                    json.encodeToString(tabState)
                }

                "tab_list" -> {
                    json.encodeToString(tabManager.tabs.value)
                }

                "tab_execute" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull ?: -1
                    val subTool = args["tool"]?.jsonPrimitive?.content ?: ""
                    val subArgs = args["args"]?.jsonObject ?: buildJsonObject {}
                    handleTabExecute(tabId, subTool, subArgs)
                }

                "tab_wait_for_load" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    handleTabWaitForLoad(tabId, timeoutMs)
                }

                // ===== Phase 2: Download Tools =====
                "download_trigger" -> {
                    val selectorOrUrl = args["selectorOrUrl"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    downloadManager.triggerDownload(selectorOrUrl, tabId)
                }

                "download_list" -> {
                    downloadManager.listDownloads()
                }

                "download_wait" -> {
                    val downloadId = args["downloadId"]?.jsonPrimitive?.content ?: ""
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 60_000L
                    downloadManager.waitForDownload(downloadId, timeoutMs)
                }

                "download_get_path" -> {
                    val downloadId = args["downloadId"]?.jsonPrimitive?.content ?: ""
                    downloadManager.getPath(downloadId)
                }

                "download_cancel" -> {
                    val downloadId = args["downloadId"]?.jsonPrimitive?.content ?: ""
                    downloadManager.cancelDownload(downloadId)
                }

                // ===== Phase 2: Wait Tools =====
                "wait_for_selector" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    waitController.waitForSelector(selector, tabId, timeoutMs)
                }

                "wait_for_text" -> {
                    val text = args["text"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    waitController.waitForText(text, tabId, timeoutMs)
                }

                "wait_for_navigation" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    waitController.waitForNavigation(tabId, timeoutMs)
                }

                "wait_for_url_contains" -> {
                    val substring = args["substring"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    waitController.waitForUrlContains(substring, tabId, timeoutMs)
                }

                "wait_for_download" -> {
                    val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 30_000L
                    waitController.waitForDownload(timeoutMs)
                }

                // ===== Phase 2: Screenshot Tools =====
                "screenshot_full" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"success":false,"error":"No WebView available"}"""
                    ScreenshotUtil.screenshotFull(wv)
                }

                "screenshot_element" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"success":false,"error":"No WebView available"}"""
                    ScreenshotUtil.screenshotElement(wv, selector)
                }

                else -> {
                    """{"error":"Unknown tool: $tool"}"""
                }
            }

            json.encodeToString(
                ToolCallResponse(
                    type = "tool/call_response",
                    id = id,
                    status = "success",
                    result = json.parseToJsonElement(result)
                )
            )
        } catch (e: Exception) {
            Logger.e("Tool call error for $tool", e)
            json.encodeToString(
                ToolCallResponse(
                    type = "tool/call_response",
                    id = id,
                    status = "error",
                    error = ErrorDetail(code = "JS_ERROR", message = e.message ?: "Tool execution failed")
                )
            )
        }
    }

    private suspend fun handleTabExecute(tabId: Int, subTool: String, subArgs: JsonObject): String {
        return try {
            when (subTool) {
                "navigate" -> {
                    val url = subArgs["url"]?.jsonPrimitive?.content ?: ""
                    domController.navigate(url, tabId)
                }
                "querySelector" -> {
                    val selector = subArgs["selector"]?.jsonPrimitive?.content ?: ""
                    val all = subArgs["all"]?.jsonPrimitive?.booleanOrNull ?: false
                    domController.querySelector(selector, all, tabId)
                }
                "click" -> {
                    val selector = subArgs["selector"]?.jsonPrimitive?.content ?: ""
                    val index = subArgs["index"]?.jsonPrimitive?.intOrNull ?: 0
                    domController.click(selector, index, tabId)
                }
                "type" -> {
                    val selector = subArgs["selector"]?.jsonPrimitive?.content ?: ""
                    val text = subArgs["text"]?.jsonPrimitive?.content ?: ""
                    val clearFirst = subArgs["clearFirst"]?.jsonPrimitive?.booleanOrNull ?: false
                    domController.type(selector, text, clearFirst, tabId)
                }
                "evaluate" -> {
                    val script = subArgs["script"]?.jsonPrimitive?.content ?: ""
                    domController.evaluate(script, tabId)
                }
                else -> """{"error":"Unknown sub-tool: $subTool"}"""
            }
        } catch (e: Exception) {
            Logger.e("tab_execute error", e)
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    private suspend fun handleTabWaitForLoad(tabId: Int?, timeoutMs: Long): String {
        val effectiveTabId = tabId ?: tabManager.getActiveTabIdValue()
        if (effectiveTabId == -1) return """{"loaded":false,"error":"No active tab"}"""

        val deferred = tabManager.createPageLoadDeferred(effectiveTabId)

        val loaded = kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            deferred.await()
        }

        val tabState = tabManager.tabs.value.find { it.tabId == effectiveTabId }
        return if (loaded != null) {
            """{"loaded":true,"url":"${loaded.replace("\"", "\\\"")}","title":"${tabState?.title?.replace("\"", "\\\"") ?: ""}"}"""
        } else {
            """{"loaded":false,"error":"Timeout waiting for tab $effectiveTabId to load"}"""
        }
    }

    private fun resolveWebView(tabId: Int?): android.webkit.WebView? {
        if (tabId != null) {
            return tabManager.getTab(tabId)
        }
        val activeId = tabManager.getActiveTabIdValue()
        if (activeId != -1) {
            return tabManager.getTab(activeId)
        }
        return com.vayu.agenticbrowser.engine.WebViewManager.getInstance().getWebView()
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Serializable message types

    @Serializable
    data class AuthChallenge(val type: String, val nonce: String)

    @Serializable
    data class AuthResult(val type: String, val error: String? = null)

    @Serializable
    data class AuthSuccess(val type: String, val sessionId: String)

    @Serializable
    data class ToolListResponse(val type: String, val id: String, val tools: JsonArray)

    @Serializable
    data class ToolCallResponse(
        val type: String,
        val id: String,
        val status: String,
        val result: JsonElement? = null,
        val error: ErrorDetail? = null
    )

    @Serializable
    data class ErrorResponse(val type: String, val id: String, val error: ErrorDetail)

    @Serializable
    data class ErrorDetail(val code: String, val message: String)
}
