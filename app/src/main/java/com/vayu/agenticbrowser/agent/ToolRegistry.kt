package com.vayu.agenticbrowser.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ToolParam(
    val type: String,
    val description: String = "",
    val required: Boolean = false
)

@Serializable
data class ToolDef(
    val name: String,
    val description: String,
    val parameters: Map<String, ToolParam>
)

object ToolRegistry {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    val tools: List<ToolDef> = listOf(
        ToolDef(
            name = "browser_navigate",
            description = "Navigate the browser to a specified URL and wait for the page to finish loading. Returns the page title and final URL after navigation completes.",
            parameters = mapOf(
                "url" to ToolParam(
                    type = "string",
                    description = "The fully qualified URL to navigate to (e.g., https://example.com)",
                    required = true
                )
            )
        ),
        ToolDef(
            name = "browser_query_selector",
            description = "Query DOM elements using a CSS selector. Returns an array of matching elements with their tag name, text content, ID, class name, and visibility status.",
            parameters = mapOf(
                "selector" to ToolParam(
                    type = "string",
                    description = "A valid CSS selector string (e.g., '#login-btn', '.item', 'div > p')",
                    required = true
                ),
                "all" to ToolParam(
                    type = "boolean",
                    description = "If true, returns all matching elements; if false, returns only the first match",
                    required = false
                )
            )
        ),
        ToolDef(
            name = "browser_click",
            description = "Simulate a click event on a DOM element matched by a CSS selector. Dispatches mousedown, mouseup, and click events in sequence.",
            parameters = mapOf(
                "selector" to ToolParam(
                    type = "string",
                    description = "A valid CSS selector string to identify the element to click",
                    required = true
                ),
                "index" to ToolParam(
                    type = "integer",
                    description = "Zero-based index of the element among matches (default: 0)",
                    required = false
                )
            )
        ),
        ToolDef(
            name = "browser_type",
            description = "Type text into a form element matched by a CSS selector. Simulates realistic keystroke events including keydown, input, and keyup for each character, followed by change and blur events.",
            parameters = mapOf(
                "selector" to ToolParam(
                    type = "string",
                    description = "A valid CSS selector string to identify the input element",
                    required = true
                ),
                "text" to ToolParam(
                    type = "string",
                    description = "The text string to type into the element",
                    required = true
                ),
                "clearFirst" to ToolParam(
                    type = "boolean",
                    description = "If true, clears the existing value before typing (default: false)",
                    required = false
                )
            )
        ),
        ToolDef(
            name = "browser_evaluate",
            description = "Evaluate an arbitrary JavaScript expression in the browser context and return the result. Use with caution as this executes any JavaScript code.",
            parameters = mapOf(
                "script" to ToolParam(
                    type = "string",
                    description = "A JavaScript expression or code block to evaluate",
                    required = true
                )
            )
        )
    )

    fun toJson(): String {
        return json.encodeToString(tools)
    }

    fun getToolNames(): List<String> = tools.map { it.name }
}
