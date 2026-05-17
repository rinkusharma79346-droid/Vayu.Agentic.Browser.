package com.vayu.agenticbrowser.engine;

import android.webkit.WebView;
import com.vayu.agenticbrowser.common.Logger;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.tabs.TabManager;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u0010\u001a\u00020\f2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J$\u0010\u0014\u001a\u00020\f2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0015J,\u0010\u0016\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0017J,\u0010\u0018\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\f2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0017J,\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\f2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/vayu/agenticbrowser/engine/WaitController;", "", "tabManager", "Lcom/vayu/agenticbrowser/tabs/TabManager;", "downloadManager", "Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;", "(Lcom/vayu/agenticbrowser/tabs/TabManager;Lcom/vayu/agenticbrowser/downloads/VayuDownloadManager;)V", "checkSelectorExists", "", "tabId", "", "selector", "", "(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkTextExists", "text", "waitForDownload", "timeoutMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "waitForNavigation", "(Ljava/lang/Integer;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "waitForSelector", "(Ljava/lang/String;Ljava/lang/Integer;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "waitForText", "waitForUrlContains", "substring", "app_debug"})
public final class WaitController {
    @org.jetbrains.annotations.NotNull()
    private final com.vayu.agenticbrowser.tabs.TabManager tabManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager = null;
    
    public WaitController(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.tabs.TabManager tabManager, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.downloads.VayuDownloadManager downloadManager) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object waitForSelector(@org.jetbrains.annotations.NotNull()
    java.lang.String selector, @org.jetbrains.annotations.Nullable()
    java.lang.Integer tabId, long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object waitForText(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.Nullable()
    java.lang.Integer tabId, long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object waitForNavigation(@org.jetbrains.annotations.Nullable()
    java.lang.Integer tabId, long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object waitForUrlContains(@org.jetbrains.annotations.NotNull()
    java.lang.String substring, @org.jetbrains.annotations.Nullable()
    java.lang.Integer tabId, long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object waitForDownload(long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object checkSelectorExists(int tabId, java.lang.String selector, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object checkTextExists(int tabId, java.lang.String text, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}