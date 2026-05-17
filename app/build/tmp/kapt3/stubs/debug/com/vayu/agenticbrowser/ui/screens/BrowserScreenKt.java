package com.vayu.agenticbrowser.ui.screens;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.WebView;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.style.TextOverflow;
import com.vayu.agenticbrowser.agent.McpConfig;
import com.vayu.agenticbrowser.agent.McpStatus;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.downloads.DownloadStatus;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.StealthController;
import com.vayu.agenticbrowser.engine.WebViewManager;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tabs.TabState;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.AgentState;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u001aR\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0003H\u0007\u001a4\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00042\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u00a8\u0006\u0012"}, d2 = {"BrowserScreen", "", "agentConnected", "Lkotlinx/coroutines/flow/StateFlow;", "", "onNavigateToSettings", "Lkotlin/Function0;", "onNavigateToBrain", "agentLoop", "Lcom/vayu/agenticbrowser/brain/AgentLoop;", "mcpStatus", "Lcom/vayu/agenticbrowser/agent/McpStatus;", "TabChip", "tab", "Lcom/vayu/agenticbrowser/tabs/TabState;", "isActive", "onSelect", "onClose", "app_debug"})
public final class BrowserScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void BrowserScreen(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> agentConnected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToBrain, @org.jetbrains.annotations.Nullable()
    com.vayu.agenticbrowser.brain.AgentLoop agentLoop, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends com.vayu.agenticbrowser.agent.McpStatus> mcpStatus) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TabChip(com.vayu.agenticbrowser.tabs.TabState tab, boolean isActive, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
}