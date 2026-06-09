package com.example.fluidz

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import java.util.UUID

object DeviceSyncManager {
    private const val KEY_DEVICE_ID = "device_unique_id"
    private const val KEY_SYNCED_DEVICES = "synced_device_list"
    private const val KEY_SYNC_KEY = "fluidz_sync_key"
    private const val MAX_DEVICES = 5

    fun getSyncKey(context: Context): String? {
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_SYNC_KEY, null)
    }

    fun setSyncKey(context: Context, key: String) {
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        prefs.edit { putString(KEY_SYNC_KEY, key.uppercase()) }
    }

    fun generateNewSyncKey(context: Context): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val newKey = (1..8)
            .map { allowedChars.random() }
            .joinToString("")
            .let { "FDZ-$it" }
        
        setSyncKey(context, newKey)
        return newKey
    }

    fun getDeviceId(context: Context): String {
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_ID, id) }
        }
        return id
    }

    fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun getSyncedDevices(context: Context): List<String> {
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        return prefs.getStringSet(KEY_SYNCED_DEVICES, emptySet())?.toList() ?: emptyList()
    }

    fun addDevice(context: Context, deviceIdentifier: String): Boolean {
        val currentDevices = getSyncedDevices(context).toMutableSet()
        if (currentDevices.size >= MAX_DEVICES) return false
        
        currentDevices.add(deviceIdentifier)
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        prefs.edit { putStringSet(KEY_SYNCED_DEVICES, currentDevices) }
        return true
    }

    fun removeDevice(context: Context, deviceIdentifier: String) {
        val currentDevices = getSyncedDevices(context).toMutableSet()
        currentDevices.remove(deviceIdentifier)
        val prefs = SecurityUtils.getEncryptedSharedPreferences(context)
        prefs.edit { putStringSet(KEY_SYNCED_DEVICES, currentDevices) }
    }
}
