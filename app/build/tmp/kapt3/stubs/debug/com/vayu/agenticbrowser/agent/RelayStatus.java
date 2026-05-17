package com.vayu.agenticbrowser.agent;

import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.Serializable;
import kotlinx.serialization.json.*;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Relay connection status.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/vayu/agenticbrowser/agent/RelayStatus;", "", "(Ljava/lang/String;I)V", "DISCONNECTED", "CONNECTING", "AUTHENTICATING", "CONNECTED", "FAILED", "app_debug"})
public enum RelayStatus {
    /*public static final*/ DISCONNECTED /* = new DISCONNECTED() */,
    /*public static final*/ CONNECTING /* = new CONNECTING() */,
    /*public static final*/ AUTHENTICATING /* = new AUTHENTICATING() */,
    /*public static final*/ CONNECTED /* = new CONNECTED() */,
    /*public static final*/ FAILED /* = new FAILED() */;
    
    RelayStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.vayu.agenticbrowser.agent.RelayStatus> getEntries() {
        return null;
    }
}