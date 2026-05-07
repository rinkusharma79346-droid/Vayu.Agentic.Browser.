package com.vayu.agenticbrowser.common

import android.util.Log

object Logger {

    private const val TAG = "VAYU"

    fun v(message: String) {
        Log.v(TAG, message)
    }

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String) {
        Log.e(TAG, message)
    }

    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }

    fun wtf(message: String) {
        Log.wtf(TAG, message)
    }
}
