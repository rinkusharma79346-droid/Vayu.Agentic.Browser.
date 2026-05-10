package com.vayu.agenticbrowser.plugins

import kotlinx.serialization.Serializable

@Serializable
data class ToolParam(
    val type: String,
    val required: Boolean = false,
    val default: String? = null
)

@Serializable
data class ToolDef(
    val name: String,
    val description: String,
    val parameters: Map<String, ToolParam>
)

@Serializable
data class FormTemplate(
    val name: String,
    val fields: Map<String, String>
)

@Serializable
data class PluginRule(
    val trigger: String,
    val action: String,
    val args: Map<String, String> = emptyMap()
)

@Serializable
data class Plugin(
    val name: String,
    val version: String,
    val description: String,
    val sites: List<String>,
    val tools: List<ToolDef>,
    val rules: List<PluginRule> = emptyList(),
    val formTemplates: List<FormTemplate> = emptyList(),
    val enabled: Boolean = true
)
