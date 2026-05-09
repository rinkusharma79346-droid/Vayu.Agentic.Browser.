package com.vayu.agenticbrowser.vault

import android.webkit.WebView
import com.vayu.agenticbrowser.common.Logger
import com.vayu.agenticbrowser.engine.DomController
import com.vayu.agenticbrowser.tabs.TabManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CredentialVault(
    private val profileManager: ProfileManager,
    private val biometricAuth: BiometricAuth,
    private val domController: DomController,
    private val tabManager: TabManager
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    companion object {
        private const val KEY_ALIAS = "vayu_vault_key"
    }

    suspend fun fillLoginForm(siteUrl: String, webView: WebView): String {
        if (!biometricAuth.requireUnlock()) {
            return """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
        }

        val profiles = profileManager.listProfilesInternal()
        val matchingProfile = findBestMatch(profiles, siteUrl)

        if (matchingProfile == null) {
            return """{"error":"NO_PROFILE","message":"No profile found matching site: $siteUrl"}"""
        }

        profileManager.updateLastUsed(matchingProfile.id)

        val password = try {
            CryptoUtils.decrypt(matchingProfile.encryptedPassword, KEY_ALIAS)
        } catch (e: Exception) {
            Logger.e("Failed to decrypt password", e)
            return """{"error":"DECRYPT_ERROR","message":"Failed to decrypt password"}"""
        }

        val formResult = try {
            val formDetectJs = """
                (function() {
                    var fields = [];
                    var inputs = document.querySelectorAll('input[type="email"], input[type="text"], input[type="tel"], input[type="password"], input:not([type])');
                    for (var i = 0; i < inputs.length; i++) {
                        var el = inputs[i];
                        var isEmail = (el.type === 'email') ||
                                      (el.name && el.name.toLowerCase().includes('email')) ||
                                      (el.id && el.id.toLowerCase().includes('email')) ||
                                      (el.placeholder && el.placeholder.toLowerCase().includes('email')) ||
                                      (el.autocomplete && el.autocomplete.includes('email'));
                        var isPassword = el.type === 'password';
                        var isUser = !isEmail && !isPassword &&
                                     ((el.name && (el.name.toLowerCase().includes('user') || el.name.toLowerCase().includes('login'))) ||
                                      (el.id && (el.id.toLowerCase().includes('user') || el.id.toLowerCase().includes('login'))));
                        if (isEmail || isUser || isPassword) {
                            fields.push({
                                selector: el.id ? '#' + CSS.escape(el.id) : el.name ? '[name="' + el.name + '"]' : 'input:nth-of-type(' + (Array.from(el.parentNode.querySelectorAll('input')).indexOf(el) + 1) + ')',
                                type: isEmail ? 'email' : isPassword ? 'password' : 'username'
                            });
                        }
                    }
                    return JSON.stringify(fields);
                })()
            """.trimIndent()

            val formJson = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                webView.evaluateJavascript(formDetectJs) { result -> cont.resume(result) {} }
            }

            val fieldsResult = formJson?.let { parseJsonFields(it) } ?: emptyList()

            var emailFilled = ""
            var passwordFilled = false

            for (field in fieldsResult) {
                when (field.type) {
                    "email", "username" -> {
                        domController.type(field.selector, matchingProfile.username, true)
                        emailFilled = matchingProfile.username
                    }
                    "password" -> {
                        domController.type(field.selector, password, true)
                        passwordFilled = true
                    }
                }
            }

            """{"filled":true,"fields":{"email":"${emailFilled.replace("\"", "\\\"")}","password":"***"}}"""
        } catch (e: Exception) {
            Logger.e("fillLoginForm error", e)
            """{"error":"FILL_ERROR","message":"${e.message?.replace("\"", "\\\"")}"}"""
        }

        return formResult
    }

    fun getDecryptedPassword(profileId: String): String {
        if (!biometricAuth.requireUnlock()) {
            return """{"error":"AUTH_REQUIRED","message":"Vault is locked. Biometric authentication required."}"""
        }

        val profile = profileManager.getProfile(profileId)
            ?: return """{"error":"NOT_FOUND","message":"Profile $profileId not found"}"""

        return try {
            CryptoUtils.decrypt(profile.encryptedPassword, KEY_ALIAS)
        } catch (e: Exception) {
            Logger.e("Failed to decrypt password for profile $profileId", e)
            """{"error":"DECRYPT_ERROR","message":"Failed to decrypt password"}"""
        }
    }

    fun getDecryptedTotpSeed(profileId: String): String? {
        if (!biometricAuth.requireUnlock()) {
            return null
        }

        val profile = profileManager.getProfile(profileId) ?: return null
        val encryptedSeed = profile.encryptedTotpSeed ?: return null

        return try {
            CryptoUtils.decrypt(encryptedSeed, KEY_ALIAS)
        } catch (e: Exception) {
            Logger.e("Failed to decrypt TOTP seed for profile $profileId", e)
            null
        }
    }

    private fun findBestMatch(profiles: List<AccountProfile>, siteUrl: String): AccountProfile? {
        val siteHost = extractHost(siteUrl)

        // Exact match first
        val exactMatch = profiles.find { extractHost(it.siteUrl) == siteHost }
        if (exactMatch != null) return exactMatch

        // Contains match
        val containsMatch = profiles.find {
            siteHost.contains(extractHost(it.siteUrl)) || extractHost(it.siteUrl).contains(siteHost)
        }
        if (containsMatch != null) return containsMatch

        // Last used for this domain
        val recentMatch = profiles
            .filter { it.siteUrl.contains(siteHost.substringBefore(".")) }
            .maxByOrNull { it.lastUsedAt }
        return recentMatch
    }

    private fun extractHost(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (e: Exception) {
            url.removePrefix("https://").removePrefix("http://").substringBefore("/")
        }
    }

    private fun parseJsonFields(jsonStr: String): List<FormDetectedField> {
        return try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                val unquoted = trimmed.substring(1, trimmed.length - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                json.decodeFromString<List<FormDetectedField>>(unquoted)
            } else {
                json.decodeFromString<List<FormDetectedField>>(trimmed)
            }
        } catch (e: Exception) {
            Logger.e("Failed to parse form fields", e)
            emptyList()
        }
    }

    @kotlinx.serialization.Serializable
    data class FormDetectedField(
        val selector: String,
        val type: String
    )
}
