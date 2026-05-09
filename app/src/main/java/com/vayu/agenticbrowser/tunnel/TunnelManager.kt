package com.vayu.agenticbrowser.tunnel

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TunnelManager private constructor() {

    private var context: Context? = null
    private var process: Process? = null

    private val _tunnelUrl = MutableStateFlow<String?>(null)
    val tunnelUrl: StateFlow<String?> = _tunnelUrl.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    suspend fun startTunnel(): String {
        if (_isRunning.value) {
            return _tunnelUrl.value ?: "Tunnel starting..."
        }

        val ctx = context ?: throw IllegalStateException("TunnelManager not initialized")

        return withContext(Dispatchers.IO) {
            try {
                val binaryFile = ensureBinaryDownloaded(ctx)
                binaryFile.setExecutable(true)

                Logger.i("Starting cloudflared tunnel...")

                val processBuilder = ProcessBuilder(
                    binaryFile.absolutePath,
                    "tunnel",
                    "--url",
                    "ws://localhost:8765"
                )
                processBuilder.redirectErrorStream(true)
                process = processBuilder.start()

                _isRunning.value = true

                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                var tunnelUrlStr: String? = null
                val maxWaitMs = 30_000L
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < maxWaitMs) {
                    line = reader.readLine()
                    if (line == null) break

                    Logger.d("cloudflared: $line")

                    val urlMatch = Regex("""https://[a-zA-Z0-9\-]+\.trycloudflare\.com""").find(line)
                    if (urlMatch != null) {
                        tunnelUrlStr = urlMatch.value
                        _tunnelUrl.value = tunnelUrlStr
                        Logger.i("Tunnel URL: $tunnelUrlStr")
                        break
                    }
                }

                if (tunnelUrlStr == null) {
                    tunnelUrlStr = "Tunnel started but URL not yet resolved"
                }

                startOutputPump(reader)

                tunnelUrlStr
            } catch (e: Exception) {
                Logger.e("Failed to start tunnel", e)
                _isRunning.value = false
                _tunnelUrl.value = null
                "Error: ${e.message}"
            }
        }
    }

    private fun startOutputPump(reader: BufferedReader) {
        Thread {
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    Logger.d("cloudflared: $line")

                    val urlMatch = Regex("""https://[a-zA-Z0-9\-]+\.trycloudflare\.com""").find(line!!)
                    if (urlMatch != null) {
                        _tunnelUrl.value = urlMatch.value
                        Logger.i("Tunnel URL updated: ${urlMatch.value}")
                    }
                }
            } catch (e: Exception) {
                Logger.d("cloudflared output stream ended")
            }
        }.start()
    }

    fun stopTunnel() {
        try {
            process?.destroy()
            process = null
            _isRunning.value = false
            _tunnelUrl.value = null
            Logger.i("Tunnel stopped")
        } catch (e: Exception) {
            Logger.e("Error stopping tunnel", e)
        }
    }

    fun isRunning(): Boolean = _isRunning.value

    private suspend fun ensureBinaryDownloaded(ctx: Context): File {
        val binaryDir = File(ctx.filesDir, "cloudflared")
        if (!binaryDir.exists()) binaryDir.mkdirs()

        val binaryFile = File(binaryDir, "cloudflared")

        if (binaryFile.exists() && binaryFile.length() > 1_000_000) {
            Logger.i("cloudflared binary already exists")
            return binaryFile
        }

        Logger.i("Downloading cloudflared binary for ARM64...")

        withContext(Dispatchers.IO) {
            val downloadUrl = URL(
                "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-arm64"
            )
            val connection = downloadUrl.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 120_000
            connection.followRedirects = true

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Failed to download cloudflared: HTTP ${connection.responseCode}")
            }

            connection.inputStream.use { input ->
                binaryFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        Logger.i("cloudflared downloaded: ${binaryFile.length()} bytes")
        return binaryFile
    }

    companion object {
        @Volatile
        private var instance: TunnelManager? = null

        fun getInstance(): TunnelManager {
            return instance ?: synchronized(this) {
                instance ?: TunnelManager().also { instance = it }
            }
        }
    }
}
