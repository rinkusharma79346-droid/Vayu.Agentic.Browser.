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
        // ===== Phase 1: Browser DOM Tools =====
        ToolDef(
            name = "browser_navigate",
            description = "Navigate the browser to a specified URL and wait for the page to finish loading. Returns the page title and final URL after navigation completes.",
            parameters = mapOf(
                "url" to ToolParam(type = "string", description = "The fully qualified URL to navigate to (e.g., https://example.com)", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to navigate in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "browser_query_selector",
            description = "Query DOM elements using a CSS selector. Returns an array of matching elements with their tag name, text content, ID, class name, and visibility status.",
            parameters = mapOf(
                "selector" to ToolParam(type = "string", description = "A valid CSS selector string (e.g., '#login-btn', '.item', 'div > p')", required = true),
                "all" to ToolParam(type = "boolean", description = "If true, returns all matching elements; if false, returns only the first match", required = false),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to query in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "browser_click",
            description = "Simulate a click event on a DOM element matched by a CSS selector. Dispatches mousedown, mouseup, and click events in sequence.",
            parameters = mapOf(
                "selector" to ToolParam(type = "string", description = "A valid CSS selector string to identify the element to click", required = true),
                "index" to ToolParam(type = "integer", description = "Zero-based index of the element among matches (default: 0)", required = false),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to click in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "browser_type",
            description = "Type text into a form element matched by a CSS selector. Simulates realistic keystroke events including keydown, input, and keyup for each character, followed by change and blur events.",
            parameters = mapOf(
                "selector" to ToolParam(type = "string", description = "A valid CSS selector string to identify the input element", required = true),
                "text" to ToolParam(type = "string", description = "The text string to type into the element", required = true),
                "clearFirst" to ToolParam(type = "boolean", description = "If true, clears the existing value before typing (default: false)", required = false),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to type in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "browser_evaluate",
            description = "Evaluate an arbitrary JavaScript expression in the browser context and return the result. Use with caution as this executes any JavaScript code.",
            parameters = mapOf(
                "script" to ToolParam(type = "string", description = "A JavaScript expression or code block to evaluate", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to evaluate in. Defaults to the active tab.", required = false)
            )
        ),

        // ===== Phase 2: Tab Tools =====
        ToolDef(
            name = "tab_new",
            description = "Create a new browser tab and navigate to the specified URL. Returns the new tab's ID and state.",
            parameters = mapOf(
                "url" to ToolParam(type = "string", description = "The URL to load in the new tab", required = true),
                "background" to ToolParam(type = "boolean", description = "If true, the tab is created in the background without switching to it (default: false)", required = false)
            )
        ),
        ToolDef(
            name = "tab_close",
            description = "Close a browser tab by its ID. If the closed tab was active, switches to the most recently used tab.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "The ID of the tab to close", required = true)
            )
        ),
        ToolDef(
            name = "tab_switch",
            description = "Switch the active browser tab to the specified tab ID. Updates the UI to show the selected tab's WebView.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "The ID of the tab to switch to", required = true)
            )
        ),
        ToolDef(
            name = "tab_list",
            description = "List all open browser tabs with their current state including title, URL, loading status, and active status.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "tab_execute",
            description = "Execute a DOM operation in a specific tab's WebView without switching the active tab. Useful for background tab operations.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "The ID of the tab to execute in", required = true),
                "tool" to ToolParam(type = "string", description = "The DOM tool to execute: navigate, querySelector, click, type, or evaluate", required = true),
                "args" to ToolParam(type = "object", description = "Arguments to pass to the specified DOM tool", required = true)
            )
        ),
        ToolDef(
            name = "tab_wait_for_load",
            description = "Wait for a tab to finish loading its current page. Returns the final URL and title once loading completes or times out.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "The ID of the tab to wait for. Defaults to the active tab.", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),

        // ===== Phase 2: Download Tools =====
        ToolDef(
            name = "download_trigger",
            description = "Trigger a file download by clicking a download element matched by a CSS selector or by providing a direct URL. Returns a download ID for tracking.",
            parameters = mapOf(
                "selectorOrUrl" to ToolParam(type = "string", description = "A CSS selector for a download link, or a direct URL to download", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID where the download element exists. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "download_list",
            description = "List all tracked downloads with their current status, progress, file paths, and metadata.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "download_wait",
            description = "Wait for a specific download to complete. Polls the download status until it finishes or the timeout is reached.",
            parameters = mapOf(
                "downloadId" to ToolParam(type = "string", description = "The ID of the download to wait for", required = true),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 60000)", required = false)
            )
        ),
        ToolDef(
            name = "download_get_path",
            description = "Get the local file path and status of a specific download by its ID.",
            parameters = mapOf(
                "downloadId" to ToolParam(type = "string", description = "The ID of the download to get the path for", required = true)
            )
        ),
        ToolDef(
            name = "download_cancel",
            description = "Cancel a pending or active download by its ID.",
            parameters = mapOf(
                "downloadId" to ToolParam(type = "string", description = "The ID of the download to cancel", required = true)
            )
        ),

        // ===== Phase 2: Wait Tools =====
        ToolDef(
            name = "wait_for_selector",
            description = "Wait for a CSS selector to match at least one element in the DOM. Polls every 500ms until the element appears or timeout is reached.",
            parameters = mapOf(
                "selector" to ToolParam(type = "string", description = "A valid CSS selector to wait for", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to check in. Defaults to the active tab.", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),
        ToolDef(
            name = "wait_for_text",
            description = "Wait for specific text to appear in the page body. Polls document.body.innerText every 500ms until the text is found or timeout is reached.",
            parameters = mapOf(
                "text" to ToolParam(type = "string", description = "The text string to wait for", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to check in. Defaults to the active tab.", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),
        ToolDef(
            name = "wait_for_navigation",
            description = "Wait for the page URL to change from its current value, indicating a navigation has occurred.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to monitor. Defaults to the active tab.", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),
        ToolDef(
            name = "wait_for_url_contains",
            description = "Wait for the current page URL to contain a specific substring. Useful for detecting redirects or SPA route changes.",
            parameters = mapOf(
                "substring" to ToolParam(type = "string", description = "The substring that the URL should contain", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to monitor. Defaults to the active tab.", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),
        ToolDef(
            name = "wait_for_download",
            description = "Wait for any new download to be triggered. Returns the download ID and filename once a download starts.",
            parameters = mapOf(
                "timeoutMs" to ToolParam(type = "integer", description = "Maximum time to wait in milliseconds (default: 30000)", required = false)
            )
        ),

        // ===== Phase 2: Screenshot Tools =====
        ToolDef(
            name = "screenshot_full",
            description = "Capture a full-page screenshot of the current WebView content. Returns a base64-encoded PNG image string.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to screenshot. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "screenshot_element",
            description = "Capture a screenshot of a specific DOM element matched by a CSS selector. Returns a base64-encoded PNG image of the element's bounding rectangle.",
            parameters = mapOf(
                "selector" to ToolParam(type = "string", description = "A valid CSS selector to identify the element to screenshot", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to screenshot. Defaults to the active tab.", required = false)
            )
        )
    )

    fun toJson(): String {
        return json.encodeToString(tools)
    }

    fun getToolNames(): List<String> = tools.map { it.name }
}
