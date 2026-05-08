# VAYU Agentic Browser

**Phase 2: Multi-Tab, Downloads, Wait Tools, Screenshots**

VAYU is a custom Android browser where AI agents control web pages via MCP (Model Context Protocol) WebSocket instead of accessibility services.

## Architecture

- **MCP WebSocket Server** — Ktor CIO server on port 8765 with SHA-256 auth challenge
- **JavaScript Bridge** — `@JavascriptInterface` bridge for bidirectional JS ↔ Kotlin communication
- **DOM Controller** — 5 browser tools: navigate, querySelector, click, type, evaluate (with optional tabId)
- **Tab Manager** — Multi-tab support with per-tab WebView instances and state tracking
- **Download Manager** — Download tracking with Android DownloadManager integration
- **Wait Controller** — 5 wait tools for async conditions (selector, text, navigation, URL, download)
- **Screenshot Utility** — Full-page and element-level screenshot capture as base64 PNG
- **Jetpack Compose UI** — URL bar, tab strip, WebView, download indicator, agent status bar

## 26 Agent Tools

### Browser DOM Tools (Phase 1)
| Tool | Description |
|------|-------------|
| `browser_navigate` | Navigate to URL, wait for page load (optional tabId) |
| `browser_query_selector` | Query DOM elements by CSS selector (optional tabId) |
| `browser_click` | Click element by selector + index (optional tabId) |
| `browser_type` | Type text into form element with key events (optional tabId) |
| `browser_evaluate` | Evaluate arbitrary JavaScript (optional tabId) |

### Tab Tools (Phase 2)
| Tool | Description |
|------|-------------|
| `tab_new` | Create new tab with URL, optional background |
| `tab_close` | Close tab by ID |
| `tab_switch` | Switch active tab |
| `tab_list` | List all open tabs with state |
| `tab_execute` | Run DOM operation in any tab without switching |
| `tab_wait_for_load` | Wait for a tab to finish loading |

### Download Tools (Phase 2)
| Tool | Description |
|------|-------------|
| `download_trigger` | Trigger download by URL or clicking element |
| `download_list` | List all tracked downloads |
| `download_wait` | Wait for download to complete |
| `download_get_path` | Get local file path of download |
| `download_cancel` | Cancel a download |

### Wait Tools (Phase 2)
| Tool | Description |
|------|-------------|
| `wait_for_selector` | Poll until CSS selector matches |
| `wait_for_text` | Poll until text appears in body |
| `wait_for_navigation` | Wait for URL to change |
| `wait_for_url_contains` | Wait for URL to contain substring |
| `wait_for_download` | Wait for any download to start |

### Screenshot Tools (Phase 2)
| Tool | Description |
|------|-------------|
| `screenshot_full` | Full-page screenshot as base64 PNG |
| `screenshot_element` | Element screenshot by CSS selector |

## Authentication

Agents connect via WebSocket to `ws://<device-ip>:8765/mcp` and complete a SHA-256 challenge using pairing code `vayu1234`.

## Build

```bash
./gradlew assembleDebug
```

## Tech Stack

- Kotlin 2.0, Jetpack Compose BOM 2024.09.00
- Ktor Server 2.3.12 (WebSockets + CIO)
- Hilt 2.51, kotlinx-serialization-json 1.6.3
- AndroidX WebView, Coroutines 1.8.1, Material3
- minSdk 26, targetSdk 34

## No Accessibility Services

This browser explicitly does NOT use any accessibility service. Agent control is achieved entirely through the MCP WebSocket protocol and JavaScript injection.
