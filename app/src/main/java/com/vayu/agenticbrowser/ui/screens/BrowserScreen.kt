package com.vayu.agenticbrowser.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vayu.agenticbrowser.engine.WebViewManager
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    agentConnected: StateFlow<Boolean>
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf("https://www.google.com") }
    val isConnected by agentConnected.collectAsState()

    val webViewManager = remember { WebViewManager.getInstance() }

    Column(modifier = Modifier.fillMaxSize()) {
        // URL Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var url = urlText.trim()
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "https://$url"
                                urlText = url
                            }
                            webViewManager.loadUrl(url)
                        }
                    )
                )

                IconButton(
                    onClick = {
                        var url = urlText.trim()
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://$url"
                            urlText = url
                        }
                        webViewManager.loadUrl(url)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go"
                    )
                }
            }
        }

        // WebView
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    webViewManager.init(ctx)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Status Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (isConnected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                    ) {}
                    Text(
                        text = if (isConnected) "Agent: Connected" else "Agent: Disconnected",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "MCP Port: 8765",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
