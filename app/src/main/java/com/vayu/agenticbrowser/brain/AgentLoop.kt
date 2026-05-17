package com.vayu.agenticbrowser.brain

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import com.vayu.agenticbrowser.plugins.PluginRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class AgentState {
    IDLE, THINKING, EXECUTING, WAITING, COMPLETED, FAILED
}

@Serializable
data class AgentStep(
    val index: Int,
    val thought: String? = null,
    val tool: String? = null,
    val args: String? = null,
    val result: String? = null,
    val timestamp: Long
)

class AgentLoop(
    private val context: Context,
    private val brainClient: BrainClient,
    private val pluginRegistry: PluginRegistry,
    private val toolExecutor: suspend (tool: String, args: Map<String, String>) -> String
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _currentGoal = MutableStateFlow<String?>(null)
    val currentGoal: StateFlow<String?> = _currentGoal.asStateFlow()

    private val _stepLog = MutableStateFlow<List<AgentStep>>(emptyList())
    val stepLog: StateFlow<List<AgentStep>> = _stepLog.asStateFlow()

    private val _totalTokens = MutableStateFlow(0)
    val totalTokens: StateFlow<Int> = _totalTokens.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    private var job: Job? = null
    private var config: BrainConfig = BrainConfig.load(context)

    private val maxIterations = 50

    fun updateConfig(newConfig: BrainConfig) {
        config = newConfig
        BrainConfig.save(context, newConfig)
    }

    fun getConfig(): BrainConfig = config

    /** Error message exposed for UI feedback */
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun runGoal(goal: String) {
        // Allow starting from IDLE or stuck states (COMPLETED/FAILED)
        if (_state.value != AgentState.IDLE && _state.value != AgentState.COMPLETED && _state.value != AgentState.FAILED) {
            Logger.w("AgentLoop: Cannot start new goal — state is ${_state.value}")
            return
        }

        // Cancel any previous job if still running
        job?.cancel()
        job = null

        // Validate API key before starting
        _lastError.value = null
        if (config.apiKey.isBlank()) {
            val msg = "No API key configured. Go to Configuration below, select a provider (GEMINI/GROQ/OPENROUTER), enter your API key, and click Save."
            Logger.e("AgentLoop: $msg")
            _lastError.value = msg
            _state.value = AgentState.FAILED
            _currentGoal.value = goal
            addStep(AgentStep(
                index = 0,
                thought = msg,
                timestamp = System.currentTimeMillis()
            ))
            // Reset to IDLE after a longer delay so user can see the error
            // and understand they need to configure the API key
            CoroutineScope(Dispatchers.Main).launch {
                delay(5000)
                if (_state.value == AgentState.FAILED && _lastError.value?.contains("API key") == true) {
                    _state.value = AgentState.IDLE
                }
            }
            return
        }

        _currentGoal.value = goal
        _stepLog.value = emptyList()
        _totalTokens.value = 0
        _totalSteps.value = 0

        // Set state to THINKING IMMEDIATELY so callers see the transition
        _state.value = AgentState.THINKING

        job = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            executeGoal(goal)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _state.value = AgentState.IDLE
        _currentGoal.value = null
        Logger.i("AgentLoop: Stopped by user")
    }

    private suspend fun executeGoal(goal: String) {
        Logger.i("AgentLoop: Starting goal: $goal")
        // State is already set to THINKING by runGoal() — no need to set again here
        // This eliminates the race condition where MCP clients see IDLE

        val messageHistory = mutableListOf<ChatMessage>()
        messageHistory.add(
            ChatMessage(role = "system", content = config.systemPrompt)
        )
        messageHistory.add(
            ChatMessage(role = "user", content = goal)
        )

        val availableTools = ToolAdapter.convertToOpenAiFunctions(pluginRegistry)

        var iteration = 0

        while (iteration < maxIterations) {
            iteration++
            _totalSteps.value = iteration

            if (!isActive()) {
                _state.value = AgentState.FAILED
                return
            }

            // Call LLM
            _state.value = AgentState.THINKING
            addStep(AgentStep(
                index = iteration,
                thought = "Thinking...",
                timestamp = System.currentTimeMillis()
            ))

            val response = try {
                brainClient.chat(config, messageHistory, availableTools)
            } catch (e: Exception) {
                Logger.e("AgentLoop: LLM call failed", e)
                _state.value = AgentState.FAILED
                _lastError.value = "LLM call failed: ${e.message}"
                addStep(AgentStep(
                    index = iteration,
                    thought = "LLM call failed: ${e.message}",
                    timestamp = System.currentTimeMillis()
                ))
                autoResetToIdle(15000)
                return
            }

            // Track token usage
            response.usage?.let { usage ->
                _totalTokens.value += usage.totalTokens
            }

            // Add assistant message to history
            val assistantMessage = ChatMessage(
                role = "assistant",
                content = response.content,
                toolCalls = response.toolCalls
            )
            messageHistory.add(assistantMessage)

            // Update step with thought
            if (response.content != null) {
                updateLastStep { prev ->
                    prev.copy(thought = response.content.take(500))
                }
            }

            // Check if LLM returned an error
            if (response.finishReason == "error") {
                Logger.e("AgentLoop: LLM returned error: ${response.content}")
                _lastError.value = response.content
                _state.value = AgentState.FAILED
                updateLastStep { prev ->
                    prev.copy(thought = "LLM Error: ${response.content?.take(300) ?: "Unknown error"}")
                }
                autoResetToIdle(15000)
                return
            }

            // Check if done
            if (response.toolCalls == null || response.toolCalls.isEmpty()) {
                // Only mark COMPLETED if the response looks like a real completion,
                // not an API error masquerading as content
                val contentStr = response.content ?: ""
                val isErrorLike = contentStr.contains("API error", ignoreCase = true) ||
                        contentStr.contains("Request failed", ignoreCase = true) ||
                        contentStr.contains("401", ignoreCase = true) ||
                        contentStr.contains("403", ignoreCase = true) ||
                        contentStr.contains("Unauthorized", ignoreCase = true)

                if (isErrorLike) {
                    Logger.e("AgentLoop: LLM returned error-like content: $contentStr")
                    _lastError.value = contentStr
                    _state.value = AgentState.FAILED
                    updateLastStep { prev ->
                        prev.copy(thought = "LLM Error: ${contentStr.take(300)}")
                    }
                    autoResetToIdle(15000)
                    return
                }

                Logger.i("AgentLoop: No more tool calls — goal completed")
                _state.value = AgentState.COMPLETED
                addStep(AgentStep(
                    index = iteration,
                    thought = response.content ?: "Goal completed",
                    timestamp = System.currentTimeMillis()
                ))
                // Auto-reset to IDLE after 10 seconds so new goals can be started
                autoResetToIdle(10000)
                return
            }

            if (response.finishReason == "stop" || response.finishReason == "end_turn") {
                Logger.i("AgentLoop: LLM finished — goal completed")
                _state.value = AgentState.COMPLETED
                // Auto-reset to IDLE after 10 seconds so new goals can be started
                autoResetToIdle(10000)
                return
            }

            // Execute tool calls
            _state.value = AgentState.EXECUTING

            for (toolCall in response.toolCalls) {
                if (!isActive()) {
                    _state.value = AgentState.FAILED
                    return
                }

                Logger.i("AgentLoop: Executing tool: ${toolCall.name}")

                val argsMap = parseArguments(toolCall.arguments)

                addStep(AgentStep(
                    index = iteration,
                    tool = toolCall.name,
                    args = toolCall.arguments.take(300),
                    timestamp = System.currentTimeMillis()
                ))

                val result = try {
                    toolExecutor(toolCall.name, argsMap)
                } catch (e: Exception) {
                    "Error executing ${toolCall.name}: ${e.message}"
                }

                // Update step with result
                updateLastStep { prev ->
                    prev.copy(result = result.take(1000))
                }

                // Add tool result to message history
                messageHistory.add(
                    ChatMessage(
                        role = "tool",
                        content = result.take(4000),
                        toolCallId = toolCall.id,
                        name = toolCall.name
                    )
                )

                Logger.d("AgentLoop: Tool ${toolCall.name} result: ${result.take(200)}")
            }

            // Brief pause between iterations
            _state.value = AgentState.WAITING
            delay(500)
        }

        Logger.w("AgentLoop: Max iterations ($maxIterations) reached")
        _state.value = AgentState.FAILED
        addStep(AgentStep(
            index = iteration,
            thought = "Max iterations reached. Goal may not be fully completed.",
            timestamp = System.currentTimeMillis()
        ))
        // Auto-reset to IDLE after 15 seconds
        autoResetToIdle(15000)
    }

    /** Auto-reset state to IDLE after a delay, allowing new goals to be started */
    private fun autoResetToIdle(delayMs: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delayMs)
            if (_state.value == AgentState.COMPLETED || _state.value == AgentState.FAILED) {
                _state.value = AgentState.IDLE
                Logger.i("AgentLoop: Auto-reset state to IDLE")
            }
        }
    }

    private fun parseArguments(argsJson: String): Map<String, String> {
        return try {
            val obj = json.parseToJsonElement(argsJson).jsonObject
            obj.mapValues { (_, v) ->
                when {
                    v is kotlinx.serialization.json.JsonPrimitive -> v.content
                    else -> v.toString()
                }
            }
        } catch (e: Exception) {
            Logger.w("AgentLoop: Failed to parse tool arguments: $argsJson")
            emptyMap()
        }
    }

    private fun isActive(): Boolean =
        _state.value != AgentState.FAILED && job?.isActive == true

    private fun addStep(step: AgentStep) {
        _stepLog.value = _stepLog.value + step
    }

    private fun updateLastStep(transform: (AgentStep) -> AgentStep) {
        val steps = _stepLog.value.toMutableList()
        if (steps.isNotEmpty()) {
            val lastIndex = steps.lastIndex
            steps[lastIndex] = transform(steps[lastIndex])
            _stepLog.value = steps
        }
    }

    fun getStats(): Map<String, Any> = mapOf(
        "state" to _state.value.name,
        "currentGoal" to (_currentGoal.value ?: ""),
        "totalSteps" to _totalSteps.value,
        "totalTokens" to _totalTokens.value,
        "stepLogSize" to _stepLog.value.size
    )
}
