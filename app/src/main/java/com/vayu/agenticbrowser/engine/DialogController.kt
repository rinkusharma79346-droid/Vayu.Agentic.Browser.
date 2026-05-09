package com.vayu.agenticbrowser.engine

import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.suspendCancellableCoroutine

class DialogController {

    suspend fun detectDialog(webView: WebView): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var result = {detected: false, type: '', text: ''};

                        // Check for modal divs
                        var modalSelectors = [
                            '[role="dialog"]',
                            '[aria-modal="true"]',
                            '.modal', '.Modal',
                            '.dialog', '.Dialog',
                            '.popup', '.Popup',
                            '.overlay', '.Overlay',
                            '[class*="modal"]',
                            '[class*="dialog"]',
                            '[class*="popup"]',
                            '[class*="cookie"]',
                            '[class*="banner"]',
                            '[class*="consent"]',
                            '[class*="gdpr"]'
                        ];

                        for (var i = 0; i < modalSelectors.length; i++) {
                            var el = document.querySelector(modalSelectors[i]);
                            if (el && el.offsetParent !== null) {
                                var visibleText = el.innerText ? el.innerText.substring(0, 500) : '';
                                var tag = el.tagName.toLowerCase();
                                var isCookie = modalSelectors[i].indexOf('cookie') >= 0 ||
                                               modalSelectors[i].indexOf('consent') >= 0 ||
                                               modalSelectors[i].indexOf('gdpr') >= 0 ||
                                               visibleText.toLowerCase().indexOf('cookie') >= 0 ||
                                               visibleText.toLowerCase().indexOf('consent') >= 0;
                                result = {
                                    detected: true,
                                    type: isCookie ? 'cookie_banner' : 'modal',
                                    text: visibleText,
                                    selector: modalSelectors[i]
                                };
                                break;
                            }
                        }

                        // Check for open <dialog> elements
                        if (!result.detected) {
                            var dialogs = document.querySelectorAll('dialog[open]');
                            if (dialogs.length > 0) {
                                var dialog = dialogs[0];
                                result = {
                                    detected: true,
                                    type: 'native_dialog',
                                    text: dialog.innerText ? dialog.innerText.substring(0, 500) : '',
                                    selector: 'dialog[open]'
                                };
                            }
                        }

                        return JSON.stringify(result);
                    } catch(e) {
                        return JSON.stringify({detected: false, error: e.message});
                    }
                })()
            """.trimIndent()

            val rawResult = suspendCancellableCoroutine<String?> { cont ->
                webView.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
            }

            stripQuotes(rawResult ?: """{"detected":false}""")
        } catch (e: Exception) {
            Logger.e("detectDialog error", e)
            """{"detected":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun acceptDialog(webView: WebView): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var acceptPatterns = ['accept', 'ok', 'allow', 'agree', 'continue', 'got it', 'got it', 'understand', 'yes', 'confirm', 'enable', 'proceed', 'i agree', 'i accept'];
                        var allButtons = document.querySelectorAll('button, a, input[type="button"], input[type="submit"], [role="button"]');

                        for (var i = 0; i < allButtons.length; i++) {
                            var btn = allButtons[i];
                            if (btn.offsetParent === null) continue;
                            var text = (btn.textContent || btn.value || '').trim().toLowerCase();
                            for (var j = 0; j < acceptPatterns.length; j++) {
                                if (text.indexOf(acceptPatterns[j]) >= 0) {
                                    btn.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
                                    btn.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true}));
                                    btn.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true}));
                                    return JSON.stringify({success: true, clicked: text});
                                }
                            }
                        }
                        return JSON.stringify({success: false, error: 'NO_ACCEPT_BUTTON_FOUND'});
                    } catch(e) {
                        return JSON.stringify({success: false, error: e.message});
                    }
                })()
            """.trimIndent()

            val rawResult = suspendCancellableCoroutine<String?> { cont ->
                webView.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
            }

            stripQuotes(rawResult ?: """{"success":false,"error":"No result"}""")
        } catch (e: Exception) {
            Logger.e("acceptDialog error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun dismissDialog(webView: WebView): String {
        return try {
            val jsCode = """
                (function() {
                    try {
                        var dismissPatterns = ['reject', 'cancel', 'close', 'decline', 'no thanks', 'no, thanks', 'dismiss', 'deny', 'opt out', 'refuse', 'not now', 'maybe later'];
                        var allButtons = document.querySelectorAll('button, a, input[type="button"], [role="button"]');

                        for (var i = 0; i < allButtons.length; i++) {
                            var btn = allButtons[i];
                            if (btn.offsetParent === null) continue;
                            var text = (btn.textContent || btn.value || '').trim().toLowerCase();
                            for (var j = 0; j < dismissPatterns.length; j++) {
                                if (text.indexOf(dismissPatterns[j]) >= 0) {
                                    btn.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
                                    btn.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true}));
                                    btn.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true}));
                                    return JSON.stringify({success: true, clicked: text});
                                }
                            }
                        }

                        // Try close (X) buttons as fallback
                        var closeButtons = document.querySelectorAll('[class*="close"], [aria-label*="close"], [aria-label*="Close"]');
                        for (var k = 0; k < closeButtons.length; k++) {
                            var closeBtn = closeButtons[k];
                            if (closeBtn.offsetParent === null) continue;
                            closeBtn.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
                            closeBtn.dispatchEvent(new MouseEvent('mouseup', {bubbles: true, cancelable: true}));
                            closeBtn.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true}));
                            return JSON.stringify({success: true, clicked: 'close_button'});
                        }

                        return JSON.stringify({success: false, error: 'NO_DISMISS_BUTTON_FOUND'});
                    } catch(e) {
                        return JSON.stringify({success: false, error: e.message});
                    }
                })()
            """.trimIndent()

            val rawResult = suspendCancellableCoroutine<String?> { cont ->
                webView.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
            }

            stripQuotes(rawResult ?: """{"success":false,"error":"No result"}""")
        } catch (e: Exception) {
            Logger.e("dismissDialog error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
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
}
