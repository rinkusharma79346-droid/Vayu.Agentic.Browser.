package com.vayu.agenticbrowser.agent;

/**
 * Single source of truth for all MCP connection configuration.
 * Render SSE is the primary remote endpoint; local WS is the fallback.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/vayu/agenticbrowser/agent/McpConfig;", "", "()V", "APP_VERSION", "", "CONNECTION_TIMEOUT_MS", "", "LOCAL_PORT", "", "LOCAL_WS_URL", "PAIRING_CODE", "RECONNECT_DELAY_MS", "RENDER_SSE_URL", "SERVER_NAME", "app_debug"})
public final class McpConfig {
    
    /**
     * Remote Render SSE endpoint (JARVIS MCP server)
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String RENDER_SSE_URL = "https://j-a-r-v-i-s-ktlh.onrender.com/sse";
    
    /**
     * Local WebSocket MCP endpoint
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LOCAL_WS_URL = "ws://localhost:8765/mcp";
    
    /**
     * Local server port
     */
    public static final int LOCAL_PORT = 8765;
    
    /**
     * Connection timeout for Render SSE in milliseconds
     */
    public static final long CONNECTION_TIMEOUT_MS = 30000L;
    
    /**
     * Reconnect delay between attempts in milliseconds
     */
    public static final long RECONNECT_DELAY_MS = 5000L;
    
    /**
     * Pairing code for local WebSocket auth
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PAIRING_CODE = "vayu1234";
    
    /**
     * Server name for display
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SERVER_NAME = "VAYU MCP Server";
    
    /**
     * App version
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String APP_VERSION = "1.3.0";
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.agent.McpConfig INSTANCE = null;
    
    private McpConfig() {
        super();
    }
}