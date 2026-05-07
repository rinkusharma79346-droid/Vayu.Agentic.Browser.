package com.vayu.agenticbrowser.agent

import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.engine.DomController
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
    private val domController: DomController
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
                "browser_navigate" -> {
                    val url = args["url"]?.jsonPrimitive?.content ?: ""
                    domController.navigate(url)
                }

                "browser_query_selector" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val all = args["all"]?.jsonPrimitive?.booleanOrNull ?: false
                    domController.querySelector(selector, all)
                }

                "browser_click" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val index = args["index"]?.jsonPrimitive?.intOrNull ?: 0
                    domController.click(selector, index)
                }

                "browser_type" -> {
                    val selector = args["selector"]?.jsonPrimitive?.content ?: ""
                    val text = args["text"]?.jsonPrimitive?.content ?: ""
                    val clearFirst = args["clearFirst"]?.jsonPrimitive?.booleanOrNull ?: false
                    domController.type(selector, text, clearFirst)
                }

                "browser_evaluate" -> {
                    val script = args["script"]?.jsonPrimitive?.content ?: ""
                    domController.evaluate(script)
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
