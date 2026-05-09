package com.vayu.agenticbrowser.vault

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class SmsOtpReader private constructor() {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun readLatestOtp(timeoutMs: Long = 60_000L): String? {
        val ctx = appContext ?: return null

        val otpRegex = Regex("\\b\\d{6}\\b")
        val startTime = System.currentTimeMillis()

        return withTimeoutOrNull(timeoutMs) {
            var otp: String? = null
            while (otp == null) {
                otp = querySmsInbox(ctx, otpRegex, startTime)
                if (otp == null) {
                    delay(2000)
                }
            }
            otp
        }
    }

    private fun querySmsInbox(context: Context, regex: Regex, sinceTime: Long): String? {
        val uri = android.net.Uri.parse("content://sms/inbox")

        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf("body", "date"),
                "date > ?",
                arrayOf(sinceTime.toString()),
                "date DESC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val body = it.getString(0) ?: continue
                    val match = regex.find(body)
                    if (match != null) {
                        Logger.i("Found OTP in SMS: ${match.value}")
                        return match.value
                    }
                }
            }
            null
        } catch (e: SecurityException) {
            Logger.e("SMS read permission not granted", e)
            null
        } catch (e: Exception) {
            Logger.e("Error reading SMS inbox", e)
            null
        }
    }

    companion object {
        @Volatile
        private var instance: SmsOtpReader? = null

        fun getInstance(): SmsOtpReader {
            return instance ?: synchronized(this) {
                instance ?: SmsOtpReader().also { instance = it }
            }
        }
    }
}
