package com.vayu.agenticbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.ui.screens.BrowserScreen
import com.vayu.agenticbrowser.ui.theme.VAYUTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mcpServer: McpServer

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var downloadManager: VayuDownloadManager

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Logger.i("MainActivity created")

        // Initialize managers with context
        tabManager.init(applicationContext)
        downloadManager.init(applicationContext)

        // Start MCP server on app launch
        activityScope.launch(Dispatchers.IO) {
            try {
                mcpServer.start()
                Logger.i("MCP Server started successfully")
            } catch (e: Exception) {
                Logger.e("Failed to start MCP Server", e)
            }
        }

        setContent {
            VAYUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserScreen(
                        agentConnected = mcpServer.isRunning
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mcpServer.stop()
        tabManager.destroyAll()
        WebViewManager.getInstance().destroy()
        Logger.i("MainActivity destroyed")
    }
}
