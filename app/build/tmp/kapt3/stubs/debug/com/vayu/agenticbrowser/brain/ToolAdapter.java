package com.vayu.agenticbrowser.brain;

import com.vayu.agenticbrowser.agent.ToolRegistry;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import kotlinx.serialization.json.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bH\u0002J\u001c\u0010\u000b\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\bH\u0002J\u0018\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/vayu/agenticbrowser/brain/ToolAdapter;", "", "()V", "json", "Lkotlinx/serialization/json/Json;", "buildParametersJson", "Lkotlinx/serialization/json/JsonObject;", "params", "", "", "Lcom/vayu/agenticbrowser/agent/ToolParam;", "buildPluginParametersJson", "Lcom/vayu/agenticbrowser/plugins/ToolParam;", "convertToOpenAiFunctions", "", "Lcom/vayu/agenticbrowser/brain/OpenAiFunction;", "pluginRegistry", "Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "app_debug"})
public final class ToolAdapter {
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.brain.ToolAdapter INSTANCE = null;
    
    private ToolAdapter() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.brain.OpenAiFunction> convertToOpenAiFunctions(@org.jetbrains.annotations.Nullable()
    com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry) {
        return null;
    }
    
    private final kotlinx.serialization.json.JsonObject buildParametersJson(java.util.Map<java.lang.String, com.vayu.agenticbrowser.agent.ToolParam> params) {
        return null;
    }
    
    private final kotlinx.serialization.json.JsonObject buildPluginParametersJson(java.util.Map<java.lang.String, com.vayu.agenticbrowser.plugins.ToolParam> params) {
        return null;
    }
}