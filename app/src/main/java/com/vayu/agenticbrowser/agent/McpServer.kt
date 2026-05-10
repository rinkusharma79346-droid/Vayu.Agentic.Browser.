package com.vayu.agenticbrowser.agent

import android.content.Context
import android.content.SharedPreferences
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.common.NetworkMonitor
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.DialogController
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.FormDetector
import com.vayu.agenticbrowser.engine.ScreenshotUtil
import com.vayu.agenticbrowser.engine.StealthController
import com.vayu.agenticbrowser.engine.WaitController
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.plugins.PluginRegistry
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.tunnel.SshTunnelManager
import com.vayu.agenticbrowser.tunnel.TunnelManager
import com.vayu.agenticbrowser.vault.BiometricAuth
import com.vayu.agenticbrowser.vault.CredentialVault
import com.vayu.agenticbrowser.vault.ProfileManager
import com.vayu.agenticbrowser.vault.SmsOtpReader
import com.vayu.agenticbrowser.vault.TotpGenerator
import com.vayu.agenticbrowser.brain.AgentLoop
import com.vayu.agenticbrowser.brain.BrainConfig
import com.vayu.agenticbrowser.brain.GoalScheduler
import com.vayu.agenticbrowser.brain.WorkflowEngine
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class McpServer(
    private val domController: DomController,
    private val tabManager: TabManager,
    private val downloadManager: VayuDownloadManager,
    private val waitController: WaitController,
    private val credentialVault: CredentialVault,
    private val profileManager: ProfileManager,
    private val biometricAuth: BiometricAuth,
    private val formDetector: FormDetector,
    private val dialogController: DialogController,
    private val smsOtpReader: SmsOtpReader,
    private val pluginRegistry: PluginRegistry,
    private val tunnelManager: TunnelManager,
    private val sessionRecorder: SessionRecorder,
    private val networkMonitor: NetworkMonitor,
    private val sshTunnelManager: SshTunnelManager
) {

    private lateinit var agentLoop: AgentLoop
    private lateinit var goalScheduler: GoalScheduler
    private var workflowEngine: WorkflowEngine? = null
    private var brainComponentsSet = false

    fun setBrainComponents(loop: AgentLoop, scheduler: GoalScheduler) {
        agentLoop = loop
        goalScheduler = scheduler
        brainComponentsSet = true
    }

    fun setWorkflowEngine(engine: WorkflowEngine) {
        workflowEngine = engine
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var server: ApplicationEngine? = null
    private var appContext: Context? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _connectedSince = MutableStateFlow(0L)
    val connectedSince: StateFlow<Long> = _connectedSince.asStateFlow()

    private val pairingCode = "vayu1234"

    // ===== MCP SSE (JSON-RPC) Support =====
    // Active SSE connections: sessionId -> OutputStreamWriter
    private val sseConnections = ConcurrentHashMap<String, CopyOnWriteArrayList<OutputStreamWriter>>()
    private var mcpInitialized = false
    private var clientCapabilities: JsonObject = buildJsonObject {}

    /**
     * Direct tool execution for the AgentLoop brain.
     * Bypasses WebSocket, executes the tool and returns the result string.
     */
    suspend fun executeToolDirectly(tool: String, args: JsonObject): String {
        return try {
            val id = java.util.UUID.randomUUID().toString()
            val response = handleToolCall(id, tool, args)
            val responseJson = json.parseToJsonElement(response).jsonObject
            when {
                responseJson.containsKey("result") -> responseJson["result"].toString()
                responseJson.containsKey("error") -> responseJson["error"].toString()
                else -> response
            }
        } catch (e: Exception) {
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    fun setContext(ctx: Context) {
        appContext = ctx.applicationContext
    }

    fun start() {
        if (server != null) {
            Logger.w("MCP Server already running")
            return
        }

        Logger.i("Starting MCP WebSocket server on port 8765")

        server = embeddedServer(CIO, port = 8765) {
            install(WebSockets)

            routing {
                // ===== Original WebSocket endpoint (custom protocol) =====
                webSocket("/mcp") {
                    Logger.i("New MCP WS client connection")
                    val sessionId = UUID.randomUUID().toString()

                    try {
                        val nonce = UUID.randomUUID().toString()
                        val challengeMsg = json.encodeToString(
                            AuthChallenge(type = "auth_challenge", nonce = nonce)
                        )
                        send(Frame.Text(challengeMsg))
                        Logger.d("Sent auth_challenge with nonce: $nonce")

                        val authFrame = incoming.receive()
                        val authText = (authFrame as? Frame.Text)?.readText() ?: run {
                            Logger.w("Invalid auth frame received")
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid frame"))
                            return@webSocket
                        }

                        Logger.d("Received auth response: $authText")

                        val authPayload = json.parseToJsonElement(authText).jsonObject
                        val clientHash = authPayload["hash"]?.jsonPrimitive?.content ?: ""

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

                        _connectedSince.value = System.currentTimeMillis()
                        Logger.i("Auth successful for session: $sessionId")
                        val successMsg = json.encodeToString(
                            AuthSuccess(type = "auth_success", sessionId = sessionId)
                        )
                        send(Frame.Text(successMsg))

                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            Logger.d("Received message: $text")
                            val response = handleMessage(text)
                            send(Frame.Text(response))
                        }

                    } catch (e: Exception) {
                        Logger.e("MCP WebSocket error", e)
                    } finally {
                        Logger.i("MCP WS client disconnected: $sessionId")
                    }
                }

                // ===== MCP SSE Endpoint (JSON-RPC spec for Claude / AI) =====
                // GET /sse — Client opens SSE connection to receive server events
                get("/sse") {
                    val sseSessionId = UUID.randomUUID().toString()
                    Logger.i("MCP SSE: New connection from Claude/AI client, sessionId=$sseSessionId")

                    _connectedSince.value = System.currentTimeMillis()

                    call.response.header("Cache-Control", "no-cache")
                    call.response.header("Connection", "keep-alive")
                    call.response.header("X-Accel-Buffering", "no")

                    val outputStream = call.response.outputStream()
                    val writer = OutputStreamWriter(outputStream)

                    // Register this SSE connection
                    sseConnections.getOrPut(sseSessionId) { CopyOnWriteArrayList() }.add(writer)

                    try {
                        // Send the endpoint event — tells the client where to POST messages
                        val messageEndpoint = "/message?sessionId=$sseSessionId"
                        writer.write("event: endpoint\n")
                        writer.write("data: $messageEndpoint\n\n")
                        writer.flush()
                        Logger.d("MCP SSE: Sent endpoint event to $sseSessionId")

                        // Keep the connection alive by sending periodic heartbeat comments
                        while (sseConnections.containsKey(sseSessionId)) {
                            Thread.sleep(15_000)
                            try {
                                writer.write(": heartbeat\n\n")
                                writer.flush()
                            } catch (e: Exception) {
                                Logger.d("MCP SSE: Client disconnected: $sseSessionId")
                                break
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e("MCP SSE: Connection error", e)
                    } finally {
                        sseConnections.remove(sseSessionId)
                        Logger.i("MCP SSE: Client disconnected: $sseSessionId")
                    }
                }

                // POST /message — Client sends JSON-RPC messages here
                post("/message") {
                    val sessionId = call.parameters["sessionId"] ?: UUID.randomUUID().toString()
                    val rawBody = call.receiveText()
                    Logger.d("MCP SSE: Received message from sessionId=$sessionId: ${rawBody.take(200)}")

                    try {
                        val response = handleJsonRpcMessage(rawBody)

                        // Send response as SSE event to the matching session
                        val writers = sseConnections[sessionId]
                        if (writers != null && writers.isNotEmpty()) {
                            for (writer in writers) {
                                try {
                                    writer.write("event: message\n")
                                    writer.write("data: $response\n\n")
                                    writer.flush()
                                } catch (e: Exception) {
                                    Logger.w("MCP SSE: Failed to write to session $sessionId", e)
                                }
                            }
                            call.respondText("{\"status\":\"ok\"}", ContentType.Application.Json)
                        } else {
                            // No active SSE connection for this session — return as HTTP response
                            call.respondText(response, ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        Logger.e("MCP SSE: Message handling error", e)
                        call.respondText(
                            """{"jsonrpc":"2.0","error":{"code":-32603,"message":"${e.message?.replace("\"", "\\\"")}"},"id":null}""",
                            ContentType.Application.Json
                        )
                    }
                }

                // GET /health — Health check endpoint for AI clients
                get("/health") {
                    call.respondText(
                        """{"status":"ok","server":"VAYU MCP","version":"1.0.0","tools":${ToolRegistry.tools.size},"transport":["websocket","sse"],"running":${_isRunning.value}}""",
                        ContentType.Application.Json
                    )
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
        _connectedSince.value = 0L
        Logger.i("MCP Server stopped")
    }

    /**
     * Handle MCP JSON-RPC messages (official MCP spec for Claude / AI assistants).
     * This follows the MCP specification with methods: initialize, tools/list, tools/call, notifications.
     */
    private suspend fun handleJsonRpcMessage(rawMessage: String): String {
        return try {
            val message = json.parseToJsonElement(rawMessage).jsonObject
            val jsonrpc = message["jsonrpc"]?.jsonPrimitive?.contentOrNull ?: "2.0"
            val id = message["id"]
            val method = message["method"]?.jsonPrimitive?.contentOrNull ?: ""
            val params = message["params"]?.jsonObject ?: buildJsonObject {}

            when (method) {
                "initialize" -> {
                    mcpInitialized = true
                    clientCapabilities = params["capabilities"]?.jsonObject ?: buildJsonObject {}

                    val result = buildJsonObject {
                        put("protocolVersion", "2024-11-05")
                        put("capabilities", buildJsonObject {
                            put("tools", buildJsonObject {
                                put("listChanged", true)
                            })
                        })
                        put("serverInfo", buildJsonObject {
                            put("name", "VAYU Agentic Browser")
                            put("version", "1.0.0")
                        })
                    }

                    buildJsonRpcResponse(id, result)
                }

                "notifications/initialized" -> {
                    // Client confirmed initialization — no response needed for notifications
                    """{"jsonrpc":"2.0","result":null,"id":null}"""
                }

                "tools/list" -> {
                    val toolDefs = ToolRegistry.tools.map { toolDef ->
                        buildJsonObject {
                            put("name", toolDef.name)
                            put("description", toolDef.description)
                            put("inputSchema", buildJsonObject {
                                put("type", "object")
                                put("properties", buildJsonObject {
                                    toolDef.parameters.forEach { (paramName, param) ->
                                        put(paramName, buildJsonObject {
                                            put("type", param.type)
                                            put("description", param.description)
                                        })
                                    }
                                })
                                val requiredParams = toolDef.parameters
                                    .filter { it.value.required }
                                    .keys
                                    .map { JsonPrimitive(it) }
                                if (requiredParams.isNotEmpty()) {
                                    put("required", JsonArray(requiredParams))
                                }
                            })
                        }
                    }

                    val result = buildJsonObject {
                        put("tools", JsonArray(toolDefs))
                    }

                    buildJsonRpcResponse(id, result)
                }

                "tools/call" -> {
                    val toolName = params["name"]?.jsonPrimitive?.contentOrNull ?: ""
                    val toolArgs = params["arguments"]?.jsonObject ?: buildJsonObject {}

                    if (toolName.isBlank()) {
                        buildJsonRpcError(id, -32602, "Missing tool name")
                    } else {
                        try {
                            val toolCallId = UUID.randomUUID().toString()
                            val toolResponse = handleToolCall(toolCallId, toolName, toolArgs)
                            val responseJson = json.parseToJsonElement(toolResponse).jsonObject

                            // Extract the actual result from our custom format
                            val resultContent = when {
                                responseJson.containsKey("result") -> responseJson["result"]!!.jsonPrimitive.content
                                responseJson.containsKey("error") -> {
                                    val errDetail = responseJson["error"]
                                    if (errDetail is JsonObject) {
                                        errDetail["message"]?.jsonPrimitive?.contentOrNull ?: errDetail.toString()
                                    } else errDetail.toString()
                                }
                                else -> toolResponse
                            }

                            val result = buildJsonObject {
                                put("content", JsonArray(listOf(
                                    buildJsonObject {
                                        put("type", "text")
                                        put("text", resultContent)
                                    }
                                )))
                            }

                            buildJsonRpcResponse(id, result)
                        } catch (e: Exception) {
                            Logger.e("MCP JSON-RPC: Tool call error for $toolName", e)
                            buildJsonRpcError(id, -32603, "Tool execution error: ${e.message}")
                        }
                    }
                }

                "ping" -> {
                    buildJsonRpcResponse(id, buildJsonObject {})
                }

                else -> {
                    buildJsonRpcError(id, -32601, "Method not found: $method")
                }
            }
        } catch (e: Exception) {
            Logger.e("MCP JSON-RPC: Parse error", e)
            buildJsonRpcError(JsonNull, -32700, "Parse error: ${e.message}")
        }
    }

    private fun buildJsonRpcResponse(id: JsonElement?, result: JsonObject): String {
        val response = buildJsonObject {
            put("jsonrpc", "2.0")
            if (id != null && id != JsonNull) put("id", id)
            put("result", result)
        }
        return json.encodeToString(response)
    }

    private fun buildJsonRpcError(id: JsonElement?, code: Int, message: String): String {
        val response = buildJsonObject {
            put("jsonrpc", "2.0")
            if (id != null && id != JsonNull) put("id", id)
            put("error", buildJsonObject {
                put("code", code)
                put("message", message)
            })
        }
        return json.encodeToString(response)
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
        // Record tool call if recording is active
        val argsMap = args.entries.associate { entry ->
            entry.key to (entry.value.jsonPrimitive.contentOrNull ?: entry.value.toString())
        }
        sessionRecorder.recordCommand(tool, argsMap)

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

                // ===== Phase 3: Vault Tools =====
                "vault_list_profiles" -> {
                    if (!biometricAuth.requireUnlock()) {
                        """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
                    } else {
                        json.encodeToString(profileManager.listProfiles())
                    }
                }

                "vault_use_profile" -> {
                    if (!biometricAuth.requireUnlock()) {
                        """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
                    } else {
                        val profileId = args["profileId"]?.jsonPrimitive?.content ?: ""
                        val profile = profileManager.getProfile(profileId)
                        if (profile != null) {
                            val totpCode = profile.encryptedTotpSeed?.let { seed ->
                                try {
                                    val decryptedSeed = com.vayu.agenticbrowser.vault.CryptoUtils.decrypt(
                                        seed, "vayu_vault_key"
                                    )
                                    TotpGenerator.generate(decryptedSeed)
                                } catch (e: Exception) { null }
                            }
                            val safeProfile = profile.copy(
                                encryptedPassword = "***",
                                encryptedBackupCodes = null
                            )
                            val profileJson = json.encodeToString(safeProfile).dropLast(1)
                            val totpJson = totpCode?.let { ""","totpCode":"$it","totpSecondsRemaining":${TotpGenerator.getSecondsRemaining()}""" } ?: ""
                            """${profileJson}$totpJson}"""
                        } else {
                            """{"error":"Profile $profileId not found"}"""
                        }
                    }
                }

                "vault_fill_login" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"error":"No WebView available"}"""
                    val siteUrl = args["siteUrl"]?.jsonPrimitive?.content ?: wv.url ?: ""
                    credentialVault.fillLoginForm(siteUrl, wv)
                }

                "vault_get_otp" -> {
                    if (!biometricAuth.requireUnlock()) {
                        """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
                    } else {
                        val useSms = args["sms"]?.jsonPrimitive?.booleanOrNull ?: false
                        if (useSms) {
                            val timeoutMs = args["timeoutMs"]?.jsonPrimitive?.longOrNull ?: 60_000L
                            val otp = smsOtpReader.readLatestOtp(timeoutMs)
                            if (otp != null) {
                                """{"otp":"$otp","source":"sms"}"""
                            } else {
                                """{"error":"No SMS OTP found within timeout"}"""
                            }
                        } else {
                            val profileId = args["profileId"]?.jsonPrimitive?.content ?: ""
                            val seed = credentialVault.getDecryptedTotpSeed(profileId)
                            if (seed != null) {
                                val code = TotpGenerator.generate(seed)
                                val remaining = TotpGenerator.getSecondsRemaining()
                                """{"otp":"$code","source":"totp","secondsRemaining":$remaining}"""
                            } else {
                                """{"error":"No TOTP seed found for profile $profileId"}"""
                            }
                        }
                    }
                }

                "vault_save_cookies" -> {
                    if (!biometricAuth.requireUnlock()) {
                        """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
                    } else {
                        val profileId = args["profileId"]?.jsonPrimitive?.content ?: ""
                        val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                        val wv = resolveWebView(tabId)
                            ?: return """{"error":"No WebView available"}"""

                        val cookiesResult = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                            wv.evaluateJavascript("document.cookie") { result -> cont.resume(result) {} }
                        }

                        val cookiesJson = cookiesResult?.let { """{"cookies":$it}""" } ?: "{}"
                        val profile = profileManager.getProfile(profileId)
                        if (profile != null) {
                            profileManager.saveProfile(profile.copy(savedCookiesJson = cookiesJson))
                            """{"success":true,"profileId":"$profileId"}"""
                        } else {
                            """{"error":"Profile $profileId not found"}"""
                        }
                    }
                }

                // ===== Phase 3: Form Tools =====
                "form_detect" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"error":"No WebView available"}"""
                    formDetector.detectForms(wv)
                }

                "form_fill" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"error":"No WebView available"}"""
                    val mappingObj = args["mapping"]?.jsonObject ?: buildJsonObject {}
                    val mapping = mappingObj.entries.associate { it.key to it.value.jsonPrimitive.content }
                    val submitSelector = args["submitSelector"]?.jsonPrimitive?.contentOrNull
                    formDetector.fillForm(mapping, submitSelector, wv)
                }

                // ===== Phase 3: Dialog Tools =====
                "dialog_detect" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"detected":false,"error":"No WebView available"}"""
                    dialogController.detectDialog(wv)
                }

                "dialog_accept" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"success":false,"error":"No WebView available"}"""
                    dialogController.acceptDialog(wv)
                }

                "dialog_dismiss" -> {
                    val tabId = args["tabId"]?.jsonPrimitive?.intOrNull
                    val wv = resolveWebView(tabId)
                        ?: return """{"success":false,"error":"No WebView available"}"""
                    dialogController.dismissDialog(wv)
                }

                // ===== Phase 4: Plugin Tools =====
                "plugin_list" -> {
                    val plugins = pluginRegistry.allPlugins.value
                    val result = plugins.map { p ->
                        mapOf(
                            "name" to p.name,
                            "version" to p.version,
                            "description" to p.description,
                            "enabled" to p.enabled.toString(),
                            "sites" to p.sites.joinToString(", "),
                            "toolCount" to p.tools.size.toString()
                        )
                    }
                    json.encodeToString(result)
                }

                "plugin_enable" -> {
                    val name = args["name"]?.jsonPrimitive?.content ?: ""
                    pluginRegistry.enablePlugin(name)
                    """{"success":true,"name":"$name","enabled":true}"""
                }

                "plugin_disable" -> {
                    val name = args["name"]?.jsonPrimitive?.content ?: ""
                    pluginRegistry.disablePlugin(name)
                    """{"success":true,"name":"$name","enabled":false}"""
                }

                // ===== Phase 4: Tunnel Tools =====
                "tunnel_start" -> {
                    val url = tunnelManager.startTunnel()
                    """{"success":true,"url":"$url"}"""
                }

                "tunnel_stop" -> {
                    tunnelManager.stopTunnel()
                    """{"success":true,"stopped":true}"""
                }

                "tunnel_get_url" -> {
                    val url = tunnelManager.tunnelUrl.value
                    if (url != null) {
                        """{"active":true,"url":"$url"}"""
                    } else {
                        """{"active":false,"url":null}"""
                    }
                }

                // ===== Phase 4: Recording Tools =====
                "recording_start" -> {
                    val name = args["name"]?.jsonPrimitive?.content ?: "Untitled"
                    sessionRecorder.startRecording(name)
                    """{"success":true,"name":"$name","recording":true}"""
                }

                "recording_stop" -> {
                    val recording = sessionRecorder.stopRecording()
                    json.encodeToString(recording)
                }

                "recording_list" -> {
                    val recordings = sessionRecorder.listRecordings()
                    json.encodeToString(recordings)
                }

                "recording_replay" -> {
                    val recordingId = args["id"]?.jsonPrimitive?.content ?: ""
                    sessionRecorder.replayRecording(recordingId) { toolName, toolArgs ->
                        handleRecordingReplayTool(toolName, toolArgs)
                    }
                }

                "recording_delete" -> {
                    val recordingId = args["id"]?.jsonPrimitive?.content ?: ""
                    sessionRecorder.deleteRecording(recordingId)
                    """{"success":true,"deleted":"$recordingId"}"""
                }

                // ===== Phase 4: Session Save/Load Tools =====
                "session_save" -> {
                    val sessionName = args["name"]?.jsonPrimitive?.content ?: "default"
                    saveSession(sessionName)
                }

                "session_load" -> {
                    val sessionName = args["name"]?.jsonPrimitive?.content ?: "default"
                    loadSession(sessionName)
                }

                // ===== Phase 4: User Agent Tool =====
                "user_agent_set" -> {
                    val userAgent = args["userAgent"]?.jsonPrimitive?.content ?: ""
                    val effectiveUa = StealthController.USER_AGENT_PRESETS[userAgent] ?: userAgent
                    val wv = resolveWebView(null)
                    if (wv != null) {
                        StealthController.setUserAgent(wv, effectiveUa)
                        // Apply to all tab WebViews
                        tabManager.tabs.value.forEach { tab ->
                            tabManager.getTab(tab.tabId)?.let { tabWv ->
                                StealthController.setUserAgent(tabWv, effectiveUa)
                            }
                        }
                        """{"success":true,"userAgent":"${effectiveUa.take(80)}..."}"""
                    } else {
                        """{"success":false,"error":"No WebView available"}"""
                    }
                }

                // ===== Phase 4: Stealth Tools =====
                "stealth_enable" -> {
                    val wv = resolveWebView(null)
                    if (wv != null) {
                        StealthController.applyStealthMode(wv)
                        tabManager.tabs.value.forEach { tab ->
                            tabManager.getTab(tab.tabId)?.let { tabWv ->
                                StealthController.applyStealthMode(tabWv)
                            }
                        }
                        """{"success":true,"stealthEnabled":true}"""
                    } else {
                        """{"success":false,"error":"No WebView available"}"""
                    }
                }

                "stealth_disable" -> {
                    val wv = resolveWebView(null)
                    if (wv != null) {
                        StealthController.removeStealthMode(wv)
                        tabManager.tabs.value.forEach { tab ->
                            tabManager.getTab(tab.tabId)?.let { tabWv ->
                                StealthController.removeStealthMode(tabWv)
                            }
                        }
                        """{"success":true,"stealthEnabled":false}"""
                    } else {
                        """{"success":false,"error":"No WebView available"}"""
                    }
                }

                // ===== Phase 4: Browser Info Tool =====
                "browser_info" -> {
                    val tunnelUrl = tunnelManager.tunnelUrl.value
                    val pluginCount = pluginRegistry.activePlugins.value.size
                    val tabCount = tabManager.tabs.value.size
                    val brainState = agentLoop.state.value.name
                    """{"appVersion":"1.0.0","tabCount":$tabCount,"connectedSince":${_connectedSince.value},"tunnelUrl":${if (tunnelUrl != null) "\"$tunnelUrl\"" else "null"},"pluginCount":$pluginCount,"platform":"android","networkConnected":${networkMonitor.isConnected.value},"brainState":"$brainState"}"""
                }

                // ===== Phase 5: Brain / Autonomous Agent Tools =====
                "brain_run" -> {
                    val goal = args["goal"]?.jsonPrimitive?.content ?: ""
                    if (goal.isBlank()) {
                        """{"error":"Goal is required"}"""
                    } else {
                        agentLoop.runGoal(goal)
                        """{"success":true,"goal":"${goal.replace("\"", "\\\"")}","state":"${agentLoop.state.value.name}"}"""
                    }
                }

                "brain_stop" -> {
                    agentLoop.stop()
                    """{"success":true,"state":"IDLE"}"""
                }

                "brain_status" -> {
                    val stats = agentLoop.getStats()
                    val stepLog = agentLoop.stepLog.value.takeLast(5).map { step ->
                        mapOf(
                            "index" to step.index,
                            "thought" to (step.thought?.take(100) ?: ""),
                            "tool" to (step.tool ?: ""),
                            "result" to (step.result?.take(100) ?: "")
                        )
                    }
                    json.encodeToString(mapOf(
                        "state" to stats["state"].toString(),
                        "currentGoal" to stats["currentGoal"].toString(),
                        "totalSteps" to stats["totalSteps"].toString(),
                        "totalTokens" to stats["totalTokens"].toString(),
                        "recentSteps" to stepLog
                    ))
                }

                "brain_config" -> {
                    val config = agentLoop.getConfig()
                    val hasApiKey = config.apiKey.isNotBlank()

                    // Check if any config updates were provided
                    val newProvider = args["provider"]?.jsonPrimitive?.contentOrNull
                    val newApiKey = args["apiKey"]?.jsonPrimitive?.contentOrNull
                    val newBaseUrl = args["baseUrl"]?.jsonPrimitive?.contentOrNull
                    val newModel = args["model"]?.jsonPrimitive?.contentOrNull
                    val newMaxTokens = args["maxTokens"]?.jsonPrimitive?.intOrNull

                    if (newProvider != null || newApiKey != null || newBaseUrl != null || newModel != null || newMaxTokens != null) {
                        val updatedConfig = config.copy(
                            provider = newProvider?.let {
                                try { com.vayu.agenticbrowser.brain.LlmProvider.valueOf(it) } catch (_: Exception) { config.provider }
                            } ?: config.provider,
                            apiKey = newApiKey ?: config.apiKey,
                            baseUrl = newBaseUrl ?: config.baseUrl,
                            model = newModel ?: config.model,
                            maxTokens = newMaxTokens ?: config.maxTokens,
                            enabled = (newApiKey ?: config.apiKey).isNotBlank()
                        )
                        agentLoop.updateConfig(updatedConfig)
                        """{"success":true,"provider":"${updatedConfig.provider.name}","model":"${updatedConfig.effectiveModel()}","hasApiKey":${updatedConfig.apiKey.isNotBlank()},"enabled":${updatedConfig.enabled}}"""
                    } else {
                        """{"provider":"${config.provider.name}","model":"${config.effectiveModel()}","baseUrl":"${config.effectiveBaseUrl()}","maxTokens":${config.maxTokens},"hasApiKey":$hasApiKey,"enabled":${config.enabled}}"""
                    }
                }

                "brain_list_goals" -> {
                    val goals = goalScheduler.listGoals()
                    json.encodeToString(goals.map { g ->
                        mapOf(
                            "id" to g.id,
                            "goal" to g.goal,
                            "scheduledAt" to g.scheduledAt,
                            "completed" to g.completed,
                            "recurring" to (g.recurringIntervalMs != null)
                        )
                    })
                }

                "brain_schedule" -> {
                    val goal = args["goal"]?.jsonPrimitive?.content ?: ""
                    val delayMinutes = args["delayMinutes"]?.jsonPrimitive?.intOrNull ?: 60
                    if (goal.isBlank()) {
                        """{"error":"Goal is required"}"""
                    } else {
                        val scheduledAt = System.currentTimeMillis() + (delayMinutes * 60_000L)
                        val scheduledGoal = goalScheduler.scheduleGoal(goal, scheduledAt)
                        """{"success":true,"id":"${scheduledGoal.id}","scheduledAt":$scheduledAt,"delayMinutes":$delayMinutes}"""
                    }
                }

                // ===== Phase 5: Workflow Tools =====
                "workflow_list" -> {
                    val engine = workflowEngine
                    if (engine == null) {
                        """{"error":"WorkflowEngine not initialized"}"""
                    } else {
                        val workflows = engine.listWorkflows()
                        json.encodeToString(workflows.map { w ->
                            mapOf(
                                "id" to w.id,
                                "name" to w.name,
                                "description" to w.description,
                                "goalPrompt" to w.goalPrompt,
                                "isBuiltIn" to w.isBuiltIn,
                                "schedule" to (w.schedule ?: "")
                            )
                        })
                    }
                }

                "workflow_run" -> {
                    val engine = workflowEngine
                    val workflowId = args["id"]?.jsonPrimitive?.content ?: ""
                    if (engine == null) {
                        """{"error":"WorkflowEngine not initialized"}"""
                    } else if (workflowId.isBlank()) {
                        """{"error":"Workflow ID is required"}"""
                    } else {
                        engine.runWorkflow(workflowId)
                    }
                }

                "workflow_save" -> {
                    val engine = workflowEngine
                    val name = args["name"]?.jsonPrimitive?.content ?: ""
                    val description = args["description"]?.jsonPrimitive?.content ?: ""
                    val goalPrompt = args["goalPrompt"]?.jsonPrimitive?.content ?: ""
                    if (engine == null) {
                        """{"error":"WorkflowEngine not initialized"}"""
                    } else if (name.isBlank() || goalPrompt.isBlank()) {
                        """{"error":"Name and goalPrompt are required"}"""
                    } else {
                        val workflow = com.vayu.agenticbrowser.brain.Workflow(
                            name = name,
                            description = description,
                            goalPrompt = goalPrompt
                        )
                        val saved = engine.saveWorkflow(workflow)
                        """{"success":true,"id":"${saved.id}","name":"${saved.name.replace("\"", "\\\"")}"}"""
                    }
                }

                // ===== Phase 6: SSH Tunnel Tools =====
                "ssh_tunnel_start" -> {
                    val host = args["host"]?.jsonPrimitive?.content ?: ""
                    val port = args["port"]?.jsonPrimitive?.intOrNull ?: 22
                    val username = args["username"]?.jsonPrimitive?.content ?: ""
                    val authType = args["authType"]?.jsonPrimitive?.contentOrNull ?: "password"
                    val password = args["password"]?.jsonPrimitive?.contentOrNull ?: ""
                    val privateKey = args["privateKey"]?.jsonPrimitive?.contentOrNull ?: ""
                    val remotePort = args["remotePort"]?.jsonPrimitive?.intOrNull ?: 8765
                    val localPort = args["localPort"]?.jsonPrimitive?.intOrNull ?: 8765

                    if (host.isBlank() || username.isBlank()) {
                        """{"error":"Host and username are required"}"""
                    } else {
                        // Save config for future use
                        sshTunnelManager.updateConfig(com.vayu.agenticbrowser.tunnel.SshTunnelConfig(
                            host = host, port = port, username = username,
                            authType = authType, password = password, privateKey = privateKey,
                            remotePort = remotePort, localPort = localPort
                        ))
                        sshTunnelManager.startTunnel(
                            host = host, port = port, username = username,
                            authType = authType, password = password, privateKey = privateKey,
                            remotePort = remotePort, localPort = localPort
                        )
                    }
                }

                "ssh_tunnel_stop" -> {
                    sshTunnelManager.stopTunnel()
                    """{"success":true,"stopped":true}"""
                }

                "ssh_tunnel_status" -> {
                    val running = sshTunnelManager.isRunning.value
                    val url = sshTunnelManager.tunnelUrl.value
                    val error = sshTunnelManager.lastError.value
                    val config = sshTunnelManager.config.value
                    val sseUrl = sshTunnelManager.getMcpEndpointUrl("sse")
                    val wsUrl = sshTunnelManager.getMcpEndpointUrl("ws")
                    """{"running":$running,"tunnelUrl":${if (url != null) "\"$url\"" else "null"},"sseEndpoint":${if (sseUrl != null) "\"$sseUrl\"" else "null"},"wsEndpoint":${if (wsUrl != null) "\"$wsUrl\"" else "null"},"lastError":${if (error != null) "\"${error.replace("\"", "\\\"")}\"" else "null"},"config":{"host":"${config.host}","port":${config.port},"username":"${config.username}","authType":"${config.authType}","remotePort":${config.remotePort},"localPort":${config.localPort}}}"""
                }

                "ssh_tunnel_config" -> {
                    val config = sshTunnelManager.config.value
                    // Check for config updates
                    val newHost = args["host"]?.jsonPrimitive?.contentOrNull
                    val newPort = args["port"]?.jsonPrimitive?.intOrNull
                    val newUsername = args["username"]?.jsonPrimitive?.contentOrNull
                    val newAuthType = args["authType"]?.jsonPrimitive?.contentOrNull
                    val newPassword = args["password"]?.jsonPrimitive?.contentOrNull
                    val newPrivateKey = args["privateKey"]?.jsonPrimitive?.contentOrNull
                    val newRemotePort = args["remotePort"]?.jsonPrimitive?.intOrNull
                    val newLocalPort = args["localPort"]?.jsonPrimitive?.intOrNull

                    if (newHost != null || newUsername != null || newPassword != null || newPrivateKey != null) {
                        val updatedConfig = config.copy(
                            host = newHost ?: config.host,
                            port = newPort ?: config.port,
                            username = newUsername ?: config.username,
                            authType = newAuthType ?: config.authType,
                            password = newPassword ?: config.password,
                            privateKey = newPrivateKey ?: config.privateKey,
                            remotePort = newRemotePort ?: config.remotePort,
                            localPort = newLocalPort ?: config.localPort
                        )
                        sshTunnelManager.updateConfig(updatedConfig)
                        """{"success":true,"host":"${updatedConfig.host}","port":${updatedConfig.port},"username":"${updatedConfig.username}","authType":"${updatedConfig.authType}","remotePort":${updatedConfig.remotePort},"localPort":${updatedConfig.localPort}}"""
                    } else {
                        """{"host":"${config.host}","port":${config.port},"username":"${config.username}","authType":"${config.authType}","remotePort":${config.remotePort},"localPort":${config.localPort},"hasPassword":${config.password.isNotBlank()},"hasPrivateKey":${config.privateKey.isNotBlank()}}"""
                    }
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

    private suspend fun handleRecordingReplayTool(tool: String, args: Map<String, String>): String {
        val jsonArgs = buildJsonObject {
            args.forEach { (key, value) ->
                put(key, value)
            }
        }
        return try {
            when (tool) {
                "browser_navigate" -> domController.navigate(args["url"] ?: "", args["tabId"]?.toIntOrNull())
                "browser_query_selector" -> domController.querySelector(args["selector"] ?: "", args["all"]?.toBoolean() ?: false, args["tabId"]?.toIntOrNull())
                "browser_click" -> domController.click(args["selector"] ?: "", args["index"]?.toIntOrNull() ?: 0, args["tabId"]?.toIntOrNull())
                "browser_type" -> domController.type(args["selector"] ?: "", args["text"] ?: "", args["clearFirst"]?.toBoolean() ?: false, args["tabId"]?.toIntOrNull())
                "browser_evaluate" -> domController.evaluate(args["script"] ?: "", args["tabId"]?.toIntOrNull())
                else -> """{"replayed":true,"tool":"$tool"}"""
            }
        } catch (e: Exception) {
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    private fun saveSession(name: String): String {
        val ctx = appContext ?: return """{"error":"No context available"}"""
        val prefs = ctx.getSharedPreferences("vayu_sessions", Context.MODE_PRIVATE)
        val tabs = tabManager.tabs.value

        val sessionData = mutableMapOf<String, String>()
        tabs.forEachIndexed { index, tabState ->
            sessionData["tab_${index}_url"] = tabState.url
            sessionData["tab_${index}_title"] = tabState.title
        }
        sessionData["tab_count"] = tabs.size.toString()

        prefs.edit().apply {
            putString("session_${name}_count", tabs.size.toString())
            tabs.forEachIndexed { index, tabState ->
                putString("session_${name}_tab_${index}_url", tabState.url)
                putString("session_${name}_tab_${index}_title", tabState.title)
            }
            apply()
        }

        Logger.i("Session saved: $name with ${tabs.size} tabs")
        return """{"success":true,"name":"$name","tabCount":${tabs.size}}"""
    }

    private fun loadSession(name: String): String {
        val ctx = appContext ?: return """{"error":"No context available"}"""
        val prefs = ctx.getSharedPreferences("vayu_sessions", Context.MODE_PRIVATE)
        val tabCount = prefs.getString("session_${name}_count", "0")?.toIntOrNull() ?: 0

        if (tabCount == 0) {
            return """{"error":"Session '$name' not found"}"""
        }

        // Close existing tabs
        val existingTabs = tabManager.tabs.value.toList()
        existingTabs.forEach { tabManager.closeTab(it.tabId) }

        // Restore tabs
        for (i in 0 until tabCount) {
            val url = prefs.getString("session_${name}_tab_${i}_url", "") ?: ""
            val background = i > 0
            if (url.isNotEmpty()) {
                tabManager.newTab(url, background)
            }
        }

        Logger.i("Session loaded: $name with $tabCount tabs")
        return """{"success":true,"name":"$name","tabCount":$tabCount}"""
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
        return WebViewManager.getInstance().getWebView()
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

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
