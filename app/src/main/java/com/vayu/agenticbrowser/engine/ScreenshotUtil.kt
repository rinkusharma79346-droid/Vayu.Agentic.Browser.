package com.vayu.agenticbrowser.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Base64
import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream

object ScreenshotUtil {

    suspend fun screenshotFull(webView: WebView): String {
        return try {
            val bitmap = suspendCancellableCoroutine<Bitmap> { cont ->
                webView.post {
                    try {
                        val width = webView.width
                        val height = webView.contentHeight * webView.scale
                        val bmpHeight = height.toInt().coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(width, bmpHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)
                        cont.resume(bitmap) {}
                    } catch (e: Exception) {
                        cont.cancel(e)
                    }
                }
            }

            val base64 = bitmapToBase64Png(bitmap)
            bitmap.recycle()
            """{"success":true,"base64":"$base64","width":${bitmap.width},"height":${bitmap.height}}"""
        } catch (e: Exception) {
            Logger.e("screenshotFull error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    suspend fun screenshotElement(webView: WebView, selector: String): String {
        return try {
            val boundsJson = suspendCancellableCoroutine<String?> { cont ->
                val jsCode = """
                    (function() {
                        try {
                            var el = document.querySelector('${selector.replace("'", "\\'")}');
                            if (!el) return JSON.stringify({error: 'ELEMENT_NOT_FOUND'});
                            var rect = el.getBoundingClientRect();
                            return JSON.stringify({
                                left: rect.left,
                                top: rect.top,
                                width: rect.width,
                                height: rect.height,
                                scrollX: window.scrollX,
                                scrollY: window.scrollY
                            });
                        } catch(e) {
                            return JSON.stringify({error: e.message});
                        }
                    })()
                """.trimIndent()
                webView.evaluateJavascript(jsCode) { result -> cont.resume(result) {} }
            }

            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val parsed = json.parseToJsonElement(boundsJson ?: """{"error":"No result"}""").jsonObject

            if (parsed.containsKey("error")) {
                val errorMsg = parsed["error"]?.jsonPrimitive?.content ?: "Unknown error"
                return """{"success":false,"error":"$errorMsg"}"""
            }

            val left = parsed["left"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0
            val top = parsed["top"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0
            val elWidth = parsed["width"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0
            val elHeight = parsed["height"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0
            val scrollX = parsed["scrollX"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0
            val scrollY = parsed["scrollY"]?.jsonPrimitive?.floatOrNull?.toInt() ?: 0

            if (elWidth <= 0 || elHeight <= 0) {
                return """{"success":false,"error":"Element has zero dimensions"}"""
            }

            val scale = webView.scale

            val fullBitmap = suspendCancellableCoroutine<Bitmap> { cont ->
                webView.post {
                    try {
                        val viewWidth = webView.width
                        val contentHeight = webView.contentHeight * scale
                        val bmpHeight = contentHeight.toInt().coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(viewWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)
                        cont.resume(bitmap) {}
                    } catch (e: Exception) {
                        cont.cancel(e)
                    }
                }
            }

            val cropLeft = ((left + scrollX) * scale).toInt().coerceAtLeast(0)
            val cropTop = ((top + scrollY) * scale).toInt().coerceAtLeast(0)
            val cropWidth = (elWidth * scale).toInt().coerceAtMost(fullBitmap.width - cropLeft)
            val cropHeight = (elHeight * scale).toInt().coerceAtMost(fullBitmap.height - cropTop)

            if (cropWidth <= 0 || cropHeight <= 0) {
                fullBitmap.recycle()
                return """{"success":false,"error":"Crop dimensions invalid"}"""
            }

            val croppedBitmap = Bitmap.createBitmap(fullBitmap, cropLeft, cropTop, cropWidth, cropHeight)
            val base64 = bitmapToBase64Png(croppedBitmap)

            fullBitmap.recycle()
            croppedBitmap.recycle()

            """{"success":true,"base64":"$base64","width":$cropWidth,"height":$cropHeight}"""
        } catch (e: Exception) {
            Logger.e("screenshotElement error", e)
            """{"success":false,"error":"${e.message?.replace("\"", "\\\"")}"}"""
        }
    }

    private fun bitmapToBase64Png(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
