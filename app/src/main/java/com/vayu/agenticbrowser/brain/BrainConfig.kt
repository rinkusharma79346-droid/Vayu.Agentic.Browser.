package com.vayu.agenticbrowser.brain

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.vault.CryptoUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class LlmProvider {
    GEMINI,
    GROQ,
    OPENROUTER,
    CUSTOM
}

@Serializable
data class BrainConfig(
    val provider: LlmProvider = LlmProvider.GEMINI,
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val maxTokens: Int = 4096,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val enabled: Boolean = false
) {
    fun effectiveBaseUrl(): String = if (baseUrl.isNotBlank()) baseUrl else providerDefaults[provider]?.first ?: ""

    fun effectiveModel(): String = if (model.isNotBlank()) model else providerDefaults[provider]?.second ?: ""

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """You are VAYU, an autonomous AI agent controlling an Android browser. Execute the user's goal using available tools.

Workflow: Analyze goal → Plan tool calls → Execute → Observe results → Adapt → Repeat until done.

Rules:
- Wait for page loads after navigation
- Use wait_for_selector before interacting with dynamic content
- Handle dialogs/cookie banners proactively
- If a tool fails, try an alternative approach
- Be concise in your thoughts
- Never ask for help — solve problems autonomously with available tools"""

        val providerDefaults: Map<LlmProvider, Pair<String, String>> = mapOf(
            LlmProvider.GEMINI to Pair(
                "https://generativelanguage.googleapis.com/v1beta/openai",
                "gemini-2.0-flash"
            ),
            LlmProvider.GROQ to Pair(
                "https://api.groq.com/openai/v1",
                "llama-3.3-70b-versatile"
            ),
            LlmProvider.OPENROUTER to Pair(
                "https://openrouter.ai/api/v1",
                "google/gemini-2.5-pro"
            ),
            LlmProvider.CUSTOM to Pair("", "")
        )

        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        private const val PREFS_NAME = "vayu_brain_config"
        private const val KEY_CONFIG = "brain_config_encrypted"

        fun load(ctx: Context): BrainConfig {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val encrypted = prefs.getString(KEY_CONFIG, null) ?: return BrainConfig()

            return try {
                val decrypted = CryptoUtils.decrypt(encrypted, "vayu_brain_key")
                json.decodeFromString<BrainConfig>(decrypted)
            } catch (e: Exception) {
                Logger.e("Failed to load brain config", e)
                BrainConfig()
            }
        }

        fun save(ctx: Context, config: BrainConfig) {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = json.encodeToString(config)
            val encrypted = CryptoUtils.encrypt(jsonStr, "vayu_brain_key")
            prefs.edit().putString(KEY_CONFIG, encrypted).apply()
            Logger.i("Brain config saved: provider=${config.provider}, model=${config.effectiveModel()}")
        }
    }
}
