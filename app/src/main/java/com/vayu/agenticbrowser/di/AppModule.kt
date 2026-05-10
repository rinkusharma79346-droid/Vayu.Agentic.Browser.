package com.vayu.agenticbrowser.di

import android.content.Context
import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.agent.SessionRecorder
import com.vayu.agenticbrowser.brain.AgentLoop
import com.vayu.agenticbrowser.brain.BrainClient
import com.vayu.agenticbrowser.brain.GoalScheduler
import com.vayu.agenticbrowser.brain.WorkflowEngine
import com.vayu.agenticbrowser.common.NetworkMonitor
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.DialogController
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.FormDetector
import com.vayu.agenticbrowser.engine.WaitController
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.plugins.PluginRegistry
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.tunnel.SshTunnelManager
import com.vayu.agenticbrowser.tunnel.TunnelManager
import com.vayu.agenticbrowser.vault.BiometricAuth
import com.vayu.agenticbrowser.vault.CredentialVault
import com.vayu.agenticbrowser.vault.ProfileManager
import com.vayu.agenticbrowser.vault.SmsOtpReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton fun provideWebViewManager(): WebViewManager = WebViewManager.getInstance()
    @Provides @Singleton fun provideTabManager(): TabManager = TabManager.getInstance()
    @Provides @Singleton fun provideDownloadManager(): VayuDownloadManager = VayuDownloadManager.getInstance()
    @Provides @Singleton fun provideProfileManager(): ProfileManager = ProfileManager.getInstance()
    @Provides @Singleton fun provideBiometricAuth(): BiometricAuth = BiometricAuth.getInstance()
    @Provides @Singleton fun provideSmsOtpReader(): SmsOtpReader = SmsOtpReader.getInstance()
    @Provides @Singleton fun providePluginRegistry(): PluginRegistry = PluginRegistry.getInstance()
    @Provides @Singleton fun provideTunnelManager(): TunnelManager = TunnelManager.getInstance()
    @Provides @Singleton fun provideSshTunnelManager(): SshTunnelManager = SshTunnelManager.getInstance()
    @Provides @Singleton fun provideSessionRecorder(): SessionRecorder = SessionRecorder.getInstance()
    @Provides @Singleton fun provideNetworkMonitor(): NetworkMonitor = NetworkMonitor.getInstance()
    @Provides @Singleton fun provideGoalScheduler(): GoalScheduler = GoalScheduler.getInstance()
    @Provides @Singleton fun provideBrainClient(): BrainClient = BrainClient()

    @Provides @Singleton
    fun provideDomController(webViewManager: WebViewManager, tabManager: TabManager): DomController =
        DomController(webViewManager, tabManager)

    @Provides @Singleton
    fun provideWaitController(tabManager: TabManager, downloadManager: VayuDownloadManager): WaitController =
        WaitController(tabManager, downloadManager)

    @Provides @Singleton
    fun provideFormDetector(domController: DomController): FormDetector = FormDetector(domController)

    @Provides @Singleton
    fun provideDialogController(): DialogController = DialogController()

    @Provides @Singleton
    fun provideCredentialVault(
        profileManager: ProfileManager, biometricAuth: BiometricAuth,
        domController: DomController, tabManager: TabManager
    ): CredentialVault = CredentialVault(profileManager, biometricAuth, domController, tabManager)

    @Provides @Singleton
    fun provideMcpServer(
        domController: DomController, tabManager: TabManager,
        downloadManager: VayuDownloadManager, waitController: WaitController,
        credentialVault: CredentialVault, profileManager: ProfileManager,
        biometricAuth: BiometricAuth, formDetector: FormDetector,
        dialogController: DialogController, smsOtpReader: SmsOtpReader,
        pluginRegistry: PluginRegistry, tunnelManager: TunnelManager,
        sessionRecorder: SessionRecorder, networkMonitor: NetworkMonitor,
        sshTunnelManager: SshTunnelManager
    ): McpServer = McpServer(
        domController, tabManager, downloadManager, waitController,
        credentialVault, profileManager, biometricAuth, formDetector, dialogController,
        smsOtpReader, pluginRegistry, tunnelManager, sessionRecorder, networkMonitor,
        sshTunnelManager
    )

    @Provides @Singleton
    fun provideAgentLoop(
        @ApplicationContext ctx: Context,
        brainClient: BrainClient,
        pluginRegistry: PluginRegistry,
        mcpServer: McpServer,
        goalScheduler: GoalScheduler
    ): AgentLoop {
        val agentLoop = AgentLoop(
            context = ctx,
            brainClient = brainClient,
            pluginRegistry = pluginRegistry,
            toolExecutor = { tool, args ->
                val jsonArgs = kotlinx.serialization.json.buildJsonObject {
                    args.forEach { (k, v) -> put(k, kotlinx.serialization.json.JsonPrimitive(v)) }
                }
                mcpServer.executeToolDirectly(tool, jsonArgs)
            }
        )
        // Wire the agent loop and goal scheduler to McpServer
        mcpServer.setBrainComponents(agentLoop, goalScheduler)
        return agentLoop
    }

    @Provides @Singleton
    fun provideWorkflowEngine(
        @ApplicationContext ctx: Context,
        agentLoop: AgentLoop,
        mcpServer: McpServer
    ): WorkflowEngine {
        val engine = WorkflowEngine(ctx, agentLoop)
        // Wire the WorkflowEngine to McpServer for workflow tool dispatching
        mcpServer.setWorkflowEngine(engine)
        return engine
    }
}
