package com.vayu.agenticbrowser.plugins

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PluginRegistry private constructor() {

    private var context: Context? = null
    private var pluginLoader: PluginLoader? = null

    private val _activePlugins = MutableStateFlow<List<Plugin>>(emptyList())
    val activePlugins: StateFlow<List<Plugin>> = _activePlugins.asStateFlow()

    private val _allPlugins = MutableStateFlow<List<Plugin>>(emptyList())
    val allPlugins: StateFlow<List<Plugin>> = _allPlugins.asStateFlow()

    fun init(ctx: Context) {
        context = ctx.applicationContext
        pluginLoader = PluginLoader(ctx.applicationContext)
        loadAllPlugins()
    }

    fun loadAllPlugins() {
        val loader = pluginLoader ?: return
        val ctx = context ?: return

        val builtIn = loader.loadBuiltInPlugins()
        val userPlugins = loader.loadAll()

        val merged = mutableListOf<Plugin>()

        val existingNames = mutableSetOf<String>()
        for (plugin in builtIn) {
            merged.add(plugin)
            existingNames.add(plugin.name)
        }
        for (plugin in userPlugins) {
            if (plugin.name !in existingNames) {
                merged.add(plugin)
                existingNames.add(plugin.name)
            }
        }

        _allPlugins.value = merged
        _activePlugins.value = merged.filter { it.enabled }

        Logger.i("PluginRegistry: ${merged.size} total, ${_activePlugins.value.size} active")
    }

    fun enablePlugin(name: String) {
        val plugins = _allPlugins.value.toMutableList()
        val index = plugins.indexOfFirst { it.name == name }
        if (index != -1) {
            plugins[index] = plugins[index].copy(enabled = true)
            _allPlugins.value = plugins
            _activePlugins.value = plugins.filter { it.enabled }
            Logger.i("Enabled plugin: $name")

            val loader = pluginLoader ?: return
            loader.savePlugin(plugins[index])
        }
    }

    fun disablePlugin(name: String) {
        val plugins = _allPlugins.value.toMutableList()
        val index = plugins.indexOfFirst { it.name == name }
        if (index != -1) {
            plugins[index] = plugins[index].copy(enabled = false)
            _allPlugins.value = plugins
            _activePlugins.value = plugins.filter { it.enabled }
            Logger.i("Disabled plugin: $name")

            val loader = pluginLoader ?: return
            loader.savePlugin(plugins[index])
        }
    }

    fun getPluginsForSite(url: String): List<Plugin> {
        val loader = pluginLoader ?: return emptyList()
        return _activePlugins.value.filter { plugin ->
            loader.matchesCurrentSite(plugin, url)
        }
    }

    fun getAdditionalTools(url: String): List<ToolDef> {
        val plugins = getPluginsForSite(url)
        return plugins.flatMap { it.tools }
    }

    fun getPluginByName(name: String): Plugin? {
        return _allPlugins.value.find { it.name == name }
    }

    fun getRulesForSite(url: String): List<PluginRule> {
        return getPluginsForSite(url).flatMap { it.rules }
    }

    fun getFormTemplatesForSite(url: String): List<FormTemplate> {
        return getPluginsForSite(url).flatMap { it.formTemplates }
    }

    companion object {
        @Volatile
        private var instance: PluginRegistry? = null

        fun getInstance(): PluginRegistry {
            return instance ?: synchronized(this) {
                instance ?: PluginRegistry().also { instance = it }
            }
        }
    }
}
