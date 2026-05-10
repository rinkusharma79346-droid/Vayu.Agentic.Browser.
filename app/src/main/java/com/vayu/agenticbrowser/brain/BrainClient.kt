package com.vayu.agenticbrowser.brain

import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class ChatMessage(
    val role: String,
    val content: String? = null,
    val toolCallId: String? = null,
    val toolCalls: List<ToolCall>? = null,
    val name: String? = null
)

@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: String
)

@Serializable
data class BrainResponse(
    val content: String? = null,
    val toolCalls: List<ToolCall>? = null,
    val finishReason: String = "",
    val usage: TokenUsage? = null
)

@Serializable
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

data class OpenAiFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

class BrainClient {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun chat(
        config: BrainConfig,
        messages: List<ChatMessage>,
        tools: List<OpenAiFunction>
    ): BrainResponse = withContext(Dispatchers.IO) {
        try {
            val requestJson = buildRequestJson(config, messages, tools)
            val baseUrl = config.effectiveBaseUrl().trimEnd('/')
            val url = "$baseUrl/chat/completions"

            Logger.d("BrainClient: POST $url model=${config.effectiveModel()}")

            val requestBody = requestJson.toRequestBody("application/json".toMediaType())

            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")

            val apiKey = config.apiKey
            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                if (config.provider == LlmProvider.OPENROUTER) {
                    requestBuilder.addHeader("HTTP-Referer", "https://vayu.agenticbrowser.app")
                    requestBuilder.addHeader("X-Title", "VAYU Agentic Browser")
                }
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                val errorMsg = "API error ${response.code}: ${responseBody?.take(500)}"
                Logger.e("BrainClient: $errorMsg")
                return@withContext BrainResponse(
                    content = errorMsg,
                    finishReason = "error"
                )
            }

            parseResponse(responseBody)
        } catch (e: Exception) {
            Logger.e("BrainClient request failed", e)
            BrainResponse(
                content = "Request failed: ${e.message}",
                finishReason = "error"
            )
        }
    }

    private fun buildRequestJson(
        config: BrainConfig,
        messages: List<ChatMessage>,
        tools: List<OpenAiFunction>
    ): String {
        val messagesArray = buildJsonArray {
            for (msg in messages) {
                add(buildJsonObject {
                    put("role", msg.role)
                    msg.content?.let { put("content", it) }
                    msg.name?.let { put("name", it) }
                    msg.toolCallId?.let { put("tool_call_id", it) }
                    if (msg.toolCalls != null && msg.toolCalls.isNotEmpty()) {
                        put("tool_calls", buildJsonArray {
                            for (tc in msg.toolCalls) {
                                add(buildJsonObject {
                                    put("id", tc.id)
                                    put("type", "function")
                                    put("function", buildJsonObject {
                                        put("name", tc.name)
                                        put("arguments", tc.arguments)
                                    })
                                })
                            }
                        })
                    }
                })
            }
        }

        val requestObj = buildJsonObject {
            put("model", config.effectiveModel())
            put("messages", messagesArray)
            put("max_tokens", config.maxTokens)
            put("stream", false)

            if (tools.isNotEmpty()) {
                put("tools", buildJsonArray {
                    for (tool in tools) {
                        add(buildJsonObject {
                            put("type", "function")
                            put("function", buildJsonObject {
                                put("name", tool.name)
                                put("description", tool.description)
                                put("parameters", tool.parameters)
                            })
                        })
                    }
                })
            }
        }

        return requestObj.toString()
    }

    private fun parseResponse(body: String): BrainResponse {
        return try {
            val root = json.parseToJsonElement(body).jsonObject
            val choice = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
            val message = choice?.get("message")?.jsonObject

            val content = message?.get("content")?.jsonPrimitive?.contentOrNull
            val finishReason = choice?.get("finish_reason")?.jsonPrimitive?.contentOrNull ?: ""

            val toolCalls = message?.get("tool_calls")?.jsonArray?.map { tcElement ->
                val tc = tcElement.jsonObject
                val func = tc["function"]?.jsonObject
                ToolCall(
                    id = tc["id"]?.jsonPrimitive?.content ?: "",
                    name = func?.get("name")?.jsonPrimitive?.content ?: "",
                    arguments = func?.get("arguments")?.jsonPrimitive?.content ?: "{}"
                )
            }

            val usage = root["usage"]?.jsonObject?.let { u ->
                TokenUsage(
                    promptTokens = u["prompt_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                    completionTokens = u["completion_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                    totalTokens = u["total_tokens"]?.jsonPrimitive?.intOrNull ?: 0
                )
            }

            BrainResponse(
                content = content,
                toolCalls = toolCalls,
                finishReason = finishReason,
                usage = usage
            )
        } catch (e: Exception) {
            Logger.e("BrainClient: Failed to parse response", e)
            BrainResponse(
                content = "Failed to parse LLM response: ${e.message}",
                finishReason = "error"
            )
        }
    }
}
