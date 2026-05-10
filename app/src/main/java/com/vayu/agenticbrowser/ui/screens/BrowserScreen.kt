package com.vayu.agenticbrowser.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vayu.agenticbrowser.agent.SessionRecorder
import com.vayu.agenticbrowser.downloads.DownloadStatus
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.StealthController
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.tabs.TabState
import com.vayu.agenticbrowser.tunnel.TunnelManager
import com.vayu.agenticbrowser.brain.AgentLoop
import com.vayu.agenticbrowser.brain.AgentState
import com.vayu.agenticbrowser.ui.components.AgentDashboard
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    agentConnected: StateFlow<Boolean>,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBrain: () -> Unit = {},
    agentLoop: AgentLoop? = null
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf("https://www.google.com") }
    val isConnected by agentConnected.collectAsState()

    val webViewManager = remember { WebViewManager.getInstance() }
    val tabManager = remember { TabManager.getInstance() }
    val downloadMgr = remember { VayuDownloadManager.getInstance() }
    val tunnelManager = remember { TunnelManager.getInstance() }
    val sessionRecorder = remember { SessionRecorder.getInstance() }

    val tabs by tabManager.tabs.collectAsState()
    val activeTabId by tabManager.activeTabId.collectAsState()
    val downloads by downloadMgr.downloads.collectAsState()
    val tunnelUrl by tunnelManager.tunnelUrl.collectAsState()
    val isRecording by sessionRecorder.isRecording.collectAsState()

    val activeDownloads = downloads.values.count {
        it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING
    }

    val stealthEnabled = StealthController.isStealthModeEnabled()

    // Agent state for dashboard
    val agentState by (agentLoop?.state ?: kotlinx.coroutines.flow.MutableStateFlow(AgentState.IDLE)).collectAsState()
    val currentGoal by (agentLoop?.currentGoal ?: kotlinx.coroutines.flow.MutableStateFlow<String?>(null)).collectAsState()
    val agentStepCount by (agentLoop?.totalSteps ?: kotlinx.coroutines.flow.MutableStateFlow(0)).collectAsState()

    // Initialize tab manager with context on first composition
    LaunchedEffect(Unit) {
        tabManager.init(context)
        downloadMgr.init(context)
    }

    // Create initial tab if none exists
    LaunchedEffect(tabs.isEmpty()) {
        if (tabs.isEmpty()) {
            tabManager.newTab("https://www.google.com", background = false)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // URL Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var url = urlText.trim()
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "https://$url"
                                urlText = url
                            }
                            if (activeTabId != -1) {
                                tabManager.loadUrlInTab(activeTabId, url)
                            } else {
                                webViewManager.loadUrl(url)
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        var url = urlText.trim()
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://$url"
                            urlText = url
                        }
                        if (activeTabId != -1) {
                            tabManager.loadUrlInTab(activeTabId, url)
                        } else {
                            webViewManager.loadUrl(url)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go"
                    )
                }

                // Brain icon
                IconButton(onClick = onNavigateToBrain) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Brain"
                    )
                }

                // Settings gear icon
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }

        // Tab Strip
        Surface(
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    TabChip(
                        tab = tab,
                        isActive = tab.tabId == activeTabId,
                        onSelect = { tabManager.switchTab(tab.tabId) },
                        onClose = { tabManager.closeTab(tab.tabId) }
                    )
                }

                // New Tab Button
                IconButton(
                    onClick = {
                        tabManager.newTab("https://www.google.com", background = false)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // WebView - shows active tab's WebView
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTabId != -1) {
                val activeWebView = tabManager.getTab(activeTabId)
                if (activeWebView != null) {
                    AndroidView(
                        factory = { _ ->
                            activeWebView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Fallback to the legacy WebViewManager if no tabs
                AndroidView(
                    factory = { ctx ->
                        webViewManager.init(ctx)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Agent Dashboard floating overlay
            AgentDashboard(
                agentConnected = agentConnected,
                lastToolName = "",
                tabCount = tabs.size,
                activeTabIndex = tabs.indexOfFirst { it.tabId == activeTabId }.coerceAtLeast(0),
                downloadManager = downloadMgr,
                isRecording = isRecording,
                stealthEnabled = stealthEnabled,
                agentState = agentState,
                currentGoal = currentGoal,
                agentStepCount = agentStepCount
            )
        }

        // Status Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (isConnected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                    ) {}
                    Text(
                        text = if (isConnected) "Agent: Connected" else "Agent: Disconnected",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Recording indicator
                    if (isRecording) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.error
                        ) {}
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Stealth indicator
                    if (stealthEnabled) {
                        Text(
                            text = "STEALTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tunnel URL chip
                    if (tunnelUrl != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Tunnel URL", tunnelUrl))
                            }
                        ) {
                            Text(
                                text = "Tunnel: ${tunnelUrl!!.take(30)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (activeDownloads > 0) {
                        Text(
                            text = "\u2193 $activeDownloads active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Tabs: ${tabs.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "MCP Port: 8765",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    tab: TabState,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(28.dp),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (tab.title.isNotEmpty()) tab.title else tab.url,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp),
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (tab.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(
        androidx.compose.foundation.clickable(onClick = onClick)
    )
