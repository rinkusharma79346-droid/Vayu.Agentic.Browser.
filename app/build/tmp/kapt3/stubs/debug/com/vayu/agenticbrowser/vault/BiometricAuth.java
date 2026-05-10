package com.vayu.agenticbrowser.vault;

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u000b\u001a\u00020\fJ*\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\f0\u0011J\u0006\u0010\u0013\u001a\u00020\u0004J\b\u0010\u0014\u001a\u00020\fH\u0002R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/vayu/agenticbrowser/vault/BiometricAuth;", "", "()V", "<set-?>", "", "isUnlocked", "()Z", "lockJob", "Lkotlinx/coroutines/Job;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "lock", "", "promptUnlock", "activity", "Landroidx/fragment/app/FragmentActivity;", "onSuccess", "Lkotlin/Function0;", "onFailure", "requireUnlock", "startLockTimer", "Companion", "app_debug"})
public final class BiometricAuth {
    private boolean isUnlocked = false;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job lockJob;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.vayu.agenticbrowser.vault.BiometricAuth instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.vault.BiometricAuth.Companion Companion = null;
    
    private BiometricAuth() {
        super();
    }
    
    public final boolean isUnlocked() {
        return false;
    }
    
    public final void promptUnlock(@org.jetbrains.annotations.NotNull()
    androidx.fragment.app.FragmentActivity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onFailure) {
    }
    
    public final boolean requireUnlock() {
        return false;
    }
    
    public final void lock() {
    }
    
    private final void startLockTimer() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/vayu/agenticbrowser/vault/BiometricAuth$Companion;", "", "()V", "instance", "Lcom/vayu/agenticbrowser/vault/BiometricAuth;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.vayu.agenticbrowser.vault.BiometricAuth getInstance() {
            return null;
        }
    }
}