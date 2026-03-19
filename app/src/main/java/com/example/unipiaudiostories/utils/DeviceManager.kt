package com.example.unipiaudiostories.utils

import android.content.Context
import java.util.UUID
import androidx.core.content.edit

/**
 * Utility singleton for managing a unique identifier for the device.
 * Used to track user history and favorites anonymously.
 */
object DeviceIdManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    /**
     * Retrieves or generates a unique UUID for the device.
     * Persists the ID in SharedPreferences.
     */
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_ID, deviceId) }
        }

        return deviceId
    }
}