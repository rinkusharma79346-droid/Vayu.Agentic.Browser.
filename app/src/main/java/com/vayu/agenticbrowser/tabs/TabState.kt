package com.vayu.agenticbrowser.tabs

import kotlinx.serialization.Serializable

@Serializable
data class TabState(
    val tabId: Int,
    val title: String,
    val url: String,
    val loading: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val lastAccessedAt: Long
)
