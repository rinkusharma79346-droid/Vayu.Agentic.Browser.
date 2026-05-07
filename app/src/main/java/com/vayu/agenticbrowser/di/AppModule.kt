package com.vayu.agenticbrowser.di

import com.vayu.agenticbrowser.agent.McpServer
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.engine.WebViewManager
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
    fun provideDomController(webViewManager: WebViewManager): DomController {
        return DomController(webViewManager)
    }

    @Provides
    @Singleton
    fun provideMcpServer(domController: DomController): McpServer {
        return McpServer(domController)
    }
}
