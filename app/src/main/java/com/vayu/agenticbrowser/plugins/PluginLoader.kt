package com.vayu.agenticbrowser.plugins

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PluginLoader(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun loadAll(): List<Plugin> {
        val pluginsDir = File(
            context.getExternalFilesDir(null),
            "Plugins"
        )

        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
            Logger.i("Created plugins directory: ${pluginsDir.absolutePath}")
            return emptyList()
        }

        val pluginFiles = pluginsDir.listFiles { file ->
            file.extension == "json"
        } ?: return emptyList()

        val plugins = mutableListOf<Plugin>()
        for (file in pluginFiles) {
            try {
                val content = file.readText()
                val plugin = loadFromJson(content)
                plugins.add(plugin)
                Logger.i("Loaded plugin: ${plugin.name} v${plugin.version}")
            } catch (e: Exception) {
                Logger.e("Failed to load plugin from ${file.name}", e)
            }
        }

        return plugins
    }

    fun loadFromJson(jsonStr: String): Plugin {
        return json.decodeFromString<Plugin>(jsonStr)
    }

    fun loadBuiltInPlugins(): List<Plugin> {
        val plugins = mutableListOf<Plugin>()
        val resources = context.resources

        val rawIds = intArrayOf(
            com.vayu.agenticbrowser.R.raw.plugin_google_ai_studio,
            com.vayu.agenticbrowser.R.raw.plugin_youtube_upload,
            com.vayu.agenticbrowser.R.raw.plugin_flow_studios
        )

        for (id in rawIds) {
            try {
                val content = resources.openRawResource(id).bufferedReader().use { it.readText() }
                val plugin = loadFromJson(content)
                plugins.add(plugin)
                Logger.i("Loaded built-in plugin: ${plugin.name}")
            } catch (e: Exception) {
                Logger.e("Failed to load built-in plugin resource", e)
            }
        }

        return plugins
    }

    fun matchesCurrentSite(plugin: Plugin, url: String): Boolean {
        if (plugin.sites.isEmpty()) return true
        return plugin.sites.any { site ->
            url.contains(site, ignoreCase = true)
        }
    }

    fun savePlugin(plugin: Plugin) {
        val pluginsDir = File(
            context.getExternalFilesDir(null),
            "Plugins"
        )
        if (!pluginsDir.exists()) pluginsDir.mkdirs()

        val file = File(pluginsDir, "${plugin.name.replace(" ", "_")}.json")
        file.writeText(json.encodeToString(plugin))
        Logger.i("Saved plugin: ${plugin.name} to ${file.absolutePath}")
    }

    fun deletePlugin(name: String) {
        val pluginsDir = File(
            context.getExternalFilesDir(null),
            "Plugins"
        )
        val file = File(pluginsDir, "${name.replace(" ", "_")}.json")
        if (file.exists()) {
            file.delete()
            Logger.i("Deleted plugin file: ${file.name}")
        }
    }
}
