package com.vayu.agenticbrowser.engine;

import android.webkit.JavascriptInterface;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.flow.SharedFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005H\u0007R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\r"}, d2 = {"Lcom/vayu/agenticbrowser/engine/AgenticBridge;", "", "()V", "_messageFlow", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "messageFlow", "Lkotlinx/coroutines/flow/SharedFlow;", "getMessageFlow", "()Lkotlinx/coroutines/flow/SharedFlow;", "postMessage", "", "data", "app_debug"})
public final class AgenticBridge {
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableSharedFlow<java.lang.String> _messageFlow = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.SharedFlow<java.lang.String> messageFlow = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.engine.AgenticBridge INSTANCE = null;
    
    private AgenticBridge() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<java.lang.String> getMessageFlow() {
        return null;
    }
    
    @android.webkit.JavascriptInterface()
    public final void postMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
    }
}