package com.vayu.agenticbrowser.plugins;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 #2\u00020\u0001:\u0001#B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015J\u000e\u0010\u0016\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015J\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u00052\u0006\u0010\u0019\u001a\u00020\u0015J\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u00052\u0006\u0010\u0019\u001a\u00020\u0015J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0014\u001a\u00020\u0015J\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0019\u001a\u00020\u0015J\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00052\u0006\u0010\u0019\u001a\u00020\u0015J\u000e\u0010 \u001a\u00020\u00132\u0006\u0010!\u001a\u00020\u000fJ\u0006\u0010\"\u001a\u00020\u0013R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u001d\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000bR\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "", "()V", "_activePlugins", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/vayu/agenticbrowser/plugins/Plugin;", "_allPlugins", "activePlugins", "Lkotlinx/coroutines/flow/StateFlow;", "getActivePlugins", "()Lkotlinx/coroutines/flow/StateFlow;", "allPlugins", "getAllPlugins", "context", "Landroid/content/Context;", "pluginLoader", "Lcom/vayu/agenticbrowser/plugins/PluginLoader;", "disablePlugin", "", "name", "", "enablePlugin", "getAdditionalTools", "Lcom/vayu/agenticbrowser/plugins/ToolDef;", "url", "getFormTemplatesForSite", "Lcom/vayu/agenticbrowser/plugins/FormTemplate;", "getPluginByName", "getPluginsForSite", "getRulesForSite", "Lcom/vayu/agenticbrowser/plugins/PluginRule;", "init", "ctx", "loadAllPlugins", "Companion", "app_debug"})
public final class PluginRegistry {
    @org.jetbrains.annotations.Nullable()
    private android.content.Context context;
    @org.jetbrains.annotations.Nullable()
    private com.vayu.agenticbrowser.plugins.PluginLoader pluginLoader;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> _activePlugins = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> activePlugins = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> _allPlugins = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> allPlugins = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.vayu.agenticbrowser.plugins.PluginRegistry instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.plugins.PluginRegistry.Companion Companion = null;
    
    private PluginRegistry() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> getActivePlugins() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.plugins.Plugin>> getAllPlugins() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void loadAllPlugins() {
    }
    
    public final void enablePlugin(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void disablePlugin(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.Plugin> getPluginsForSite(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.ToolDef> getAdditionalTools(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.vayu.agenticbrowser.plugins.Plugin getPluginByName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.PluginRule> getRulesForSite(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.FormTemplate> getFormTemplatesForSite(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/vayu/agenticbrowser/plugins/PluginRegistry$Companion;", "", "()V", "instance", "Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.vayu.agenticbrowser.plugins.PluginRegistry getInstance() {
            return null;
        }
    }
}