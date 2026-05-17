package com.vayu.agenticbrowser.ui.components;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextOverflow;
import com.vayu.agenticbrowser.agent.McpStatus;
import com.vayu.agenticbrowser.brain.AgentState;
import com.vayu.agenticbrowser.downloads.DownloadStatus;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001at\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00042\b\b\u0002\u0010\r\u001a\u00020\u00042\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0011\u001a\u00020\b2\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u0007\u00a8\u0006\u0014"}, d2 = {"AgentDashboard", "", "agentConnected", "Lkotlinx/coroutines/flow/StateFlow;", "", "lastToolName", "", "tabCount", "", "activeTabIndex", "downloadManager", "Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;", "isRecording", "stealthEnabled", "agentState", "Lcom/vayu/agenticbrowser/brain/AgentState;", "currentGoal", "agentStepCount", "mcpStatus", "Lcom/vayu/agenticbrowser/agent/McpStatus;", "app_debug"})
public final class AgentDashboardKt {
    
    @androidx.compose.runtime.Composable()
    public static final void AgentDashboard(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> agentConnected, @org.jetbrains.annotations.NotNull()
    java.lang.String lastToolName, int tabCount, int activeTabIndex, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager, boolean isRecording, boolean stealthEnabled, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.AgentState agentState, @org.jetbrains.annotations.Nullable()
    java.lang.String currentGoal, int agentStepCount, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.McpStatus mcpStatus) {
    }
}