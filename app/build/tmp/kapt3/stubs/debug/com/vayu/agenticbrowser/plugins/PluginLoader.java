package com.vayu.agenticbrowser.plugins;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fJ\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\fJ\u000e\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\nJ\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\nJ\u000e\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/vayu/agenticbrowser/plugins/PluginLoader;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "json", "Lkotlinx/serialization/json/Json;", "deletePlugin", "", "name", "", "loadAll", "", "Lcom/vayu/agenticbrowser/plugins/Plugin;", "loadBuiltInPlugins", "loadFromJson", "jsonStr", "matchesCurrentSite", "", "plugin", "url", "savePlugin", "app_debug"})
public final class PluginLoader {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    
    public PluginLoader(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.Plugin> loadAll() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.plugins.Plugin loadFromJson(@org.jetbrains.annotations.NotNull()
    java.lang.String jsonStr) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.plugins.Plugin> loadBuiltInPlugins() {
        return null;
    }
    
    public final boolean matchesCurrentSite(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.Plugin plugin, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return false;
    }
    
    public final void savePlugin(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.Plugin plugin) {
    }
    
    public final void deletePlugin(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
}