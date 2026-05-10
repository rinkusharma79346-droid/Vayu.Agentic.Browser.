package com.vayu.agenticbrowser.vault

import com.vayu.agenticbrowser.common.Logger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpGenerator {

    private const val TIME_STEP_SECONDS = 30
    private const val CODE_DIGITS = 6
    private const val HMAC_ALGORITHM = "HmacSHA1"

    fun generate(base32Seed: String): String {
        val key = decodeBase32(base32Seed)
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        val timeStep = currentTimeSeconds / TIME_STEP_SECONDS

        val timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val hash = mac.doFinal(timeBytes)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val otp = binary % Math.pow(10.0, CODE_DIGITS.toDouble()).toInt()
        return otp.toString().padStart(CODE_DIGITS, '0')
    }

    fun getSecondsRemaining(): Int {
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return (TIME_STEP_SECONDS - (currentTimeSeconds % TIME_STEP_SECONDS)).toInt()
    }

    private fun decodeBase32(base32: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = base32.uppercase().replace("=", "").replace(" ", "")
        val buffer = mutableListOf<Byte>()
        var bits = 0L
        var bitCount = 0

        for (char in cleaned) {
            val value = alphabet.indexOf(char)
            if (value < 0) continue
            bits = (bits shl 5) or value.toLong()
            bitCount += 5
            if (bitCount >= 8) {
                bitCount -= 8
                buffer.add((bits shr bitCount).toByte())
            }
        }

        return buffer.toByteArray()
    }
}

// Need java.nio.ByteBuffer for time step encoding
private typealias ByteBuffer = java.nio.ByteBuffer
