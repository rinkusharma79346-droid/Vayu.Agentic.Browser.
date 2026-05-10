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
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.response.*
import io.ktor.server.request.receiveText
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
    private val networkMonitor: NetworkMonitor
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

    /** Server port — exposed for UI display */
    val port: Int = 8765

    /** Server name — exposed for UI display */
    val serverName: String = "VAYU MCP Server"

    /** SSE clients for streaming responses — each client has a channel to receive SSE data */
    private val sseClients = ConcurrentHashMap<String, Channel<String>>()

    /** Number of connected SSE clients */
    val sseClientCount: Int get() = sseClients.size

    /**
     * Get the local IP address for display purposes.
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = java.util.Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {}
        return "0.0.0.0"
    }

    /** WebSocket URL for MCP clients */
    fun getWsUrl(): String = "ws://${getLocalIpAddress()}:$port/mcp"

    /** SSE endpoint URL for Claude AI */
    fun getSseUrl(): String = "http://${getLocalIpAddress()}:$port/sse"

    /** HTTP message endpoint for Claude AI (MCP standard) */
    fun getMessageUrl(): String = "http://${getLocalIpAddress()}:$port/message"

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

        Logger.i("Starting MCP Server on port $port (WebSocket + SSE)")

        server = embeddedServer(CIO, port = port) {
            install(WebSockets)

            routing {
                // ===== WebSocket endpoint (existing) =====
                webSocket("/mcp") {
                    Logger.i("New MCP WebSocket client connection")
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
                        _connectedSince.value = System.currentTimeMillis()
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

                // ===== SSE endpoint for Claude AI (manual SSE implementation) =====
                get("/sse") {
                    Logger.i("New MCP SSE client connection")
                    val clientId = UUID.randomUUID().toString()

                    // Create a channel for this client to receive messages
                    val channel = Channel<String>(Channel.BUFFERED)
                    sseClients[clientId] = channel

                    _connectedSince.value = System.currentTimeMillis()

                    call.respondOutputStream(contentType = ContentType.Text.EventStream) {
                        val writer = java.io.OutputStreamWriter(this)
                        try {
                            // Send endpoint event (MCP protocol: client should POST to /message)
                            writer.write("event: endpoint\n")
                            writer.write("data: /message?clientId=$clientId\n\n")
                            writer.flush()

                            // Send messages from channel as SSE events
                            for (data in channel) {
                                writer.write("event: message\n")
                                writer.write("data: $data\n\n")
                                writer.flush()
                            }
                        } catch (_: java.io.IOException) {
                            // Client disconnected
                        } finally {
                            sseClients.remove(clientId)
                            writer.close()
                            Logger.i("MCP SSE client disconnected: $clientId")
                        }
                    }
                }

                // ===== HTTP POST message endpoint for Claude AI (SSE transport) =====
                post("/message") {
                    val clientId = call.parameters["clientId"] ?: run {
                        call.respondText(
                            """{"error":"clientId parameter required"}""",
                            ContentType.Application.Json,
                            HttpStatusCode.BadRequest
                        )
                        return@post
                    }

                    val rawBody = call.receiveText()
                    Logger.d("SSE message from client $clientId: $rawBody")

                    // Process the MCP message
                    val response = handleMessage(rawBody)

                    // Push the response via SSE channel to the client
                    val sseChannel = sseClients[clientId]
                    if (sseChannel != null) {
                        try {
                            sseChannel.send(response)
                        } catch (e: Exception) {
                            Logger.e("Failed to send SSE response to client $clientId", e)
                        }
                        call.respondText("""{"status":"sent"}""", ContentType.Application.Json)
                    } else {
                        // If no SSE channel, return the response directly via HTTP
                        call.respondText(response, ContentType.Application.Json)
                    }
                }

                // ===== Server info endpoint (useful for discovery) =====
                get("/info") {
                    val tunnelUrl = tunnelManager.tunnelUrl.value
                    val info = buildJsonObject {
                        put("name", serverName)
                        put("version", "1.0.0")
                        put("port", port)
                        put("wsUrl", getWsUrl())
                        put("sseUrl", getSseUrl())
                        put("messageUrl", getMessageUrl())
                        put("pairingCodeRequired", true)
                        put("toolCount", ToolRegistry.toJson().let {
                            json.parseToJsonElement(it).jsonArray.size
                        })
                        if (tunnelUrl != null) {
                            put("tunnelUrl", tunnelUrl)
                        }
                    }
                    call.respondText(json.encodeToString(info), ContentType.Application.Json)
                }
            }
        }.start(wait = false)

        _isRunning.value = true
        Logger.i("MCP Server started on port $port — WS: /mcp, SSE: /sse, HTTP: /message, Info: /info")
    }

    fun stop() {
        sseClients.clear()
        server?.stop(1000, 2000)
        server = null
        _isRunning.value = false
        _connectedSince.value = 0L
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
