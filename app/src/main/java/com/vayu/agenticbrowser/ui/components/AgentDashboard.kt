package com.vayu.agenticbrowser.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.brain.AgentState
import com.vayu.agenticbrowser.downloads.DownloadStatus
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.ui.theme.*
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
        AgentState.IDLE -> VayuOnSurfaceDim
        AgentState.THINKING -> VayuViolet
        AgentState.EXECUTING -> VayuCyan
        AgentState.WAITING -> VayuTeal
        AgentState.COMPLETED -> VayuSuccess
        AgentState.FAILED -> VayuError
    }

    // Pulse animation for active agent
    val infiniteTransition = rememberInfiniteTransition(label = "dashPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashPulse"
    )

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
            // Minimized: glowing connection dot with agent state ring
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    shape = CircleShape,
                    color = if (isConnected) VayuSuccess else VayuError,
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(4.dp, CircleShape),
                    onClick = { isMinimized = false }
                ) {}
                if (agentState != AgentState.IDLE) {
                    Surface(
                        shape = CircleShape,
                        color = agentStateColor.copy(alpha = pulseAlpha),
                        modifier = Modifier
                            .size(12.dp)
                            .shadow(2.dp, CircleShape)
                    ) {}
                } else if (isRecording) {
                    Surface(
                        shape = CircleShape,
                        color = VayuError.copy(alpha = pulseAlpha),
                        modifier = Modifier.size(10.dp)
                    ) {}
                }
            }
        } else {
            // Expanded dashboard — immersive glass-morphism card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = VayuSurfaceOverlay,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .width(240.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
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
                                        if (isConnected) VayuSuccess else VayuError
                                    )
                            )
                            Text(
                                text = if (isConnected) "Connected" else "Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                fontSize = 11.sp,
                                color = if (isConnected) VayuSuccess else VayuError
                            )
                        }

                        IconButton(
                            onClick = { isMinimized = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.ExpandLess,
                                contentDescription = "Minimize",
                                modifier = Modifier.size(14.dp),
                                tint = VayuOnSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        color = VayuOnSurfaceDim.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Agent state indicator
                    if (agentState != AgentState.IDLE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(agentStateColor.copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = agentState.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 10.sp,
                                color = agentStateColor
                            )
                            Text(
                                text = "($agentStepCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = VayuOnSurfaceVariant
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
                                color = VayuOnSurfaceVariant
                            )
                        }
                    }

                    // Last tool
                    DashboardInfoRow("Last:", lastToolName.ifBlank { "\u2014" })

                    // Tab info
                    DashboardInfoRow("Tab:", "$activeTabIndex / $tabCount")

                    // Downloads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DL:",
                            style = MaterialTheme.typography.labelSmall,
                            color = VayuOnSurfaceDim,
                            modifier = Modifier.width(38.dp),
                            fontSize = 10.sp
                        )
                        Text(
                            text = if (activeDownloads > 0) "$activeDownloads active" else "none",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (activeDownloads > 0) VayuCyan else VayuOnSurfaceVariant
                        )
                    }

                    // Recording indicator
                    if (isRecording) {
                        HorizontalDivider(
                            color = VayuOnSurfaceDim.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VayuError.copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 10.sp,
                                color = VayuError
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VayuTeal)
                            )
                            Text(
                                text = "STEALTH",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 10.sp,
                                color = VayuTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VayuOnSurfaceDim,
            modifier = Modifier.width(38.dp),
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            maxLines = 1,
            color = VayuOnSurface
        )
    }
}
