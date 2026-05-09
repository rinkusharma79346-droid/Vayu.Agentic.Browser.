package com.vayu.agenticbrowser.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.agent.SessionRecorder
import com.vayu.agenticbrowser.common.NetworkMonitor
import com.vayu.agenticbrowser.engine.StealthController
import com.vayu.agenticbrowser.plugins.PluginRegistry
import com.vayu.agenticbrowser.tunnel.TunnelManager
import kotlinx.coroutines.launch
import java.net.NetworkInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tunnelManager = remember { TunnelManager.getInstance() }
    val pluginRegistry = remember { PluginRegistry.getInstance() }
    val sessionRecorder = remember { SessionRecorder.getInstance() }
    val networkMonitor = remember { NetworkMonitor.getInstance() }

    val tunnelUrl by tunnelManager.tunnelUrl.collectAsState()
    val tunnelRunning by tunnelManager.isRunning.collectAsState()
    val allPlugins by pluginRegistry.allPlugins.collectAsState()
    val recordings by remember { mutableStateOf(sessionRecorder.listRecordings()) }
    val isRecording by sessionRecorder.isRecording.collectAsState()
    val networkConnected by networkMonitor.isConnected.collectAsState()

    var wsPort by remember { mutableStateOf("8765") }
    var pairingCode by remember { mutableStateOf("vayu1234") }
    var showPairingCode by remember { mutableStateOf(false) }
    var userAgentText by remember { mutableStateOf(StealthController.getDefaultUserAgent()) }
    var humanTyping by remember { mutableStateOf(StealthController.isHumanTypingEnabled()) }
    var webdriverSpoof by remember { mutableStateOf(StealthController.isStealthModeEnabled()) }

    // Collapsible section states
    var connectionExpanded by remember { mutableStateOf(true) }
    var accountsExpanded by remember { mutableStateOf(false) }
    var pluginsExpanded by remember { mutableStateOf(false) }
    var stealthExpanded by remember { mutableStateOf(false) }
    var downloadsExpanded by remember { mutableStateOf(false) }
    var recordingsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ===== Connection Section =====
            SectionHeader(
                title = "Connection",
                icon = Icons.Default.Wifi,
                expanded = connectionExpanded,
                onToggle = { connectionExpanded = !connectionExpanded }
            )

            AnimatedVisibility(visible = connectionExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Network status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = MaterialTheme.shapes.small,
                            color = if (networkConnected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                        ) {}
                        Text(
                            text = if (networkConnected) "Network: Connected" else "Network: Disconnected",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // WebSocket port
                    OutlinedTextField(
                        value = wsPort,
                        onValueChange = { wsPort = it },
                        label = { Text("WebSocket Port") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Pairing code
                    OutlinedTextField(
                        value = pairingCode,
                        onValueChange = { pairingCode = it },
                        label = { Text("Pairing Code") },
                        visualTransformation = if (showPairingCode) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPairingCode = !showPairingCode }) {
                                Icon(
                                    if (showPairingCode) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Tunnel toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cloudflare Tunnel", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = tunnelRunning,
                            onCheckedChange = { enable ->
                                scope.launch {
                                    if (enable) {
                                        tunnelManager.startTunnel()
                                    } else {
                                        tunnelManager.stopTunnel()
                                    }
                                }
                            }
                        )
                    }

                    // Tunnel URL display
                    if (tunnelUrl != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Tunnel URL", tunnelUrl))
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = tunnelUrl ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Copy WS URL button
                    Button(
                        onClick = {
                            val localIp = getLocalIpAddress()
                            val wsUrl = "ws://$localIp:$wsPort/mcp"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("WS URL", wsUrl))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy WS URL")
                    }
                }
            }

            HorizontalDivider()

            // ===== Accounts Section =====
            SectionHeader(
                title = "Accounts",
                icon = Icons.Default.Lock,
                expanded = accountsExpanded,
                onToggle = { accountsExpanded = !accountsExpanded }
            )

            AnimatedVisibility(visible = accountsExpanded) {
                Button(
                    onClick = onNavigateToVault,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Credential Vault")
                }
            }

            HorizontalDivider()

            // ===== Plugins Section =====
            SectionHeader(
                title = "Plugins",
                icon = Icons.Default.Extension,
                expanded = pluginsExpanded,
                onToggle = { pluginsExpanded = !pluginsExpanded }
            )

            AnimatedVisibility(visible = pluginsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allPlugins.forEach { plugin ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plugin.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "v${plugin.version} — ${plugin.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "Sites: ${plugin.sites.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Switch(
                                    checked = plugin.enabled,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            pluginRegistry.enablePlugin(plugin.name)
                                        } else {
                                            pluginRegistry.disablePlugin(plugin.name)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Open Plugins Folder
                    OutlinedButton(
                        onClick = {
                            val pluginsDir = java.io.File(
                                context.getExternalFilesDir(null),
                                "Plugins"
                            )
                            if (!pluginsDir.exists()) pluginsDir.mkdirs()

                            val uri = Uri.fromFile(pluginsDir)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "resource/folder")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallback = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata%2Fcom.vayu.agenticbrowser%2Ffiles%2FPlugins")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(fallback)
                                } catch (e2: Exception) {
                                    // No file manager available
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Plugins Folder")
                    }
                }
            }

            HorizontalDivider()

            // ===== Stealth Section =====
            SectionHeader(
                title = "Stealth",
                icon = Icons.Default.Security,
                expanded = stealthExpanded,
                onToggle = { stealthExpanded = !stealthExpanded }
            )

            AnimatedVisibility(visible = stealthExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = userAgentText,
                        onValueChange = { userAgentText = it },
                        label = { Text("User Agent") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // UA presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StealthController.USER_AGENT_PRESETS.forEach { (name, ua) ->
                            OutlinedButton(
                                onClick = { userAgentText = ua },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(name, fontSize = 10.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Human Typing Simulation", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = humanTyping,
                            onCheckedChange = { enabled ->
                                humanTyping = enabled
                                StealthController.enableHumanTypingSimulation(enabled)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Webdriver Spoofing", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = webdriverSpoof,
                            onCheckedChange = { enabled ->
                                webdriverSpoof = enabled
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // ===== Downloads Section =====
            SectionHeader(
                title = "Downloads",
                icon = Icons.Default.Download,
                expanded = downloadsExpanded,
                onToggle = { downloadsExpanded = !downloadsExpanded }
            )

            AnimatedVisibility(visible = downloadsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val downloadPath = "Android/data/com.vayu.agenticbrowser/files/Downloads"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = downloadPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val downloadCount = com.vayu.agenticbrowser.downloads.VayuDownloadManager
                        .getInstance().downloads.value.size
                    Text(
                        text = "Download history: $downloadCount items",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Open SAF folder picker
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Change Folder", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // Clear download history
                                com.vayu.agenticbrowser.downloads.VayuDownloadManager
                                    .getInstance().clearHistory()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear History", fontSize = 12.sp)
                        }
                    }
                }
            }

            HorizontalDivider()

            // ===== Recordings Section =====
            SectionHeader(
                title = "Recordings",
                icon = Icons.Default.FiberManualRecord,
                expanded = recordingsExpanded,
                onToggle = { recordingsExpanded = !recordingsExpanded }
            )

            AnimatedVisibility(visible = recordingsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isRecording) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(10.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.error
                                ) {}
                                Text(
                                    text = "Recording in progress...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    if (recordings.isEmpty()) {
                        Text(
                            text = "No saved recordings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        recordings.forEach { recording ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = recording.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${recording.commands.size} commands — ${formatDate(recording.createdAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            scope.launch {
                                                sessionRecorder.replayRecording(recording.id) { tool, args ->
                                                    """{"replayed":true,"tool":"$tool"}"""
                                                }
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Replay",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = {
                                            sessionRecorder.deleteRecording(recording.id)
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand"
        )
    }
}

private fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
        for (intf in interfaces) {
            if (intf.isLoopback || !intf.isUp) continue
            for (addr in intf.inetAddresses) {
                if (addr.isLoopbackAddress) continue
                val hostAddress = addr.hostAddress ?: continue
                if (hostAddress.contains(":")) continue // Skip IPv6
                return hostAddress
            }
        }
    } catch (e: Exception) {
        // Fall through
    }
    return "127.0.0.1"
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
