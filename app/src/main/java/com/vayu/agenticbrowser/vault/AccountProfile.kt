package com.vayu.agenticbrowser.vault

import kotlinx.serialization.Serializable

@Serializable
data class AccountProfile(
    val id: String,
    val name: String,
    val siteUrl: String,
    val username: String,
    val encryptedPassword: String,
    val phoneNumber: String? = null,
    val encryptedTotpSeed: String? = null,
    val encryptedBackupCodes: String? = null,
    val savedCookiesJson: String? = null,
    val createdAt: Long,
    val lastUsedAt: Long
)
