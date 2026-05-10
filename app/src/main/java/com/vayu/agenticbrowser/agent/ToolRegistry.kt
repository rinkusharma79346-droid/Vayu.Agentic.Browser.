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
        ),

        // ===== Phase 3: Vault Tools =====
        ToolDef(
            name = "vault_list_profiles",
            description = "List all saved credential profiles in the VAYU Vault. Returns profile names, site URLs, and usernames without exposing passwords. Requires biometric unlock.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "vault_use_profile",
            description = "Get details of a specific vault profile by ID, including decrypted TOTP code if configured. Passwords are never returned. Requires biometric unlock.",
            parameters = mapOf(
                "profileId" to ToolParam(type = "string", description = "The ID of the profile to retrieve", required = true)
            )
        ),
        ToolDef(
            name = "vault_fill_login",
            description = "Automatically fill login form fields on the current page using the best matching vault profile for the site URL. Detects email/username and password fields. Requires biometric unlock.",
            parameters = mapOf(
                "siteUrl" to ToolParam(type = "string", description = "The site URL to match against saved profiles (defaults to current page URL)", required = false),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to fill login in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "vault_get_otp",
            description = "Generate a TOTP code from a profile's stored seed, or read the latest SMS OTP. Requires biometric unlock.",
            parameters = mapOf(
                "profileId" to ToolParam(type = "string", description = "The ID of the profile with TOTP seed configured", required = false),
                "sms" to ToolParam(type = "boolean", description = "If true, reads OTP from SMS inbox instead of TOTP (default: false)", required = false),
                "timeoutMs" to ToolParam(type = "integer", description = "Timeout for SMS OTP reading in milliseconds (default: 60000)", required = false)
            )
        ),
        ToolDef(
            name = "vault_save_cookies",
            description = "Save the current page cookies to a vault profile for session persistence. Requires biometric unlock.",
            parameters = mapOf(
                "profileId" to ToolParam(type = "string", description = "The ID of the profile to save cookies to", required = true),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to read cookies from. Defaults to the active tab.", required = false)
            )
        ),

        // ===== Phase 3: Form Tools =====
        ToolDef(
            name = "form_detect",
            description = "Detect all form input elements on the current page. Returns an array of fields with their selectors, types, names, placeholders, and visibility status.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to detect forms in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "form_fill",
            description = "Fill multiple form fields at once using a mapping of CSS selectors to values. Optionally submit the form after filling.",
            parameters = mapOf(
                "mapping" to ToolParam(type = "object", description = "A mapping of CSS selector strings to values to fill in each field", required = true),
                "submitSelector" to ToolParam(type = "string", description = "Optional CSS selector for the submit button to click after filling", required = false),
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to fill form in. Defaults to the active tab.", required = false)
            )
        ),

        // ===== Phase 3: Dialog Tools =====
        ToolDef(
            name = "dialog_detect",
            description = "Detect modal dialogs, cookie banners, and popup overlays on the current page. Returns the type and visible text of any detected dialog.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to check for dialogs. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "dialog_accept",
            description = "Accept or dismiss a detected dialog by clicking the first visible button matching accept/OK/allow/agree patterns.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to accept dialog in. Defaults to the active tab.", required = false)
            )
        ),
        ToolDef(
            name = "dialog_dismiss",
            description = "Dismiss a detected dialog by clicking the first visible button matching reject/cancel/close/decline patterns.",
            parameters = mapOf(
                "tabId" to ToolParam(type = "integer", description = "Optional tab ID to dismiss dialog in. Defaults to the active tab.", required = false)
            )
        ),

        // ===== Phase 4: Plugin Tools =====
        ToolDef(
            name = "plugin_list",
            description = "List all installed plugins with their name, version, description, enabled status, and matched sites.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "plugin_enable",
            description = "Enable a plugin by name so its tools and rules become active for matching sites.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "The name of the plugin to enable", required = true)
            )
        ),
        ToolDef(
            name = "plugin_disable",
            description = "Disable a plugin by name so its tools and rules are no longer active.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "The name of the plugin to disable", required = true)
            )
        ),

        // ===== Phase 4: Tunnel Tools =====
        ToolDef(
            name = "tunnel_start",
            description = "Start a Cloudflare Tunnel that exposes the local MCP WebSocket server to the internet via a trycloudflare.com URL. Downloads cloudflared binary if not present.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "tunnel_stop",
            description = "Stop the active Cloudflare Tunnel and remove the public URL.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "tunnel_get_url",
            description = "Get the current Cloudflare Tunnel public URL if a tunnel is active.",
            parameters = mapOf()
        ),

        // ===== Phase 4: Recording Tools =====
        ToolDef(
            name = "recording_start",
            description = "Start recording agent tool calls. All subsequent tool calls will be captured until recording_stop is called.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "A descriptive name for this recording", required = true)
            )
        ),
        ToolDef(
            name = "recording_stop",
            description = "Stop the current recording and save it. Returns the recording with all captured commands.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "recording_list",
            description = "List all saved recordings with their names, command counts, and creation dates.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "recording_replay",
            description = "Replay a saved recording by re-executing each command with original timing gaps (capped at 2s between commands). Returns a summary of results.",
            parameters = mapOf(
                "id" to ToolParam(type = "string", description = "The ID of the recording to replay", required = true)
            )
        ),
        ToolDef(
            name = "recording_delete",
            description = "Delete a saved recording by its ID.",
            parameters = mapOf(
                "id" to ToolParam(type = "string", description = "The ID of the recording to delete", required = true)
            )
        ),

        // ===== Phase 4: Session Tools =====
        ToolDef(
            name = "session_save",
            description = "Save the current browser session (all tab URLs and cookies) to SharedPreferences by name for later restoration.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "A name to identify this saved session", required = true)
            )
        ),
        ToolDef(
            name = "session_load",
            description = "Load a previously saved browser session by name, restoring all tab URLs and cookies.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "The name of the session to load", required = true)
            )
        ),

        // ===== Phase 4: User Agent Tool =====
        ToolDef(
            name = "user_agent_set",
            description = "Set a custom User-Agent string for all WebViews. Use preset names (Chrome Android, Chrome Desktop, Safari iOS) or a custom string.",
            parameters = mapOf(
                "userAgent" to ToolParam(type = "string", description = "The User-Agent string or preset name to apply", required = true)
            )
        ),

        // ===== Phase 4: Stealth Tools =====
        ToolDef(
            name = "stealth_enable",
            description = "Enable stealth mode on all WebViews. Removes navigator.webdriver, spoofs plugins, randomizes canvas fingerprint, and overrides window.chrome.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "stealth_disable",
            description = "Disable stealth mode on all WebViews, reverting anti-detection modifications.",
            parameters = mapOf()
        ),

        // ===== Phase 4: Browser Info Tool =====
        ToolDef(
            name = "browser_info",
            description = "Get comprehensive browser status information including app version, tab count, connection status, tunnel URL, plugin count, and platform.",
            parameters = mapOf()
        ),

        // ===== Phase 5: Brain / Autonomous Agent Tools =====
        ToolDef(
            name = "brain_run",
            description = "Start the autonomous AI agent to achieve a specified goal. The agent will plan steps, execute browser tools in a loop, and complete the multi-step workflow autonomously.",
            parameters = mapOf(
                "goal" to ToolParam(type = "string", description = "The goal for the autonomous agent to accomplish", required = true)
            )
        ),
        ToolDef(
            name = "brain_stop",
            description = "Stop the currently running autonomous agent goal execution.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "brain_status",
            description = "Get the current status of the autonomous agent including state, current goal, step count, token usage, and recent step log.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "brain_config",
            description = "Get or update the brain (LLM) configuration including provider, model, base URL, and API key status.",
            parameters = mapOf(
                "provider" to ToolParam(type = "string", description = "LLM provider: GEMINI, GROQ, OPENROUTER, or CUSTOM", required = false),
                "apiKey" to ToolParam(type = "string", description = "API key for the LLM provider", required = false),
                "baseUrl" to ToolParam(type = "string", description = "Custom base URL for the LLM API endpoint", required = false),
                "model" to ToolParam(type = "string", description = "Model name to use for completions", required = false),
                "maxTokens" to ToolParam(type = "integer", description = "Maximum tokens for LLM responses (default: 8192)", required = false)
            )
        ),
        ToolDef(
            name = "brain_list_goals",
            description = "List all scheduled autonomous agent goals with their scheduled times, recurrence, and completion status.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "brain_schedule",
            description = "Schedule an autonomous agent goal to run at a future time. The agent will wake up and execute the goal even when the app is backgrounded.",
            parameters = mapOf(
                "goal" to ToolParam(type = "string", description = "The goal for the agent to accomplish when triggered", required = true),
                "delayMinutes" to ToolParam(type = "integer", description = "Number of minutes from now to schedule the goal", required = true)
            )
        ),

        // ===== Phase 5: Workflow Tools =====
        ToolDef(
            name = "workflow_list",
            description = "List all available workflows including built-in and user-created ones. Returns workflow ID, name, description, and whether it is built-in.",
            parameters = mapOf()
        ),
        ToolDef(
            name = "workflow_run",
            description = "Run a saved workflow by its ID. This starts the autonomous agent with the workflow's goal prompt. Returns the agent state and goal information.",
            parameters = mapOf(
                "id" to ToolParam(type = "string", description = "The ID of the workflow to run", required = true)
            )
        ),
        ToolDef(
            name = "workflow_save",
            description = "Save a new workflow with a name, description, and goal prompt. The workflow can later be run or scheduled for automated execution.",
            parameters = mapOf(
                "name" to ToolParam(type = "string", description = "A descriptive name for the workflow", required = true),
                "description" to ToolParam(type = "string", description = "A brief description of what the workflow does", required = true),
                "goalPrompt" to ToolParam(type = "string", description = "The goal prompt that the autonomous agent will execute when this workflow is run", required = true)
            )
        )
    )

    fun toJson(): String {
        return json.encodeToString(tools)
    }

    fun getToolNames(): List<String> = tools.map { it.name }
}
