package com.vayu.agenticbrowser.brain

import com.vayu.agenticbrowser.agent.ToolRegistry
import com.vayu.agenticbrowser.plugins.PluginRegistry
import kotlinx.serialization.json.*

object ToolAdapter {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun convertToOpenAiFunctions(pluginRegistry: PluginRegistry? = null): List<OpenAiFunction> {
        val functions = mutableListOf<OpenAiFunction>()

        // Convert all MCP tools from ToolRegistry
        for (tool in ToolRegistry.tools) {
            val parametersJson = buildParametersJson(tool.parameters)
            functions.add(
                OpenAiFunction(
                    name = tool.name,
                    description = tool.description,
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
                // Avoid duplicates from ToolRegistry
                if (functions.none { it.name == tool.name }) {
                    val parametersJson = buildPluginParametersJson(tool.parameters)
                    functions.add(
                        OpenAiFunction(
                            name = tool.name,
                            description = tool.description,
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
                    put("description", param.description)
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
