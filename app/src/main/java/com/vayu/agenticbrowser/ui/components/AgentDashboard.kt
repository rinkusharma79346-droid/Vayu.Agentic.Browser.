package com.vayu.agenticbrowser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.brain.AgentState
import com.vayu.agenticbrowser.downloads.DownloadStatus
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AgentDashboard(
    agentConnected: StateFlow<Boolean>,
    lastToolName: String,
    tabCount: Int,
    activeTabIndex: Int,
    downloadManager: VayuDownloadManager,
    isRecording: Boolean = false,
    stealthEnabled: Boolean = false,
    agentState: AgentState = AgentState.IDLE,
    currentGoal: String? = null,
    agentStepCount: Int = 0
) {
    val isConnected by agentConnected.collectAsState()
    val downloads by downloadManager.downloads.collectAsState()

    val activeDownloads = downloads.values.count {
        it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING
    }

    var isMinimized by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Agent state color
    val agentStateColor = when (agentState) {
        AgentState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
        AgentState.THINKING -> MaterialTheme.colorScheme.tertiary
        AgentState.EXECUTING -> MaterialTheme.colorScheme.primary
        AgentState.WAITING -> MaterialTheme.colorScheme.secondary
        AgentState.COMPLETED -> MaterialTheme.colorScheme.primary
        AgentState.FAILED -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (isMinimized) {
            // Minimized: connection dot with agent state color ring
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    shape = CircleShape,
                    color = if (isConnected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                    onClick = { isMinimized = false }
                ) {}
                if (agentState != AgentState.IDLE) {
                    Surface(
                        shape = CircleShape,
                        color = agentStateColor,
                        modifier = Modifier.size(10.dp)
                    ) {}
                } else if (isRecording) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(10.dp)
                    ) {}
                }
            }
        } else {
            // Expanded dashboard
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                modifier = Modifier.width(220.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header row with minimize button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                            )
                            Text(
                                text = if (isConnected) "Connected" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { isMinimized = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.ExpandLess,
                                contentDescription = "Minimize",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // Agent state indicator
                    if (agentState != AgentState.IDLE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = agentStateColor
                            ) {}
                            Text(
                                text = agentState.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = agentStateColor
                            )
                            Text(
                                text = "($agentStepCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Current goal (truncated to 30 chars)
                        if (currentGoal != null) {
                            Text(
                                text = currentGoal.take(30) + if (currentGoal.length > 30) "..." else "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Last tool
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(46.dp)
                        )
                        Text(
                            text = lastToolName.ifBlank { "—" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Tab info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tab:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(46.dp)
                        )
                        Text(
                            text = "$activeTabIndex / $tabCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Downloads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DL:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(46.dp)
                        )
                        Text(
                            text = if (activeDownloads > 0) "$activeDownloads active" else "none",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (activeDownloads > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Recording indicator
                    if (isRecording) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error
                            ) {}
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Stealth indicator
                    if (stealthEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary
                            ) {}
                            Text(
                                text = "STEALTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
