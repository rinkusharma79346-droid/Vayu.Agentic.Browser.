package com.vayu.agenticbrowser.brain

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class Workflow(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val goalPrompt: String,
    val schedule: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isBuiltIn: Boolean = false
)

class WorkflowEngine(
    private val context: Context,
    private val agentLoop: AgentLoop
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val prefsName = "vayu_workflows"
    private val keyWorkflows = "saved_workflows"

    private val _workflows = MutableStateFlow<List<Workflow>>(emptyList())
    val workflows: StateFlow<List<Workflow>> = _workflows.asStateFlow()

    init {
        loadWorkflows()
    }

    fun listWorkflows(): List<Workflow> = _workflows.value

    fun getWorkflow(id: String): Workflow? = _workflows.value.find { it.id == id }

    fun saveWorkflow(workflow: Workflow): Workflow {
        val existing = _workflows.value.find { it.id == workflow.id }
        val toSave = if (workflow.id.isBlank()) {
            workflow.copy(id = UUID.randomUUID().toString())
        } else {
            workflow
        }

        val updatedList = if (existing != null) {
            _workflows.value.map { if (it.id == toSave.id) toSave else it }
        } else {
            _workflows.value + toSave
        }

        _workflows.value = updatedList
        persistWorkflows()
        Logger.i("WorkflowEngine: Saved workflow '${toSave.name}' (id=${toSave.id})")
        return toSave
    }

    fun deleteWorkflow(id: String) {
        val workflow = _workflows.value.find { it.id == id }
        if (workflow != null) {
            if (workflow.isBuiltIn) {
                Logger.w("WorkflowEngine: Cannot delete built-in workflow '$id'")
                return
            }
            _workflows.value = _workflows.value.filter { it.id != id }
            persistWorkflows()
            Logger.i("WorkflowEngine: Deleted workflow '$id'")
        }
    }

    suspend fun runWorkflow(id: String): String {
        val workflow = getWorkflow(id)
        if (workflow == null) {
            val error = "Workflow not found: $id"
            Logger.e("WorkflowEngine: $error")
            return error
        }

        Logger.i("WorkflowEngine: Running workflow '${workflow.name}'")
        agentLoop.runGoal(workflow.goalPrompt)
        return """{"success":true,"workflowId":"$id","name":"${workflow.name.replace("\"", "\\\"")}","state":"${agentLoop.state.value.name}"}"""
    }

    private fun loadWorkflows() {
        // Load built-in workflows from raw resources
        val builtInWorkflows = loadBuiltInWorkflows()

        // Load user-created workflows from SharedPreferences
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedJson = prefs.getString(keyWorkflows, null)

        val userWorkflows = if (savedJson != null) {
            try {
                json.decodeFromString<List<Workflow>>(savedJson)
            } catch (e: Exception) {
                Logger.e("WorkflowEngine: Failed to load saved workflows", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        _workflows.value = builtInWorkflows + userWorkflows
        Logger.i("WorkflowEngine: Loaded ${builtInWorkflows.size} built-in + ${userWorkflows.size} user workflows")
    }

    private fun loadBuiltInWorkflows(): List<Workflow> {
        val builtIn = mutableListOf<Workflow>()

        // YouTube Short Pipeline
        try {
            val ytJson = context.resources.openRawResource(
                context.resources.getIdentifier(
                    "workflow_youtube_short", "raw", context.packageName
                )
            ).bufferedReader().use { it.readText() }
            val ytWorkflow = json.decodeFromString<Workflow>(ytJson)
            builtIn.add(ytWorkflow)
        } catch (e: Exception) {
            Logger.w("WorkflowEngine: Could not load youtube_short workflow: ${e.message}")
        }

        // Research & Summarize
        try {
            val researchJson = context.resources.openRawResource(
                context.resources.getIdentifier(
                    "workflow_research_summarize", "raw", context.packageName
                )
            ).bufferedReader().use { it.readText() }
            val researchWorkflow = json.decodeFromString<Workflow>(researchJson)
            builtIn.add(researchWorkflow)
        } catch (e: Exception) {
            Logger.w("WorkflowEngine: Could not load research_summarize workflow: ${e.message}")
        }

        // Social Media Post
        try {
            val socialJson = context.resources.openRawResource(
                context.resources.getIdentifier(
                    "workflow_social_media_post", "raw", context.packageName
                )
            ).bufferedReader().use { it.readText() }
            val socialWorkflow = json.decodeFromString<Workflow>(socialJson)
            builtIn.add(socialWorkflow)
        } catch (e: Exception) {
            Logger.w("WorkflowEngine: Could not load social_media_post workflow: ${e.message}")
        }

        return builtIn
    }

    private fun persistWorkflows() {
        val userWorkflows = _workflows.value.filter { !it.isBuiltIn }
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(keyWorkflows, json.encodeToString(userWorkflows))
            .apply()
    }
}
