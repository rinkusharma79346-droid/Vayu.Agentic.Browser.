package com.vayu.agenticbrowser.engine

import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

class FormDetector(
    private val domController: DomController
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun detectForms(webView: WebView): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var fields = [];
                        var inputs = document.querySelectorAll('input, select, textarea');
                        for (var i = 0; i < inputs.length; i++) {
                            var el = inputs[i];
                            var inputType = el.type || el.tagName.toLowerCase();
                            fields.push({
                                selector: el.id ? '#' + CSS.escape(el.id) : (el.name ? '[name="' + el.name + '"]' : el.tagName.toLowerCase() + ':nth-of-type(' + (i + 1) + ')'),
                                type: inputType,
                                name: el.name || '',
                                id: el.id || '',
                                placeholder: el.placeholder || '',
                                value: (inputType === 'password') ? '' : (el.value || ''),
                                visible: el.offsetParent !== null || el.style.display !== 'none'
                            });
                        }
                        return JSON.stringify(fields);
                    } catch(e) {
                        return JSON.stringify({error: e.message});
                    }
                })()
            """.trimIndent()

            val rawResult = suspendCancellableCoroutine<String?> { cont ->
                webView.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
            }

            if (rawResult == null) {
                return """{"error":"No result from JavaScript evaluation"}"""
            }

            stripQuotes(rawResult)
        } catch (e: Exception) {
            Logger.e("detectForms error", e)
            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun fillForm(
        mapping: Map<String, String>,
        submitSelector: String?,
        webView: WebView
    ): String {
        return try {
            val tabId = findTabIdForWebView(webView)
            var filledCount = 0
            val errors = mutableListOf<String>()

            for ((selector, value) in mapping) {
                try {
                    val result = domController.type(selector, value, true, tabId)
                    val parsed = json.parseToJsonElement(result).jsonObject
                    if (parsed["success"]?.jsonPrimitive?.booleanOrNull == true) {
                        filledCount++
                    } else {
                        val error = parsed["error"]?.jsonPrimitive?.contentOrNull ?: "Unknown error"
                        errors.add("$selector: $error")
                    }
                } catch (e: Exception) {
                    errors.add("$selector: ${e.message}")
                }
            }

            if (submitSelector != null) {
                try {
                    domController.click(submitSelector, 0, tabId)
                } catch (e: Exception) {
                    errors.add("submit: ${e.message}")
                }
            }

            json.encodeToString(
                FillFormResult(filled = filledCount, errors = errors)
            )
        } catch (e: Exception) {
            Logger.e("fillForm error", e)
            """{"filled":0,"errors":["${e.message?.replace("\"", "\\\"")}"]}"""
        }
    }

    private fun findTabIdForWebView(webView: WebView): Int? {
        val tabManager = com.vayu.agenticbrowser.tabs.TabManager.getInstance()
        val tabs = tabManager.tabs.value
        for (tab in tabs) {
            if (tabManager.getTab(tab.tabId) === webView) {
                return tab.tabId
            }
        }
        return null
    }

    private fun stripQuotes(result: String): String {
        if (result.startsWith("\"") && result.endsWith("\"") && result.length >= 2) {
            return try {
                result.substring(1, result.length - 1)
                    .replace("\\\\\"", "\"")
                    .replace("\\\\n", "\n")
                    .replace("\\\\t", "\t")
                    .replace("\\\\", "\\")
            } catch (e: Exception) {
                result
            }
        }
        return result
    }

    @Serializable
    data class FillFormResult(
        val filled: Int,
        val errors: List<String>
    )
}
