package com.vayu.agenticbrowser.agent

import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * RelayClient connects the VAYU Android browser to the Render MCP Relay Server.
 *
 * Architecture:
 *   Claude AI ──SSE──▸ Render Relay Server ◂──WebSocket──▸ RelayClient (this) ──▸ McpServer (local)
 *
 * When Claude sends a tool call via SSE to the Render server, the server relays it
 * here via WebSocket. RelayClient forwards it to the local McpServer for execution,
 * then sends the result back to the Render server, which pushes it to Claude via SSE.
 */
class RelayClient(
    private val mcpServer: McpServer
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var webSocket: WebSocket? = null
    private var httpClient: OkHttpClient? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Connection state */
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    /** Connection status detail */
    private val _status = MutableStateFlow(RelayStatus.DISCONNECTED)
    val status: StateFlow<RelayStatus> = _status.asStateFlow()

    /** Last error message */
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    /** Whether auto-reconnect is enabled */
    private var autoReconnect = true

    /** Reconnect attempts counter */
    private var reconnectAttempts = 0

    /** Maximum reconnect attempts before giving up */
    private val maxReconnectAttempts = 10

    /** Heartbeat job */
    private var heartbeatJob: Job? = null

    /** Reconnect job */
    private var reconnectJob: Job? = null

    /**
     * Connect to the Render MCP Relay Server via WebSocket.
     */
    fun connect(relayUrl: String = buildRelayUrl()) {
        if (_connected.value) {
            Logger.w("RelayClient: Already connected")
            return
        }

        _status.value = RelayStatus.CONNECTING
        _lastError.value = null
        reconnectAttempts = 0

        Logger.i("RelayClient: Connecting to $relayUrl")

        httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(relayUrl)
            .build()

        webSocket = httpClient?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Logger.i("RelayClient: WebSocket connection opened")
                _status.value = RelayStatus.AUTHENTICATING
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    handleServerMessage(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Logger.i("RelayClient: Server closing connection (code=$code)")
                webSocket.close(1000, "Goodbye")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Logger.i("RelayClient: Connection closed (code=$code)")
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Logger.e("RelayClient: Connection failure: ${t.message}", t)
                _lastError.value = t.message
                handleDisconnect()
            }
        })
    }

    /**
     * Disconnect from the relay server.
     */
    fun disconnect() {
        autoReconnect = false
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        _connected.value = false
        _status.value = RelayStatus.DISCONNECTED
        Logger.i("RelayClient: Disconnected")
    }

    /**
     * Handle incoming message from the relay server.
     */
    private suspend fun handleServerMessage(text: String) {
        try {
            val message = Json.parseToJsonElement(text).jsonObject
            val type = message["type"]?.jsonPrimitive?.content ?: return

            when (type) {
                "auth_challenge" -> handleAuthChallenge(message)
                "auth_success" -> handleAuthSuccess(message)
                "auth_failure" -> handleAuthFailure(message)
                "relay_request" -> handleRelayRequest(message)
                "heartbeat_ack" -> {
                    // Heartbeat acknowledged
                }
                else -> {
                    Logger.w("RelayClient: Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Logger.e("RelayClient: Failed to handle server message", e)
        }
    }

    /**
     * Handle auth challenge from the server.
     */
    private fun handleAuthChallenge(message: JsonObject) {
        val nonce = message["nonce"]?.jsonPrimitive?.content ?: return
        val hash = sha256Hex(McpConfig.PAIRING_CODE + nonce)

        val response = json.encodeToString(AuthResponse(
            type = "auth_response",
            hash = hash,
            metadata = RelayMetadata(
                app = "VAYU Agentic Browser",
                version = McpConfig.APP_VERSION,
                localPort = mcpServer.port,
                localIp = mcpServer.getLocalIpAddress(),
                toolCount = 47,
            )
        ))

        webSocket?.send(response)
        Logger.d("RelayClient: Sent auth_response")
    }

    /**
     * Handle successful authentication.
     */
    private fun handleAuthSuccess(message: JsonObject) {
        val browserId = message["browserId"]?.jsonPrimitive?.content ?: "unknown"
        _connected.value = true
        _status.value = RelayStatus.CONNECTED
        reconnectAttempts = 0
        Logger.i("RelayClient: Authenticated with relay server (browserId=$browserId)")

        sendToolList()
        startHeartbeat()
    }

    /**
     * Handle authentication failure.
     */
    private fun handleAuthFailure(message: JsonObject) {
        val error = message["error"]?.jsonPrimitive?.content ?: "Unknown auth error"
        _connected.value = false
        _status.value = RelayStatus.FAILED
        _lastError.value = "Auth failed: $error"
        Logger.e("RelayClient: Auth failed: $error")
        disconnect()
    }

    /**
     * Handle a relay request from the server.
     * Forward to local McpServer and send response back.
     */
    private suspend fun handleRelayRequest(message: JsonObject) {
        val requestId = message["requestId"]?.jsonPrimitive?.content ?: return
        val payload = message["payload"]?.jsonPrimitive?.content ?: "{}"

        Logger.d("RelayClient: Relay request received (requestId=$requestId)")

        try {
            val response = mcpServer.handleMessage(payload)

            val relayResponse = json.encodeToString(RelayResponse(
                type = "relay_response",
                requestId = requestId,
                payload = response,
            ))

            webSocket?.send(relayResponse)
            Logger.d("RelayClient: Sent relay response (requestId=$requestId)")
        } catch (e: Exception) {
            Logger.e("RelayClient: Failed to handle relay request", e)
            val errorResponse = json.encodeToString(RelayResponse(
                type = "relay_response",
                requestId = requestId,
                payload = """{"error":"${e.message?.replace("\"", "\\\"")}"}""",
            ))
            webSocket?.send(errorResponse)
        }
    }

    /**
     * Send the local tool list to the relay server.
     */
    private fun sendToolList() {
        try {
            val toolsJson = ToolRegistry.toJson()
            val toolsArray = Json.parseToJsonElement(toolsJson).jsonArray
            val toolStrings = toolsArray.map { element -> element.toString() }

            val msg = json.encodeToString(ToolListMessage(
                type = "tool_list",
                tools = toolStrings,
            ))
            webSocket?.send(msg)
            Logger.d("RelayClient: Sent tool list to relay server")
        } catch (e: Exception) {
            Logger.e("RelayClient: Failed to send tool list", e)
        }
    }

    /**
     * Start heartbeat to keep connection alive.
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && _connected.value) {
                delay(McpConfig.RECONNECT_DELAY_MS)
                try {
                    val heartbeat = json.encodeToString(HeartbeatMessage(type = "heartbeat"))
                    webSocket?.send(heartbeat)
                } catch (e: Exception) {
                    Logger.e("RelayClient: Heartbeat failed", e)
                }
            }
        }
    }

    /**
     * Handle disconnection — attempt auto-reconnect.
     */
    private fun handleDisconnect() {
        _connected.value = false
        _status.value = RelayStatus.DISCONNECTED
        heartbeatJob?.cancel()

        if (autoReconnect && reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            val delayMs = minOf(
                McpConfig.RECONNECT_DELAY_MS * reconnectAttempts,
                60_000L
            )
            Logger.i("RelayClient: Reconnecting in ${delayMs}ms (attempt $reconnectAttempts/$maxReconnectAttempts)")

            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                delay(delayMs)
                connect()
            }
        } else if (reconnectAttempts >= maxReconnectAttempts) {
            _status.value = RelayStatus.FAILED
            _lastError.value = "Max reconnect attempts reached"
            Logger.e("RelayClient: Max reconnect attempts reached")
        }
    }

    /**
     * Build the WebSocket relay URL from the Render SSE URL.
     */
    private fun buildRelayUrl(): String {
        val sseUrl = McpConfig.RENDER_SSE_URL
        return sseUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .replace(Regex("/sse$"), "/relay")
    }

    /**
     * Broadcast a message to all SSE clients via the relay server.
     */
    fun broadcastToSseClients(data: String) {
        if (!_connected.value) return
        try {
            val msg = json.encodeToString(SseBroadcastMessage(
                type = "sse_broadcast",
                data = data,
            ))
            webSocket?.send(msg)
        } catch (e: Exception) {
            Logger.e("RelayClient: Failed to broadcast", e)
        }
    }

    companion object {
        private fun sha256Hex(input: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}

/**
 * Relay connection status.
 */
enum class RelayStatus {
    DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    FAILED
}

// ─── Message data classes ────────────────────────────────────────────────────

@Serializable
data class AuthResponse(
    val type: String,
    val hash: String,
    val metadata: RelayMetadata? = null
)

@Serializable
data class RelayMetadata(
    val app: String = "",
    val version: String = "",
    val localPort: Int = 0,
    val localIp: String = "",
    val toolCount: Int = 0
)

@Serializable
data class RelayResponse(
    val type: String,
    val requestId: String,
    val payload: String
)

@Serializable
data class ToolListMessage(
    val type: String,
    val tools: List<String>
)

@Serializable
data class HeartbeatMessage(
    val type: String
)

@Serializable
data class SseBroadcastMessage(
    val type: String,
    val data: String
)
