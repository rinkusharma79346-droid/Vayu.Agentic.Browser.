package com.vayu.agenticbrowser.di;

import android.content.Context;
import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.BrainClient;
import com.vayu.agenticbrowser.brain.GoalScheduler;
import com.vayu.agenticbrowser.brain.WorkflowEngine;
import com.vayu.agenticbrowser.common.NetworkMonitor;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.DialogController;
import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.engine.FormDetector;
import com.vayu.agenticbrowser.engine.WaitController;
import com.vayu.agenticbrowser.engine.WebViewManager;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.vault.BiometricAuth;
import com.vayu.agenticbrowser.vault.CredentialVault;
import com.vayu.agenticbrowser.vault.ProfileManager;
import com.vayu.agenticbrowser.vault.SmsOtpReader;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0098\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J2\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0007J\b\u0010\u000f\u001a\u00020\u0010H\u0007J\b\u0010\u0011\u001a\u00020\bH\u0007J(\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0007J\b\u0010\u001b\u001a\u00020\u001cH\u0007J\u0018\u0010\u001d\u001a\u00020\u00182\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0019\u001a\u00020\u001aH\u0007J\b\u0010 \u001a\u00020!H\u0007J\u0010\u0010\"\u001a\u00020#2\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J\b\u0010$\u001a\u00020\u000eH\u0007Jx\u0010%\u001a\u00020\f2\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010&\u001a\u00020!2\u0006\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00102\u0006\u0010*\u001a\u00020#2\u0006\u0010+\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010.\u001a\u00020/2\u0006\u00100\u001a\u0002012\u0006\u00102\u001a\u000203H\u0007J\b\u00104\u001a\u000203H\u0007J\b\u00105\u001a\u00020\nH\u0007J\b\u00106\u001a\u00020\u0015H\u0007J\b\u00107\u001a\u000201H\u0007J\b\u00108\u001a\u00020-H\u0007J\b\u00109\u001a\u00020\u001aH\u0007J\b\u0010:\u001a\u00020/H\u0007J\u0018\u0010;\u001a\u00020(2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010&\u001a\u00020!H\u0007J\b\u0010<\u001a\u00020\u001fH\u0007J\"\u0010=\u001a\u00020>2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010?\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0007\u00a8\u0006@"}, d2 = {"Lcom/vayu/agenticbrowser/di/AppModule;", "", "()V", "provideAgentLoop", "Lcom/vayu/agenticbrowser/brain/AgentLoop;", "ctx", "Landroid/content/Context;", "brainClient", "Lcom/vayu/agenticbrowser/brain/BrainClient;", "pluginRegistry", "Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "mcpServer", "Lcom/vayu/agenticbrowser/agent/McpServer;", "goalScheduler", "Lcom/vayu/agenticbrowser/brain/GoalScheduler;", "provideBiometricAuth", "Lcom/vayu/agenticbrowser/vault/BiometricAuth;", "provideBrainClient", "provideCredentialVault", "Lcom/vayu/agenticbrowser/vault/CredentialVault;", "profileManager", "Lcom/vayu/agenticbrowser/vault/ProfileManager;", "biometricAuth", "domController", "Lcom/vayu/agenticbrowser/engine/DomController;", "tabManager", "Lcom/vayu/agenticbrowser/tabs/TabManager;", "provideDialogController", "Lcom/vayu/agenticbrowser/engine/DialogController;", "provideDomController", "webViewManager", "Lcom/vayu/agenticbrowser/engine/WebViewManager;", "provideDownloadManager", "Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;", "provideFormDetector", "Lcom/vayu/agenticbrowser/engine/FormDetector;", "provideGoalScheduler", "provideMcpServer", "downloadManager", "waitController", "Lcom/vayu/agenticbrowser/engine/WaitController;", "credentialVault", "formDetector", "dialogController", "smsOtpReader", "Lcom/vayu/agenticbrowser/vault/SmsOtpReader;", "tunnelManager", "Lcom/vayu/agenticbrowser/tunnel/TunnelManager;", "sessionRecorder", "Lcom/vayu/agenticbrowser/agent/SessionRecorder;", "networkMonitor", "Lcom/vayu/agenticbrowser/common/NetworkMonitor;", "provideNetworkMonitor", "providePluginRegistry", "provideProfileManager", "provideSessionRecorder", "provideSmsOtpReader", "provideTabManager", "provideTunnelManager", "provideWaitController", "provideWebViewManager", "provideWorkflowEngine", "Lcom/vayu/agenticbrowser/brain/WorkflowEngine;", "agentLoop", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.engine.WebViewManager provideWebViewManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tabs.TabManager provideTabManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.downloads.VayuDownloadManager provideDownloadManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.ProfileManager provideProfileManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.BiometricAuth provideBiometricAuth() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.SmsOtpReader provideSmsOtpReader() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.plugins.PluginRegistry providePluginRegistry() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tunnel.TunnelManager provideTunnelManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.agent.SessionRecorder provideSessionRecorder() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.common.NetworkMonitor provideNetworkMonitor() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.GoalScheduler provideGoalScheduler() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.BrainClient provideBrainClient() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.engine.DomController provideDomController(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.WebViewManager webViewManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager tabManager) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.engine.WaitController provideWaitController(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager tabManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.engine.FormDetector provideFormDetector(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.DomController domController) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.engine.DialogController provideDialogController() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.vault.CredentialVault provideCredentialVault(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.ProfileManager profileManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.BiometricAuth biometricAuth, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.DomController domController, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager tabManager) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.agent.McpServer provideMcpServer(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.DomController domController, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager tabManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.WaitController waitController, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.CredentialVault credentialVault, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.ProfileManager profileManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.BiometricAuth biometricAuth, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.FormDetector formDetector, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.engine.DialogController dialogController, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.vault.SmsOtpReader smsOtpReader, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tunnel.TunnelManager tunnelManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.SessionRecorder sessionRecorder, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.common.NetworkMonitor networkMonitor) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.AgentLoop provideAgentLoop(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.BrainClient brainClient, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.McpServer mcpServer, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.GoalScheduler goalScheduler) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.WorkflowEngine provideWorkflowEngine(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.AgentLoop agentLoop, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.agent.McpServer mcpServer) {
        return null;
    }
}