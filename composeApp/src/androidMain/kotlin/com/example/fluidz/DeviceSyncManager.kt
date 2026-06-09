package com.example.fluidz

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import java.util.UUID

object DeviceSyncManager {
    private const val KEY_DEVICE_ID = "device_unique_id"
    private const val KEY_SYNCED_DEVICES = "synced_device_list"
    private const val MAX_DEVICES = 5

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
