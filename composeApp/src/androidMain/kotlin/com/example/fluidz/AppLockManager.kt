package com.example.fluidz

import android.os.SystemClock

object AppLockManager {
    private var lastActiveTime: Long = 0
    private const val LOCK_TIMEOUT_MS = 5 * 60 * 1000 // 5 minutes

    fun updateActivity() {
        lastActiveTime = SystemClock.elapsedRealtime()
    }

    fun isLockRequired(): Boolean {
        if (lastActiveTime == 0L) return false
        val currentTime = SystemClock.elapsedRealtime()
        return (currentTime - lastActiveTime) > LOCK_TIMEOUT_MS
    }

    fun resetLock() {
        lastActiveTime = SystemClock.elapsedRealtime()
    }
}
