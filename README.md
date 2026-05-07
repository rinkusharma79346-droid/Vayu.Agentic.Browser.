# VAYU Agentic Browser

**Phase 1: Foundation — MCP WebSocket + DOM Control Engine**

VAYU is a custom Android browser where AI agents control web pages via MCP (Model Context Protocol) WebSocket instead of accessibility services.

## Architecture

- **MCP WebSocket Server** — Ktor CIO server on port 8765 with SHA-256 auth challenge
- **JavaScript Bridge** — `@JavascriptInterface` bridge for bidirectional JS ↔ Kotlin communication
- **DOM Controller** — 5 agent tools: navigate, querySelector, click, type, evaluate
- **WebView Manager** — Singleton managing Android WebView with JS enabled
- **Jetpack Compose UI** — URL bar, WebView, agent status bar

## 5 Agent Tools

| Tool | Description |
|------|-------------|
| `browser_navigate` | Navigate to URL, wait for page load |
| `browser_query_selector` | Query DOM elements by CSS selector |
| `browser_click` | Click element by selector + index |
| `browser_type` | Type text into form element with key events |
| `browser_evaluate` | Evaluate arbitrary JavaScript |

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
- AndroidX WebView, Material3
