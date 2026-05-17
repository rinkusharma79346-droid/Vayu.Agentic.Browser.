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
    val maxTokens: Int = 8192,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val enabled: Boolean = false
) {
    fun effectiveBaseUrl(): String = if (baseUrl.isNotBlank()) baseUrl else providerDefaults[provider]?.first ?: ""

    fun effectiveModel(): String = if (model.isNotBlank()) model else providerDefaults[provider]?.second ?: ""

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """You are VAYU, an autonomous AI agent controlling an Android browser. You have access to browser tools (navigate, click, type, evaluate JS), tab management, downloads, screenshots, form filling, dialog handling, credential vault, plugins, tunnels, and session management.

Your workflow:
1. Analyze the user's goal
2. Plan a sequence of tool calls
3. Execute tools one at a time
4. Observe results and adapt your plan
5. Repeat until the goal is achieved or you determine it's impossible

Rules:
- Always wait for page loads after navigation
- Use wait_for_selector or wait_for_text before interacting with dynamic content
- Handle dialogs and cookie banners proactively
- Use vault_fill_login for login flows when credentials exist
- Save important results using session_save
- Report progress clearly in your thoughts
- If a tool fails, try an alternative approach
- Never ask the user for help - use available tools to solve problems autonomously"""

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
