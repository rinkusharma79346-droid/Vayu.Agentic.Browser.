package com.vayu.agenticbrowser.di

import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.agent.SessionRecorder
import com.vayu.agenticbrowser.common.NetworkMonitor
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.DialogController
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.FormDetector
import com.vayu.agenticbrowser.engine.WaitController
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.plugins.PluginRegistry
import com.vayu.agenticbrowser.tabs.TabManager
import com.vayu.agenticbrowser.tunnel.TunnelManager
import com.vayu.agenticbrowser.vault.BiometricAuth
import com.vayu.agenticbrowser.vault.CredentialVault
import com.vayu.agenticbrowser.vault.ProfileManager
import com.vayu.agenticbrowser.vault.SmsOtpReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWebViewManager(): WebViewManager {
        return WebViewManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideTabManager(): TabManager {
        return TabManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideDownloadManager(): VayuDownloadManager {
        return VayuDownloadManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideProfileManager(): ProfileManager {
        return ProfileManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideBiometricAuth(): BiometricAuth {
        return BiometricAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideSmsOtpReader(): SmsOtpReader {
        return SmsOtpReader.getInstance()
    }

    @Provides
    @Singleton
    fun providePluginRegistry(): PluginRegistry {
        return PluginRegistry.getInstance()
    }

    @Provides
    @Singleton
    fun provideTunnelManager(): TunnelManager {
        return TunnelManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideSessionRecorder(): SessionRecorder {
        return SessionRecorder.getInstance()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor {
        return NetworkMonitor.getInstance()
    }

    @Provides
    @Singleton
    fun provideDomController(
        webViewManager: WebViewManager,
        tabManager: TabManager
    ): DomController {
        return DomController(webViewManager, tabManager)
    }

    @Provides
    @Singleton
    fun provideWaitController(
        tabManager: TabManager,
        downloadManager: VayuDownloadManager
    ): WaitController {
        return WaitController(tabManager, downloadManager)
    }

    @Provides
    @Singleton
    fun provideFormDetector(
        domController: DomController
    ): FormDetector {
        return FormDetector(domController)
    }

    @Provides
    @Singleton
    fun provideDialogController(): DialogController {
        return DialogController()
    }

    @Provides
    @Singleton
    fun provideCredentialVault(
        profileManager: ProfileManager,
        biometricAuth: BiometricAuth,
        domController: DomController,
        tabManager: TabManager
    ): CredentialVault {
        return CredentialVault(profileManager, biometricAuth, domController, tabManager)
    }

    @Provides
    @Singleton
    fun provideMcpServer(
        domController: DomController,
        tabManager: TabManager,
        downloadManager: VayuDownloadManager,
        waitController: WaitController,
        credentialVault: CredentialVault,
        profileManager: ProfileManager,
        biometricAuth: BiometricAuth,
        formDetector: FormDetector,
        dialogController: DialogController,
        smsOtpReader: SmsOtpReader,
        pluginRegistry: PluginRegistry,
        tunnelManager: TunnelManager,
        sessionRecorder: SessionRecorder,
        networkMonitor: NetworkMonitor
    ): McpServer {
        return McpServer(
            domController, tabManager, downloadManager, waitController,
            credentialVault, profileManager, biometricAuth, formDetector, dialogController,
            smsOtpReader, pluginRegistry, tunnelManager, sessionRecorder, networkMonitor
        )
    }
}
