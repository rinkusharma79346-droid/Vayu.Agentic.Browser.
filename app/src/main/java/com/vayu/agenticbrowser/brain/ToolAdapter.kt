package com.vayu.agenticbrowser.brain

import com.vayu.agenticbrowser.agent.ToolRegistry
import com.vayu.agenticbrowser.plugins.PluginRegistry
import kotlinx.serialization.json.*

object ToolAdapter {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /**
     * Core tools that are always included — these cover 95% of agent tasks.
     * Non-core tools (vault, plugins, tunnel, recording, session, stealth, workflows, brain)
     * are excluded by default to save tokens and avoid Groq's 413 error.
     */
    private val CORE_TOOLS = setOf(
        // Navigation & DOM
        "browser_navigate", "browser_query_selector", "browser_click", "browser_type",
        "browser_evaluate",
        // Tabs
        "tab_list", "tab_new", "tab_close", "tab_switch", "tab_execute", "tab_wait_for_load",
        // Wait
        "wait_for_selector", "wait_for_text", "wait_for_navigation", "wait_for_url_contains",
        // Screenshots
        "screenshot_full", "screenshot_element",
        // Downloads
        "download_trigger", "download_list", "download_wait", "download_get_path",
        // Forms & Dialogs
        "form_detect", "form_fill", "dialog_detect", "dialog_accept", "dialog_dismiss",
        // Info
        "browser_info"
    )

    fun convertToOpenAiFunctions(pluginRegistry: PluginRegistry? = null): List<OpenAiFunction> {
        val functions = mutableListOf<OpenAiFunction>()

        // Only include core tools to stay within Groq's context limit
        for (tool in ToolRegistry.tools) {
            if (tool.name !in CORE_TOOLS) continue
            val parametersJson = buildParametersJson(tool.parameters)
            functions.add(
                OpenAiFunction(
                    name = tool.name,
                    // Truncate descriptions to save tokens
                    description = tool.description.take(200),
                    parameters = parametersJson
                )
            )
        }

        // Add plugin-specific tools for the current site
        if (pluginRegistry != null) {
            val pluginTools = mutableListOf<com.vayu.agenticbrowser.plugins.ToolDef>()
            pluginRegistry.activePlugins.value.forEach { plugin ->
                pluginTools.addAll(plugin.tools)
            }

            for (tool in pluginTools) {
                if (functions.none { it.name == tool.name }) {
                    val parametersJson = buildPluginParametersJson(tool.parameters)
                    functions.add(
                        OpenAiFunction(
                            name = tool.name,
                            description = tool.description.take(200),
                            parameters = parametersJson
                        )
                    )
                }
            }
        }

        return functions
    }

    private fun buildParametersJson(
        params: Map<String, com.vayu.agenticbrowser.agent.ToolParam>
    ): JsonObject {
        val properties = buildJsonObject {
            for ((name, param) in params) {
                put(name, buildJsonObject {
                    put("type", param.type)
                    // Truncate param descriptions too
                    put("description", param.description.take(100))
                })
            }
        }

        val required = params.filter { it.value.required }.keys.toList()

        return buildJsonObject {
            put("type", "object")
            put("properties", properties)
            if (required.isNotEmpty()) {
                put("required", buildJsonArray {
                    required.forEach { add(it) }
                })
            }
        }
    }

    private fun buildPluginParametersJson(
        params: Map<String, com.vayu.agenticbrowser.plugins.ToolParam>
    ): JsonObject {
        val properties = buildJsonObject {
            for ((name, param) in params) {
                put(name, buildJsonObject {
                    put("type", param.type)
                    if (param.default != null) {
                        put("default", param.default)
                    }
                })
            }
        }

        val required = params.filter { it.value.required }.keys.toList()

        return buildJsonObject {
            put("type", "object")
            put("properties", properties)
            if (required.isNotEmpty()) {
                put("required", buildJsonArray {
                    required.forEach { add(it) }
                })
            }
        }
    }
}
