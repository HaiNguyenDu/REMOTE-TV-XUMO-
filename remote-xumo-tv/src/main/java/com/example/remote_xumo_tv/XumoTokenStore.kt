package com.example.remote_xumo_tv

import android.content.Context

internal class XumoTokenStore(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    fun clear() {
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
    }

    private companion object {
        const val PREFS = "xumo_remote_store"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
