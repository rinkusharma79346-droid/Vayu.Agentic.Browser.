package com.vayu.agenticbrowser.tabs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.engine.AgenticBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class TabManager private constructor() {

    private val nextTabId = AtomicInteger(1)
    private val webViewMap = ConcurrentHashMap<Int, WebView>()
    private val pageLoadDeferreds = ConcurrentHashMap<Int, CompletableDeferred<String>>()

    private val _tabs = MutableStateFlow<List<TabState>>(emptyList())
    val tabs: StateFlow<List<TabState>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(-1)
    val activeTabId: StateFlow<Int> = _activeTabId.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun newTab(url: String, background: Boolean = false): TabState {
        val ctx = appContext ?: throw IllegalStateException("TabManager not initialized. Call init() first.")
        val tabId = nextTabId.getAndIncrement()
        val now = System.currentTimeMillis()

        val wv = WebView(ctx)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.settings.databaseEnabled = true
        wv.settings.allowFileAccess = true
        wv.settings.loadsImagesAutomatically = true
        wv.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Logger.d("Tab $tabId: Page started loading: $url")
                updateTabState(tabId) { copy(loading = true, url = url ?: this.url) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Logger.d("Tab $tabId: Page finished loading: $url")
                val title = view?.title ?: ""
                updateTabState(tabId) { copy(loading = false, title = title, url = url ?: this.url) }
                pageLoadDeferreds.remove(tabId)?.complete(url ?: "")
            }
        }

        wv.addJavascriptInterface(AgenticBridge, "AgenticBridge")
        wv.loadUrl(url)

        webViewMap[tabId] = wv

        val tabState = TabState(
            tabId = tabId,
            title = "",
            url = url,
            loading = true,
            active = false,
            createdAt = now,
            lastAccessedAt = now
        )

        _tabs.update { current ->
            val newTabs = current.map { it.copy(active = false) } + tabState.copy(active = !background)
        }

        if (!background) {
            _activeTabId.value = tabId
        }

        Logger.i("Created tab $tabId: $url (background=$background)")
        return tabState
    }

    fun closeTab(tabId: Int): Boolean {
        val wv = webViewMap.remove(tabId) ?: return false
        wv.destroy()
        pageLoadDeferreds.remove(tabId)

        _tabs.update { current ->
            val filtered = current.filter { it.tabId != tabId }
            if (_activeTabId.value == tabId && filtered.isNotEmpty()) {
                val newActive = filtered.last()
                _activeTabId.value = newActive.tabId
                filtered.map { if (it.tabId == newActive.tabId) it.copy(active = true) else it }
            } else {
                filtered
            }
        }

        if (_tabs.value.isEmpty()) {
            _activeTabId.value = -1
        }

        Logger.i("Closed tab $tabId")
        return true
    }

    fun switchTab(tabId: Int): TabState {
        val tab = _tabs.value.find { it.tabId == tabId }
            ?: throw IllegalArgumentException("Tab $tabId not found")

        val now = System.currentTimeMillis()
        _activeTabId.value = tabId

        _tabs.update { current ->
            current.map { t ->
                when {
                    t.tabId == tabId -> t.copy(active = true, lastAccessedAt = now)
                    else -> t.copy(active = false)
                }
            }
        }

        Logger.i("Switched to tab $tabId")
        return _tabs.value.first { it.tabId == tabId }
    }

    fun getTab(tabId: Int): WebView? {
        return webViewMap[tabId]
    }

    fun getActiveWebView(): WebView? {
        return webViewMap[_activeTabId.value]
    }

    suspend fun executeInTab(tabId: Int, block: suspend (WebView) -> String): String {
        val wv = webViewMap[tabId] ?: return """{"error":"Tab $tabId not found"}"""
        return block(wv)
    }

    fun createPageLoadDeferred(tabId: Int): CompletableDeferred<String> {
        val deferred = CompletableDeferred<String>()
        pageLoadDeferreds[tabId] = deferred
        return deferred
    }

    fun getActiveTabIdValue(): Int = _activeTabId.value

    fun getActiveTabState(): TabState? {
        return _tabs.value.find { it.tabId == _activeTabId.value }
    }

    fun evaluateJsInTab(tabId: Int, script: String, callback: ((String?) -> Unit)? = null) {
        val wv = webViewMap[tabId]
        if (wv == null) {
            callback?.invoke(null)
            return
        }
        wv.evaluateJavascript(script) { result ->
            callback?.invoke(result)
        }
    }

    fun loadUrlInTab(tabId: Int, url: String) {
        val wv = webViewMap[tabId] ?: return
        Logger.i("Tab $tabId: Loading URL: $url")
        wv.loadUrl(url)
    }

    fun destroyAll() {
        webViewMap.forEach { (_, wv) -> wv.destroy() }
        webViewMap.clear()
        pageLoadDeferreds.clear()
        _tabs.value = emptyList()
        _activeTabId.value = -1
    }

    private fun updateTabState(tabId: Int, transform: TabState.() -> TabState) {
        _tabs.update { current ->
            current.map { if (it.tabId == tabId) it.transform() else it }
        }
    }

    companion object {
        @Volatile
        private var instance: TabManager? = null

        fun getInstance(): TabManager {
            return instance ?: synchronized(this) {
                instance ?: TabManager().also { instance = it }
            }
        }
    }
}
