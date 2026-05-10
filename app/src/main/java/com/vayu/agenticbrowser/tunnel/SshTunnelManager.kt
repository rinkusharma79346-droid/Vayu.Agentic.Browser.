package com.vayu.agenticbrowser.tunnel

import android.content.Context
import android.content.SharedPreferences
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

/**
 * Manages SSH tunnel connections for exposing the local MCP server
 * to remote AI assistants (Claude, GPT, etc.) via SSH remote port forwarding.
 *
 * SSH Remote Port Forwarding (-R):
 *   Phone (localhost:8765) <--> SSH Server <--> AI Client
 *
 * The AI connects to ssh-server:remotePort which is forwarded back to
 * the phone's MCP server at localhost:8765.
 */
class SshTunnelManager private constructor() {

    private var context: Context? = null
    private var jsch: JSch? = null
    private var session: Session? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _tunnelUrl = MutableStateFlow<String?>(null)
    val tunnelUrl: StateFlow<String?> = _tunnelUrl.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Default config
    private val _config = MutableStateFlow(SshTunnelConfig())
    val config: StateFlow<SshTunnelConfig> = _config.asStateFlow()

    private var forwardThread: Thread? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
        loadConfig()
    }

    fun updateConfig(newConfig: SshTunnelConfig) {
        _config.value = newConfig
        saveConfig(newConfig)
    }

    /**
     * Start SSH remote port forwarding tunnel.
     * Connects to the SSH server and creates a remote port forward (-R)
     * from remotePort on the SSH server back to localhost:8765 on the phone.
     */
    suspend fun startTunnel(
        host: String = _config.value.host,
        port: Int = _config.value.port,
        username: String = _config.value.username,
        authType: String = _config.value.authType,
        password: String = _config.value.password,
        privateKey: String = _config.value.privateKey,
        remotePort: Int = _config.value.remotePort,
        localPort: Int = _config.value.localPort
    ): String = withContext(Dispatchers.IO) {
        if (_isRunning.value) {
            return@withContext _tunnelUrl.value ?: "Tunnel already running"
        }

        _lastError.value = null

        try {
            Logger.i("SSH Tunnel: Connecting to $username@$host:$port ...")

            val jschInstance = JSch()
            this.jsch = jschInstance

            // Set up authentication
            if (authType == "key" && privateKey.isNotBlank()) {
                Logger.d("SSH Tunnel: Using private key authentication")
                val keyStream = ByteArrayInputStream(privateKey.toByteArray(Charsets.UTF_8))
                jschInstance.addIdentity("vayu_ssh_key", keyStream, null, null)
            }

            val sshSession = jschInstance.getSession(username, host, port)

            if (authType == "password" && password.isNotBlank()) {
                Logger.d("SSH Tunnel: Using password authentication")
                sshSession.setPassword(password)
            }

            // Strict host key checking off for convenience (can be enabled in config)
            sshSession.setConfig("StrictHostKeyChecking", "no")
            sshSession.setConfig("UserKnownHostsFile", "/dev/null")
            sshSession.setConfig("PreferredAuthentications", if (authType == "key") "publickey" else "password,keyboard-interactive")
            sshSession.timeout = 30_000

            // Connect
            sshSession.connect(30_000)
            Logger.i("SSH Tunnel: Connected to SSH server")

            this.session = sshSession

            // Set up remote port forwarding: -R remotePort:localhost:localPort
            // Traffic hitting ssh-server:remotePort will be forwarded to phone:localPort
            val forwardedPort = sshSession.setPortForwardingR(remotePort, "localhost", localPort)
            Logger.i("SSH Tunnel: Remote port forwarding set up - remote:$forwardedPort -> localhost:$localPort")

            _isRunning.value = true
            _tunnelUrl.value = "$host:$forwardedPort"

            // Update config with effective values
            val effectiveConfig = _config.value.copy(
                host = host, port = port, username = username,
                authType = authType, remotePort = forwardedPort, localPort = localPort
            )
            _config.value = effectiveConfig
            saveConfig(effectiveConfig)

            // Keep-alive thread to detect disconnects
            forwardThread = Thread {
                try {
                    while (sshSession.isConnected) {
                        Thread.sleep(10_000)
                    }
                    Logger.w("SSH Tunnel: Session disconnected")
                    _isRunning.value = false
                    _tunnelUrl.value = null
                } catch (e: InterruptedException) {
                    // Normal shutdown
                }
            }.also { it.isDaemon = true; it.start() }

            """{"success":true,"host":"$host","remotePort":$forwardedPort,"localPort":$localPort,"url":"$host:$forwardedPort"}"""

        } catch (e: Exception) {
            Logger.e("SSH Tunnel: Failed to start", e)
            _isRunning.value = false
            _tunnelUrl.value = null
            _lastError.value = e.message

            // Cleanup
            try { session?.disconnect() } catch (_: Exception) {}
            session = null
            jsch = null

            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    /**
     * Stop the SSH tunnel and disconnect.
     */
    fun stopTunnel() {
        try {
            forwardThread?.interrupt()
            forwardThread = null

            session?.disconnect()
            session = null
            jsch = null

            _isRunning.value = false
            _tunnelUrl.value = null
            Logger.i("SSH Tunnel: Stopped")
        } catch (e: Exception) {
            Logger.e("SSH Tunnel: Error stopping", e)
        }
    }

    /**
     * Get the full MCP endpoint URL that an AI assistant should connect to.
     * For Claude MCP: use the SSE endpoint format
     * For custom WebSocket clients: use the WS endpoint format
     */
    fun getMcpEndpointUrl(protocol: String = "sse"): String? {
        val url = _tunnelUrl.value ?: return null
        return when (protocol) {
            "sse" -> "http://$url/sse"
            "ws" -> "ws://$url/mcp"
            else -> "http://$url/sse"
        }
    }

    private fun saveConfig(config: SshTunnelConfig) {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("vayu_ssh_tunnel", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("host", config.host)
            putInt("port", config.port)
            putString("username", config.username)
            putString("authType", config.authType)
            putString("password", config.password)
            putString("privateKey", config.privateKey)
            putInt("remotePort", config.remotePort)
            putInt("localPort", config.localPort)
            putBoolean("strictHostKeyChecking", config.strictHostKeyChecking)
            apply()
        }
    }

    private fun loadConfig() {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("vayu_ssh_tunnel", Context.MODE_PRIVATE)
        _config.value = SshTunnelConfig(
            host = prefs.getString("host", "") ?: "",
            port = prefs.getInt("port", 22),
            username = prefs.getString("username", "") ?: "",
            authType = prefs.getString("authType", "password") ?: "password",
            password = prefs.getString("password", "") ?: "",
            privateKey = prefs.getString("privateKey", "") ?: "",
            remotePort = prefs.getInt("remotePort", 8765),
            localPort = prefs.getInt("localPort", 8765),
            strictHostKeyChecking = prefs.getBoolean("strictHostKeyChecking", false)
        )
    }

    companion object {
        @Volatile
        private var instance: SshTunnelManager? = null

        fun getInstance(): SshTunnelManager {
            return instance ?: synchronized(this) {
                instance ?: SshTunnelManager().also { instance = it }
            }
        }
    }
}

/**
 * SSH tunnel configuration data.
 */
data class SshTunnelConfig(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val authType: String = "password", // "password" or "key"
    val password: String = "",
    val privateKey: String = "",
    val remotePort: Int = 8765,
    val localPort: Int = 8765,
    val strictHostKeyChecking: Boolean = false
)
