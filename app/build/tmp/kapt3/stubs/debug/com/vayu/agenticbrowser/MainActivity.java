package com.vayu.agenticbrowser;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier;
import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.GoalScheduler;
import com.vayu.agenticbrowser.brain.WorkflowEngine;
import com.vayu.agenticbrowser.common.Logger;
import com.vayu.agenticbrowser.common.NetworkMonitor;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.WebViewManager;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.vault.ProfileManager;
import com.vayu.agenticbrowser.vault.SmsOtpReader;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010M\u001a\u00020N2\b\u0010O\u001a\u0004\u0018\u00010PH\u0014J\b\u0010Q\u001a\u00020NH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001e\u0010\u0011\u001a\u00020\u00128\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001e\u0010\u0017\u001a\u00020\u00188\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u001e\u0010\u001d\u001a\u00020\u001e8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001e\u0010#\u001a\u00020$8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b\'\u0010(R\u001e\u0010)\u001a\u00020*8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b+\u0010,\"\u0004\b-\u0010.R\u001e\u0010/\u001a\u0002008\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u001e\u00105\u001a\u0002068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b7\u00108\"\u0004\b9\u0010:R\u001e\u0010;\u001a\u00020<8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b=\u0010>\"\u0004\b?\u0010@R\u001e\u0010A\u001a\u00020B8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\bC\u0010D\"\u0004\bE\u0010FR\u001e\u0010G\u001a\u00020H8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\bI\u0010J\"\u0004\bK\u0010L\u00a8\u0006R"}, d2 = {"Lcom/vayu/agenticbrowser/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "activityScope", "Lkotlinx/coroutines/CoroutineScope;", "agentLoop", "Lcom/vayu/agenticbrowser/brain/AgentLoop;", "getAgentLoop", "()Lcom/vayu/agenticbrowser/brain/AgentLoop;", "setAgentLoop", "(Lcom/vayu/agenticbrowser/brain/AgentLoop;)V", "downloadManager", "Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;", "getDownloadManager", "()Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;", "setDownloadManager", "(Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;)V", "goalScheduler", "Lcom/vayu/agenticbrowser/brain/GoalScheduler;", "getGoalScheduler", "()Lcom/vayu/agenticbrowser/brain/GoalScheduler;", "setGoalScheduler", "(Lcom/vayu/agenticbrowser/brain/GoalScheduler;)V", "mcpServer", "Lcom/vayu/agenticbrowser/agent/McpServer;", "getMcpServer", "()Lcom/vayu/agenticbrowser/agent/McpServer;", "setMcpServer", "(Lcom/vayu/agenticbrowser/agent/McpServer;)V", "networkMonitor", "Lcom/vayu/agenticbrowser/common/NetworkMonitor;", "getNetworkMonitor", "()Lcom/vayu/agenticbrowser/common/NetworkMonitor;", "setNetworkMonitor", "(Lcom/vayu/agenticbrowser/common/NetworkMonitor;)V", "pluginRegistry", "Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "getPluginRegistry", "()Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "setPluginRegistry", "(Lcom/vayu/agenticbrowser/plugins/PluginRegistry;)V", "profileManager", "Lcom/vayu/agenticbrowser/vault/ProfileManager;", "getProfileManager", "()Lcom/vayu/agenticbrowser/vault/ProfileManager;", "setProfileManager", "(Lcom/vayu/agenticbrowser/vault/ProfileManager;)V", "sessionRecorder", "Lcom/vayu/agenticbrowser/agent/SessionRecorder;", "getSessionRecorder", "()Lcom/vayu/agenticbrowser/agent/SessionRecorder;", "setSessionRecorder", "(Lcom/vayu/agenticbrowser/agent/SessionRecorder;)V", "smsOtpReader", "Lcom/vayu/agenticbrowser/vault/SmsOtpReader;", "getSmsOtpReader", "()Lcom/vayu/agenticbrowser/vault/SmsOtpReader;", "setSmsOtpReader", "(Lcom/vayu/agenticbrowser/vault/SmsOtpReader;)V", "tabManager", "Lcom/vayu/agenticbrowser/tabs/TabManager;", "getTabManager", "()Lcom/vayu/agenticbrowser/tabs/TabManager;", "setTabManager", "(Lcom/vayu/agenticbrowser/tabs/TabManager;)V", "tunnelManager", "Lcom/vayu/agenticbrowser/tunnel/TunnelManager;", "getTunnelManager", "()Lcom/vayu/agenticbrowser/tunnel/TunnelManager;", "setTunnelManager", "(Lcom/vayu/agenticbrowser/tunnel/TunnelManager;)V", "workflowEngine", "Lcom/vayu/agenticbrowser/brain/WorkflowEngine;", "getWorkflowEngine", "()Lcom/vayu/agenticbrowser/brain/WorkflowEngine;", "setWorkflowEngine", "(Lcom/vayu/agenticbrowser/brain/WorkflowEngine;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.agent.McpServer mcpServer;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.tabs.TabManager tabManager;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.vault.ProfileManager profileManager;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.vault.SmsOtpReader smsOtpReader;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.tunnel.TunnelManager tunnelManager;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.agent.SessionRecorder sessionRecorder;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.common.NetworkMonitor networkMonitor;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.brain.AgentLoop agentLoop;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.brain.GoalScheduler goalScheduler;
    @javax.inject.Inject()
    public com.vayu.agenticbrowser.brain.WorkflowEngine workflowEngine;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope activityScope = null;
    
    public MainActivity() {
        super(0);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.agent.McpServer getMcpServer() {
        return null;
    }
    
    public final void setMcpServer(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.McpServer p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tabs.TabManager getTabManager() {
        return null;
    }
    
    public final void setTabManager(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.downloads.VayuDownloadManager getDownloadManager() {
        return null;
    }
    
    public final void setDownloadManager(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.downloads.VayuDownloadManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.ProfileManager getProfileManager() {
        return null;
    }
    
    public final void setProfileManager(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.ProfileManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.SmsOtpReader getSmsOtpReader() {
        return null;
    }
    
    public final void setSmsOtpReader(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.SmsOtpReader p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.plugins.PluginRegistry getPluginRegistry() {
        return null;
    }
    
    public final void setPluginRegistry(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.PluginRegistry p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tunnel.TunnelManager getTunnelManager() {
        return null;
    }
    
    public final void setTunnelManager(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tunnel.TunnelManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.agent.SessionRecorder getSessionRecorder() {
        return null;
    }
    
    public final void setSessionRecorder(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.SessionRecorder p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.common.NetworkMonitor getNetworkMonitor() {
        return null;
    }
    
    public final void setNetworkMonitor(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.common.NetworkMonitor p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.AgentLoop getAgentLoop() {
        return null;
    }
    
    public final void setAgentLoop(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.AgentLoop p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.GoalScheduler getGoalScheduler() {
        return null;
    }
    
    public final void setGoalScheduler(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.GoalScheduler p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.WorkflowEngine getWorkflowEngine() {
        return null;
    }
    
    public final void setWorkflowEngine(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.WorkflowEngine p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
}