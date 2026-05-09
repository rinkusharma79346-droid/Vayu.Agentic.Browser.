package com.vayu.agenticbrowser.engine

import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger

object StealthController {

    private var humanTypingEnabled = false
    private var stealthModeEnabled = false

    private const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36"

    private val STEALTH_JS = """
        (function() {
            // Remove navigator.webdriver property
            try {
                Object.defineProperty(navigator, 'webdriver', {
                    get: function() { return undefined; },
                    configurable: true
                });
                delete navigator.webdriver;
            } catch(e) {}

            // Spoof navigator.plugins with a realistic list
            try {
                var fakePlugins = [
                    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: '' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' }
                ];
                var pluginArray = [];
                for (var i = 0; i < fakePlugins.length; i++) {
                    var p = fakePlugins[i];
                    var plugin = Object.create(Plugin.prototype);
                    Object.defineProperty(plugin, 'name', { get: function() { return p.name; } });
                    Object.defineProperty(plugin, 'filename', { get: function() { return p.filename; } });
                    Object.defineProperty(plugin, 'description', { get: function() { return p.description; } });
                    Object.defineProperty(plugin, 'length', { get: function() { return 0; } });
                    pluginArray.push(plugin);
                }
                Object.defineProperty(navigator, 'plugins', {
                    get: function() {
                        var arr = pluginArray.slice();
                        arr.item = function(i) { return arr[i] || null; };
                        arr.namedItem = function(name) {
                            for (var i = 0; i < arr.length; i++) {
                                if (arr[i].name === name) return arr[i];
                            }
                            return null;
                        };
                        arr.refresh = function() {};
                        return arr;
                    },
                    configurable: true
                });
            } catch(e) {}

            // Randomize canvas fingerprint slightly
            try {
                var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                HTMLCanvasElement.prototype.toDataURL = function(type) {
                    var dataUrl = origToDataURL.apply(this, arguments);
                    if (dataUrl && dataUrl.length > 100) {
                        var noise = String.fromCharCode(Math.floor(Math.random() * 10) + 48);
                        dataUrl = dataUrl.slice(0, 70) + noise + dataUrl.slice(71);
                    }
                    return dataUrl;
                };

                var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
                CanvasRenderingContext2D.prototype.getImageData = function(sx, sy, sw, sh) {
                    var imageData = origGetImageData.apply(this, arguments);
                    if (imageData && imageData.data && imageData.data.length > 4) {
                        imageData.data[0] = imageData.data[0] ^ (Math.random() * 2 | 0);
                    }
                    return imageData;
                };
            } catch(e) {}

            // Override window.chrome to appear as real Chrome
            try {
                if (!window.chrome) {
                    window.chrome = {};
                }
                if (!window.chrome.runtime) {
                    window.chrome.runtime = {
                        connect: function() {},
                        sendMessage: function() {},
                        onMessage: {
                            addListener: function() {},
                            removeListener: function() {}
                        }
                    };
                }
                if (!window.chrome.csi) {
                    window.chrome.csi = function() {};
                }
                if (!window.chrome.loadTimes) {
                    window.chrome.loadTimes = function() {
                        return {
                            commitLoadTime: Date.now() / 1000,
                            connectionInfo: 'h2',
                            finishDocumentLoadTime: Date.now() / 1000,
                            finishLoadTime: Date.now() / 1000,
                            firstPaintAfterLoadTime: 0,
                            firstPaintTime: Date.now() / 1000,
                            navigationType: 'Other',
                            npnNegotiatedProtocol: 'h2',
                            requestTime: Date.now() / 1000 - 0.5,
                            startLoadTime: Date.now() / 1000 - 0.5,
                            wasAlternateProtocolAvailable: false,
                            wasFetchedViaSpdy: true,
                            wasNpnNegotiated: true
                        };
                    };
                }
            } catch(e) {}
        })();
    """.trimIndent()

    fun applyStealthMode(webView: WebView) {
        stealthModeEnabled = true
        webView.evaluateJavascript(STEALTH_JS) { result ->
            Logger.i("Stealth mode JS injected: ${result ?: "ok"}")
        }
    }

    fun removeStealthMode(webView: WebView) {
        stealthModeEnabled = false
        webView.evaluateJavascript("""
            (function() {
                try {
                    delete navigator.__proto__.webdriver;
                } catch(e) {}
                try {
                    delete window.chrome;
                } catch(e) {}
            })();
        """.trimIndent()) { _ ->
            Logger.i("Stealth mode removed")
        }
    }

    fun setUserAgent(webView: WebView, userAgent: String) {
        webView.settings.userAgentString = userAgent
        Logger.i("User agent set to: $userAgent")
    }

    fun setDefaultUserAgent(webView: WebView) {
        setUserAgent(webView, DEFAULT_USER_AGENT)
    }

    fun enableHumanTypingSimulation(enabled: Boolean) {
        humanTypingEnabled = enabled
        Logger.i("Human typing simulation: $enabled")
    }

    fun isHumanTypingEnabled(): Boolean = humanTypingEnabled

    fun isStealthModeEnabled(): Boolean = stealthModeEnabled

    fun getDefaultUserAgent(): String = DEFAULT_USER_AGENT

    val USER_AGENT_PRESETS = mapOf(
        "Chrome Android" to "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36",
        "Chrome Desktop" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Safari/537.36",
        "Safari iOS" to "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
    )

    fun getStealthJs(): String = STEALTH_JS
}
