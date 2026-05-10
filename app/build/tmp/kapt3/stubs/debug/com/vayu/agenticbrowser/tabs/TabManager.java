package com.vayu.agenticbrowser.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.vayu.agenticbrowser.common.Logger;
import com.vayu.agenticbrowser.engine.AgenticBridge;
import kotlinx.coroutines.flow.StateFlow;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 62\u00020\u0001:\u00016B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0005J\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00140\u00132\u0006\u0010\u001b\u001a\u00020\u0005J\u0006\u0010\u001d\u001a\u00020\u001eJ0\u0010\u001f\u001a\u00020\u001e2\u0006\u0010\u001b\u001a\u00020\u00052\u0006\u0010 \u001a\u00020\u00142\u0018\b\u0002\u0010!\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u0014\u0012\u0004\u0012\u00020\u001e\u0018\u00010\"J:\u0010#\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u00052\"\u0010$\u001a\u001e\b\u0001\u0012\u0004\u0012\u00020\u0018\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140&\u0012\u0006\u0012\u0004\u0018\u00010\u00010%H\u0086@\u00a2\u0006\u0002\u0010\'J\u0006\u0010(\u001a\u00020\u0005J\b\u0010)\u001a\u0004\u0018\u00010\bJ\b\u0010*\u001a\u0004\u0018\u00010\u0018J\u0010\u0010+\u001a\u0004\u0018\u00010\u00182\u0006\u0010\u001b\u001a\u00020\u0005J\u000e\u0010,\u001a\u00020\u001e2\u0006\u0010-\u001a\u00020\u000eJ\u0016\u0010.\u001a\u00020\u001e2\u0006\u0010\u001b\u001a\u00020\u00052\u0006\u0010/\u001a\u00020\u0014J\u001a\u00100\u001a\u00020\b2\u0006\u0010/\u001a\u00020\u00142\b\b\u0002\u00101\u001a\u00020\u001aH\u0007J\u000e\u00102\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\u0005J)\u00103\u001a\u00020\u001e2\u0006\u0010\u001b\u001a\u00020\u00052\u0017\u00104\u001a\u0013\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b0\"\u00a2\u0006\u0002\b5H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0011\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\fR\u001a\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/vayu/agenticbrowser/tabs/TabManager;", "", "()V", "_activeTabId", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_tabs", "", "Lcom/vayu/agenticbrowser/tabs/TabState;", "activeTabId", "Lkotlinx/coroutines/flow/StateFlow;", "getActiveTabId", "()Lkotlinx/coroutines/flow/StateFlow;", "appContext", "Landroid/content/Context;", "nextTabId", "Ljava/util/concurrent/atomic/AtomicInteger;", "pageLoadDeferreds", "Ljava/util/concurrent/ConcurrentHashMap;", "Lkotlinx/coroutines/CompletableDeferred;", "", "tabs", "getTabs", "webViewMap", "Landroid/webkit/WebView;", "closeTab", "", "tabId", "createPageLoadDeferred", "destroyAll", "", "evaluateJsInTab", "script", "callback", "Lkotlin/Function1;", "executeInTab", "block", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "(ILkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveTabIdValue", "getActiveTabState", "getActiveWebView", "getTab", "init", "context", "loadUrlInTab", "url", "newTab", "background", "switchTab", "updateTabState", "transform", "Lkotlin/ExtensionFunctionType;", "Companion", "app_debug"})
public final class TabManager {
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicInteger nextTabId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.Integer, android.webkit.WebView> webViewMap = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.Integer, kotlinx.coroutines.CompletableDeferred<java.lang.String>> pageLoadDeferreds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.tabs.TabState>> _tabs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.tabs.TabState>> tabs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _activeTabId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> activeTabId = null;
    @org.jetbrains.annotations.Nullable()
    private android.content.Context appContext;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.vayu.agenticbrowser.tabs.TabManager instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.tabs.TabManager.Companion Companion = null;
    
    private TabManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.tabs.TabState>> getTabs() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getActiveTabId() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @android.annotation.SuppressLint(value = {"SetJavaScriptEnabled"})
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tabs.TabState newTab(@org.jetbrains.annotations.NotNull()
    java.lang.String url, boolean background) {
        return null;
    }
    
    public final boolean closeTab(int tabId) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.tabs.TabState switchTab(int tabId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.webkit.WebView getTab(int tabId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.webkit.WebView getActiveWebView() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object executeInTab(int tabId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super android.webkit.WebView, ? super kotlin.coroutines.Continuation<? super java.lang.String>, ? extends java.lang.Object> block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CompletableDeferred<java.lang.String> createPageLoadDeferred(int tabId) {
        return null;
    }
    
    public final int getActiveTabIdValue() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.vayu.agenticbrowser.tabs.TabState getActiveTabState() {
        return null;
    }
    
    public final void evaluateJsInTab(int tabId, @org.jetbrains.annotations.NotNull()
    java.lang.String script, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> callback) {
    }
    
    public final void loadUrlInTab(int tabId, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    public final void destroyAll() {
    }
    
    private final void updateTabState(int tabId, kotlin.jvm.functions.Function1<? super com.vayu.agenticbrowser.tabs.TabState, com.vayu.agenticbrowser.tabs.TabState> transform) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/vayu/agenticbrowser/tabs/TabManager$Companion;", "", "()V", "instance", "Lcom/vayu/agenticbrowser/tabs/TabManager;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.vayu.agenticbrowser.tabs.TabManager getInstance() {
            return null;
        }
    }
}