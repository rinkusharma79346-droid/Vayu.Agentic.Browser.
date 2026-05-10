package com.vayu.agenticbrowser.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicReference

class WebViewManager private constructor() {

    private var webView: WebView? = null
    private val pageLoadDeferred = AtomicReference<CompletableDeferred<String>?>(null)

    @SuppressLint("SetJavaScriptEnabled")
    fun init(context: Context): WebView {
        if (webView != null) {
            return webView!!
        }

        val wv = WebView(context)
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
                Logger.d("Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Logger.d("Page finished loading: $url")
                // Inject stealth JS if stealth mode is enabled
                if (StealthController.isStealthModeEnabled() && view != null) {
                    view.evaluateJavascript(StealthController.getStealthJs()) { _ ->
                        Logger.d("Stealth mode JS re-injected on page load")
                    }
                }
                pageLoadDeferred.getAndSet(null)?.complete(url ?: "")
            }
        }

        wv.addJavascriptInterface(AgenticBridge, "AgenticBridge")

        webView = wv
        return wv
    }

    fun getWebView(): WebView? = webView

    fun evaluateJs(script: String, callback: ((String?) -> Unit)? = null) {
        webView?.evaluateJavascript(script) { result ->
            callback?.invoke(result)
        }
    }

    fun loadUrl(url: String) {
        Logger.i("Loading URL: $url")
        webView?.loadUrl(url)
    }

    fun getCurrentUrl(): String {
        return webView?.url ?: ""
    }

    fun getTitle(): String {
        return webView?.title ?: ""
    }

    fun createPageLoadDeferred(): CompletableDeferred<String> {
        val deferred = CompletableDeferred<String>()
        pageLoadDeferred.set(deferred)
        return deferred
    }

    fun destroy() {
        webView?.destroy()
        webView = null
    }

    companion object {
        @Volatile
        private var instance: WebViewManager? = null

        fun getInstance(): WebViewManager {
            return instance ?: synchronized(this) {
                instance ?: WebViewManager().also { instance = it }
            }
        }
    }
}
