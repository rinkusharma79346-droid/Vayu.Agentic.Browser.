package com.vayu.agenticbrowser.engine

import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DomController(
    private val webViewManager: WebViewManager
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun navigate(url: String): String {
        return try {
            val deferred = webViewManager.createPageLoadDeferred()
            webViewManager.loadUrl(url)

            val loadedUrl = withTimeoutOrNull(10_000L) {
                deferred.await()
            }

            if (loadedUrl != null) {
                val title = webViewManager.getTitle()
                val currentUrl = webViewManager.getCurrentUrl()
                json.encodeToString(
                    NavigateResult(success = true, title = title, url = currentUrl)
                )
            } else {
                val title = webViewManager.getTitle()
                val currentUrl = webViewManager.getCurrentUrl()
                json.encodeToString(
                    NavigateResult(success = true, title = title, url = currentUrl)
                )
            }
        } catch (e: Exception) {
            Logger.e("navigate error", e)
            json.encodeToString(
                NavigateResult(success = false, title = "", url = "", error = e.message)
            )
        }
    }

    suspend fun querySelector(selector: String, all: Boolean): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var results = [];
                        var elements = $all ? document.querySelectorAll('${selector.replace("'", "\\'")}')
                                           : [document.querySelector('${selector.replace("'", "\\'")}')];
                        if (!elements) return JSON.stringify({error: 'No elements found'});
                        for (var i = 0; i < elements.length; i++) {
                            var el = elements[i];
                            if (!el) continue;
                            results.push({
                                tag: el.tagName ? el.tagName.toLowerCase() : '',
                                text: el.textContent ? el.textContent.substring(0, 500) : '',
                                id: el.id || '',
                                className: el.className || '',
                                visible: el.offsetParent !== null || el.style.display !== 'none'
                            });
                        }
                        return JSON.stringify(results);
                    } catch(e) {
                        return JSON.stringify({error: e.message});
                    }
                })()
            """.trimIndent()

            val rawResult = evaluateJsAndWait(jsCode)

            if (rawResult.startsWith("\"") && rawResult.endsWith("\"")) {
                val unquoted = json.decodeFromString<kotlinx.serialization.json.JsonElement>(
                    "\"$rawResult\""
                )
                val innerJson = rawResult.substring(1, rawResult.length - 1)
                    .replace("\\\\", "\\")
                    .replace("\\\"", "\"")
                return innerJson
            }
            rawResult
        } catch (e: Exception) {
            Logger.e("querySelector error", e)
            json.encodeToString(
                QueryResult(error = e.message ?: "Unknown error")
            )
        }
    }

    suspend fun click(selector: String, index: Int): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var elements = document.querySelectorAll('${selector.replace("'", "\\'")}');
                        if (!elements || elements.length <= ${index}) {
                            return JSON.stringify({success: false, error: 'ELEMENT_NOT_FOUND'});
                        }
                        var el = elements[${index}];
                        el.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
                        el.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true}));
                        el.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true}));
                        return JSON.stringify({success: true});
                    } catch(e) {
                        return JSON.stringify({success: false, error: e.message});
                    }
                })()
            """.trimIndent()

            evaluateJsAndWait(jsCode)
        } catch (e: Exception) {
            Logger.e("click error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun type(selector: String, text: String, clearFirst: Boolean): String {
        return try {
            val escapedSelector = selector.replace("'", "\\'")
            val escapedText = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r")
            val clearCode = if (clearFirst) "el.value = '';" else ""

            val jsCode = """
                (function() {
                    try {
                        var el = document.querySelector('$escapedSelector');
                        if (!el) return JSON.stringify({success: false, error: 'ELEMENT_NOT_FOUND'});
                        el.focus();
                        $clearCode
                        var text = '$escapedText';
                        for (var i = 0; i < text.length; i++) {
                            var ch = text[i];
                            el.value += ch;
                            el.dispatchEvent(new KeyboardEvent('keydown', {bubbles: true, key: ch}));
                            el.dispatchEvent(new Event('input', {bubbles: true}));
                            el.dispatchEvent(new KeyboardEvent('keyup', {bubbles: true, key: ch}));
                        }
                        el.dispatchEvent(new Event('change', {bubbles: true}));
                        el.dispatchEvent(new FocusEvent('blur', {bubbles: true}));
                        return JSON.stringify({success: true});
                    } catch(e) {
                        return JSON.stringify({success: false, error: e.message});
                    }
                })()
            """.trimIndent()

            evaluateJsAndWait(jsCode)
        } catch (e: Exception) {
            Logger.e("type error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun evaluate(script: String): String {
        return try {
            val escapedScript = script
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("\$", "\\\$")

            val jsCode = """
                (function() {
                    try {
                        var result = eval(`${escapedScript}`);
                        return JSON.stringify({result: result});
                    } catch(e) {
                        return JSON.stringify({error: e.message});
                    }
                })()
            """.trimIndent()

            evaluateJsAndWait(jsCode)
        } catch (e: Exception) {
            Logger.e("evaluate error", e)
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    private suspend fun evaluateJsAndWait(script: String): String {
        val result = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
            webViewManager.evaluateJs(script) { result ->
                cont.resume(result) {}
            }
        }

        if (result == null) {
            return """{"error":"No result from JavaScript evaluation"}"""
        }

        // Strip wrapping quotes if the result is a JSON string
        if (result.startsWith("\"") && result.endsWith("\"") && result.length >= 2) {
            return try {
                val inner = result.substring(1, result.length - 1)
                    .replace("\\\\\"", "\"")
                    .replace("\\\\n", "\n")
                    .replace("\\\\t", "\t")
                    .replace("\\\\", "\\")
                inner
            } catch (e: Exception) {
                result
            }
        }

        return result
    }

    @Serializable
    data class NavigateResult(
        val success: Boolean,
        val title: String = "",
        val url: String = "",
        val error: String? = null
    )

    @Serializable
    data class QueryResult(
        val error: String? = null
    )
}
