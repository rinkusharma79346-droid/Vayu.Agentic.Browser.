package com.vayu.agenticbrowser.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.vayu.agenticbrowser.ui.theme.*
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

    // Pulse animation for agent active indicator
    val infiniteTransition = rememberInfiniteTransition(label = "agentPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // ===== Immersive URL Bar with gradient background =====
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // URL bar with custom styling
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Enter URL",
                                color = VayuOnSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VayuCyan,
                            unfocusedBorderColor = VayuOnSurfaceDim,
                            focusedContainerColor = VayuSurfaceElevated,
                            unfocusedContainerColor = VayuSurfaceElevated,
                            cursorColor = VayuCyan,
                            focusedTextColor = VayuOnSurface,
                            unfocusedTextColor = VayuOnSurface
                        ),
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
                        ),
                        leadingIcon = {
                            if (isConnected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VayuSuccess)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VayuError)
                                )
                            }
                        }
                    )

                    // Go button
                    FilledIconButton(
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
                        },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = VayuCyan,
                            contentColor = VayuNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Brain icon — animated when agent is active
                    BadgedBox(
                        badge = {
                            if (agentState != AgentState.IDLE) {
                                Badge(
                                    containerColor = when (agentState) {
                                        AgentState.THINKING -> VayuViolet
                                        AgentState.EXECUTING -> VayuCyan
                                        AgentState.COMPLETED -> VayuSuccess
                                        AgentState.FAILED -> VayuError
                                        else -> VayuOnSurfaceDim
                                    },
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToBrain) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Brain",
                                tint = if (agentState != AgentState.IDLE) VayuViolet
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .let { mod ->
                                        if (agentState == AgentState.THINKING)
                                            mod.alpha(pulseAlpha)
                                        else mod
                                    }
                            )
                        }
                    }

                    // Settings gear icon
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // ===== Tab Strip — sleek horizontal scroll =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        ImmersiveTabChip(
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
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Tab",
                            modifier = Modifier.size(16.dp),
                            tint = VayuCyan
                        )
                    }
                }
            }
        }

        // ===== WebView Content Area =====
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTabId != -1) {
                val activeWebView = tabManager.getTab(activeTabId)
                if (activeWebView != null) {
                    AndroidView(
                        factory = { _ -> activeWebView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                AndroidView(
                    factory = { ctx -> webViewManager.init(ctx) },
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

        // ===== Immersive Status Bar =====
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Connection indicator with glow
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) VayuSuccess else VayuError
                            )
                    )
                    Text(
                        text = if (isConnected) "Connected" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected) VayuSuccess else VayuError
                    )

                    // Recording indicator
                    if (isRecording) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VayuError.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(VayuError)
                                        .alpha(pulseAlpha)
                                )
                                Text(
                                    text = "REC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VayuError,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Stealth indicator
                    if (stealthEnabled) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VayuTeal.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "STEALTH",
                                style = MaterialTheme.typography.labelSmall,
                                color = VayuTeal,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Agent state indicator
                    if (agentState != AgentState.IDLE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (agentState) {
                                AgentState.THINKING -> VayuViolet.copy(alpha = 0.2f)
                                AgentState.EXECUTING -> VayuCyan.copy(alpha = 0.2f)
                                AgentState.COMPLETED -> VayuSuccess.copy(alpha = 0.2f)
                                AgentState.FAILED -> VayuError.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }
                        ) {
                            Text(
                                text = agentState.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (agentState) {
                                    AgentState.THINKING -> VayuViolet
                                    AgentState.EXECUTING -> VayuCyan
                                    AgentState.COMPLETED -> VayuSuccess
                                    AgentState.FAILED -> VayuError
                                    else -> VayuOnSurfaceVariant
                                },
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tunnel URL chip
                    if (tunnelUrl != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VayuTeal.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Tunnel URL", tunnelUrl))
                            }
                        ) {
                            Text(
                                text = "Tunnel: ${tunnelUrl!!.take(25)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = VayuTeal,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (activeDownloads > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "\u2193",
                                color = VayuCyan,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$activeDownloads",
                                style = MaterialTheme.typography.labelSmall,
                                color = VayuCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = "${tabs.size} tabs",
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuOnSurfaceVariant,
                        fontSize = 10.sp
                    )

                    Text(
                        text = "8765",
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuOnSurfaceDim,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveTabChip(
    tab: TabState,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = if (isActive) VayuCyan.copy(alpha = 0.15f)
                else VayuSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(30.dp),
        onClick = onSelect,
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(1.dp, VayuCyan.copy(alpha = 0.4f))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (tab.title.isNotEmpty()) tab.title else tab.url,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp),
                color = if (isActive) VayuCyan else VayuOnSurfaceVariant
            )

            if (tab.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = VayuCyan
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(10.dp),
                    tint = VayuOnSurfaceDim
                )
            }
        }
    }
}


