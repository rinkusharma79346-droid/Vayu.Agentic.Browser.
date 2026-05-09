package com.vayu.agenticbrowser.vault

import android.content.Context
import android.content.SharedPreferences
import com.vayu.agenticbrowser.common.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ProfileManager private constructor() {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var prefs: SharedPreferences? = null

    private val KEY_ALIAS = "vayu_vault_key"
    private val PREFS_NAME = "vayu_vault"
    private val PROFILES_KEY = "encrypted_profiles"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveProfile(profile: AccountProfile) {
        val profiles = loadAllProfilesInternal().toMutableList()
        val existingIndex = profiles.indexOfFirst { it.id == profile.id }
        if (existingIndex >= 0) {
            profiles[existingIndex] = profile
        } else {
            profiles.add(profile)
        }
        saveAllProfilesInternal(profiles)
        Logger.i("Saved profile: ${profile.name} (${profile.id})")
    }

    fun getProfile(id: String): AccountProfile? {
        return loadAllProfilesInternal().find { it.id == id }
    }

    fun listProfiles(): List<AccountProfile> {
        return loadAllProfilesInternal().map { profile ->
            profile.copy(
                encryptedPassword = "***",
                encryptedTotpSeed = null,
                encryptedBackupCodes = null
            )
        }
    }

    fun listProfilesInternal(): List<AccountProfile> {
        return loadAllProfilesInternal()
    }

    fun deleteProfile(id: String): Boolean {
        val profiles = loadAllProfilesInternal().toMutableList()
        val removed = profiles.removeAll { it.id == id }
        if (removed) {
            saveAllProfilesInternal(profiles)
            Logger.i("Deleted profile: $id")
        }
        return removed
    }

    fun updateLastUsed(id: String) {
        val profiles = loadAllProfilesInternal().toMutableList()
        val index = profiles.indexOfFirst { it.id == id }
        if (index >= 0) {
            profiles[index] = profiles[index].copy(lastUsedAt = System.currentTimeMillis())
            saveAllProfilesInternal(profiles)
        }
    }

    private fun loadAllProfilesInternal(): List<AccountProfile> {
        val sp = prefs ?: return emptyList()
        val encrypted = sp.getString(PROFILES_KEY, null) ?: return emptyList()

        return try {
            val decrypted = CryptoUtils.decrypt(encrypted, KEY_ALIAS)
            json.decodeFromString<List<AccountProfile>>(decrypted)
        } catch (e: Exception) {
            Logger.e("Failed to decrypt profiles", e)
            emptyList()
        }
    }

    private fun saveAllProfilesInternal(profiles: List<AccountProfile>) {
        val sp = prefs ?: return
        try {
            val jsonString = json.encodeToString(profiles)
            val encrypted = CryptoUtils.encrypt(jsonString, KEY_ALIAS)
            sp.edit().putString(PROFILES_KEY, encrypted).apply()
        } catch (e: Exception) {
            Logger.e("Failed to encrypt profiles", e)
        }
    }

    companion object {
        @Volatile
        private var instance: ProfileManager? = null

        fun getInstance(): ProfileManager {
            return instance ?: synchronized(this) {
                instance ?: ProfileManager().also { instance = it }
            }
        }
    }
}
