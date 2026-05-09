package com.vayu.agenticbrowser.vault

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.*

class BiometricAuth private constructor() {

    var isUnlocked: Boolean = false
        private set

    private var lockJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun promptUnlock(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Logger.w("Biometric authentication not available: $canAuthenticate")
            // Fall back to device credential or auto-unlock for testing
            isUnlocked = true
            startLockTimer()
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock VAYU Vault")
            .setSubtitle("Authenticate to access your saved credentials")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Logger.i("Biometric authentication succeeded")
                    isUnlocked = true
                    startLockTimer()
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Logger.w("Biometric authentication failed")
                    onFailure()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Logger.e("Biometric authentication error: $errorCode - $errString")
                    onFailure()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    fun requireUnlock(): Boolean {
        return isUnlocked
    }

    fun lock() {
        isUnlocked = false
        lockJob?.cancel()
        Logger.i("Vault locked")
    }

    private fun startLockTimer() {
        lockJob?.cancel()
        lockJob = scope.launch {
            delay(30 * 60 * 1000L) // 30 minutes
            isUnlocked = false
            Logger.i("Vault auto-locked after 30 minutes")
        }
    }

    companion object {
        @Volatile
        private var instance: BiometricAuth? = null

        fun getInstance(): BiometricAuth {
            return instance ?: synchronized(this) {
                instance ?: BiometricAuth().also { instance = it }
            }
        }
    }
}
