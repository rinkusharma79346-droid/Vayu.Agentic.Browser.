package com.vayu.agenticbrowser.downloads;

import android.net.Uri;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.flow.SharedFlow;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB|\u0012u\u0010\u0002\u001aq\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0007\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\b\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\n\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\r0\u0003\u00a2\u0006\u0002\u0010\u000eJ8\u0010\u0002\u001a\u00020\r2\b\u0010\u0007\u001a\u0004\u0018\u00010\u00042\b\u0010\b\u001a\u0004\u0018\u00010\u00042\b\u0010\t\u001a\u0004\u0018\u00010\u00042\b\u0010\n\u001a\u0004\u0018\u00010\u00042\u0006\u0010\f\u001a\u00020\u000bH\u0016R}\u0010\u0002\u001aq\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\u0007\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\b\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\n\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b\u0005\u0012\b\b\u0006\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\r0\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/vayu/agenticbrowser/downloads/VayuDownloadListener;", "Landroid/webkit/DownloadListener;", "onDownloadStart", "Lkotlin/Function5;", "", "Lkotlin/ParameterName;", "name", "url", "userAgent", "contentDisposition", "mimetype", "", "contentLength", "", "(Lkotlin/jvm/functions/Function5;)V", "Companion", "app_debug"})
public final class VayuDownloadListener implements android.webkit.DownloadListener {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function5<java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long, kotlin.Unit> onDownloadStart = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.downloads.VayuDownloadListener.Companion Companion = null;
    
    public VayuDownloadListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function5<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.Long, kotlin.Unit> onDownloadStart) {
        super();
    }
    
    @java.lang.Override()
    public void onDownloadStart(@org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.Nullable()
    java.lang.String userAgent, @org.jetbrains.annotations.Nullable()
    java.lang.String contentDisposition, @org.jetbrains.annotations.Nullable()
    java.lang.String mimetype, long contentLength) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0004\u00a8\u0006\u0007"}, d2 = {"Lcom/vayu/agenticbrowser/downloads/VayuDownloadListener$Companion;", "", "()V", "parseFilename", "", "contentDisposition", "url", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String parseFilename(@org.jetbrains.annotations.NotNull()
        java.lang.String contentDisposition, @org.jetbrains.annotations.NotNull()
        java.lang.String url) {
            return null;
        }
    }
}