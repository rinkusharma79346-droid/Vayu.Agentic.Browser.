package com.vayu.agenticbrowser.engine

import android.webkit.JavascriptInterface
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AgenticBridge {

    private val _messageFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    @JavascriptInterface
    fun postMessage(data: String) {
        Logger.d("JS Bridge received message: $data")
        _messageFlow.tryEmit(data)
    }
}
