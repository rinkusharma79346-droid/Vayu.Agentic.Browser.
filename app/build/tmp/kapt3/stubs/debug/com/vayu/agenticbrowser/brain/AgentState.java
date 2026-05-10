package com.vayu.agenticbrowser.brain;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.Serializable;
import com.vayu.agenticbrowser.plugins.PluginRegistry;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/vayu/agenticbrowser/brain/AgentState;", "", "(Ljava/lang/String;I)V", "IDLE", "THINKING", "EXECUTING", "WAITING", "COMPLETED", "FAILED", "app_debug"})
public enum AgentState {
    /*public static final*/ IDLE /* = new IDLE() */,
    /*public static final*/ THINKING /* = new THINKING() */,
    /*public static final*/ EXECUTING /* = new EXECUTING() */,
    /*public static final*/ WAITING /* = new WAITING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ FAILED /* = new FAILED() */;
    
    AgentState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.vayu.agenticbrowser.brain.AgentState> getEntries() {
        return null;
    }
}