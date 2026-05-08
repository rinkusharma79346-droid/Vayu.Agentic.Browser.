package com.vayu.agenticbrowser.di

import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.downloads.VayuDownloadManager
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.WaitController
import com.vayu.agenticbrowser.engine.WebViewManager
import com.vayu.agenticbrowser.tabs.TabManager
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
    fun provideMcpServer(
        domController: DomController,
        tabManager: TabManager,
        downloadManager: VayuDownloadManager,
        waitController: WaitController
    ): McpServer {
        return McpServer(domController, tabManager, downloadManager, waitController)
    }
}
