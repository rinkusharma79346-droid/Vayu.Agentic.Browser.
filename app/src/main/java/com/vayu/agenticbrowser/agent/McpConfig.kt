package com.vayu.agenticbrowser.agent

/**
 * Single source of truth for all MCP connection configuration.
 * Render SSE is the primary remote endpoint; local WS is the fallback.
 */
object McpConfig {
    /** Remote Render SSE endpoint (VAYU MCP Relay) */
    const val RENDER_SSE_URL = "https://vayu-mcp-relay.onrender.com/sse"
    
    /** Local WebSocket MCP endpoint */
    const val LOCAL_WS_URL = "ws://localhost:8765/mcp"
    
    /** Local server port */
    const val LOCAL_PORT = 8765
    
    /** Connection timeout for Render SSE in milliseconds */
    const val CONNECTION_TIMEOUT_MS = 30_000L
    
    /** Reconnect delay between attempts in milliseconds */
    const val RECONNECT_DELAY_MS = 5_000L
    
    /** Pairing code for local WebSocket auth */
    const val PAIRING_CODE = "vayu1234"
    
    /** Server name for display */
    const val SERVER_NAME = "VAYU MCP Server"
    
    /** App version */
    const val APP_VERSION = "1.6.2"
}

/**
 * MCP connection status for UI display.
 */
enum class McpStatus {
    /** Connected to Render SSE or local WS */
    CONNECTED,
    /** Attempting to connect / retrying */
    RETRYING,
    /** Disconnected / offline */
    OFFLINE
}
