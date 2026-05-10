package com.vayu.agenticbrowser.vault;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\b\u0007\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004J\"\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u001a\u0010\u000e\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\u000f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0010R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/vayu/agenticbrowser/vault/SmsOtpReader;", "", "()V", "appContext", "Landroid/content/Context;", "init", "", "context", "querySmsInbox", "", "regex", "Lkotlin/text/Regex;", "sinceTime", "", "readLatestOtp", "timeoutMs", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class SmsOtpReader {
    @org.jetbrains.annotations.Nullable()
    private android.content.Context appContext;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.vayu.agenticbrowser.vault.SmsOtpReader instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.vault.SmsOtpReader.Companion Companion = null;
    
    private SmsOtpReader() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readLatestOtp(long timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String querySmsInbox(android.content.Context context, kotlin.text.Regex regex, long sinceTime) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/vayu/agenticbrowser/vault/SmsOtpReader$Companion;", "", "()V", "instance", "Lcom/vayu/agenticbrowser/vault/SmsOtpReader;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.vayu.agenticbrowser.vault.SmsOtpReader getInstance() {
            return null;
        }
    }
}