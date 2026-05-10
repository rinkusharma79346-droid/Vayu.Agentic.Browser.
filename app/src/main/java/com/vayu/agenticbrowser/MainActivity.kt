package com.vayu.agenticbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.agent.SessionRecorder
import com.vayu.agenticbrowser.brain.AgentLoop
import com.vayu.agenticbrowser.brain.GoalScheduler
import com.vayu.agenticbrowser.brain.WorkflowEngine
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.common.NetworkMonitor
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.plugins.PluginRegistry
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.tunnel.TunnelManager
import com.vayu.agenticbrowser.ui.screens.BrainScreen
import com.vayu.agenticbrowser.ui.screens.BrowserScreen
import com.vayu.agenticbrowser.ui.screens.SettingsScreen
import com.vayu.agenticbrowser.ui.screens.SplashScreen
import com.vayu.agenticbrowser.ui.screens.VaultScreen
import com.vayu.agenticbrowser.ui.theme.VAYUTheme
import com.vayu.agenticbrowser.vault.ProfileManager
import com.vayu.agenticbrowser.vault.SmsOtpReader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var mcpServer: McpServer
    @Inject lateinit var tabManager: TabManager
    @Inject lateinit var downloadManager: VayuDownloadManager
    @Inject lateinit var profileManager: ProfileManager
    @Inject lateinit var smsOtpReader: SmsOtpReader
    @Inject lateinit var pluginRegistry: PluginRegistry
    @Inject lateinit var tunnelManager: TunnelManager
    @Inject lateinit var sessionRecorder: SessionRecorder
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var agentLoop: AgentLoop
    @Inject lateinit var goalScheduler: GoalScheduler
    @Inject lateinit var workflowEngine: WorkflowEngine

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Logger.i("MainActivity created")

        // Initialize managers with context
        tabManager.init(applicationContext)
        downloadManager.init(applicationContext)
        profileManager.init(applicationContext)
        smsOtpReader.init(applicationContext)
        pluginRegistry.init(applicationContext)
        tunnelManager.init(applicationContext)
        sessionRecorder.init(applicationContext)
        networkMonitor.init(applicationContext)
        goalScheduler.init(applicationContext)
        mcpServer.setContext(applicationContext)

        // Start MCP server on app launch
        activityScope.launch(Dispatchers.IO) {
            try {
                mcpServer.start()
                Logger.i("MCP Server started successfully")
            } catch (e: Exception) {
                Logger.e("Failed to start MCP Server", e)
            }
        }

        // Start foreground service to keep MCP alive
        McpForegroundService.start(applicationContext)

        setContent {
            VAYUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onSplashComplete = { showSplash = false })
                    } else {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = "browser",
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(350)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(350)
                                )
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(350)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(350)
                                )
                            }
                        ) {
                            composable("browser") {
                                BrowserScreen(
                                    agentConnected = mcpServer.isRunning,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToBrain = { navController.navigate("brain") },
                                    agentLoop = agentLoop
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    onNavigateToVault = { navController.navigate("vault") },
                                    onNavigateToBrain = { navController.navigate("brain") },
                                    agentLoop = agentLoop
                                )
                            }
                            composable("vault") {
                                VaultScreen(onBack = { navController.popBackStack() })
                            }
                            composable("brain") {
                                BrainScreen(
                                    onBack = { navController.popBackStack() },
                                    agentLoop = agentLoop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mcpServer.stop()
        tabManager.destroyAll()
        WebViewManager.getInstance().destroy()
        networkMonitor.unregister()
        Logger.i("MainActivity destroyed")
    }
}
