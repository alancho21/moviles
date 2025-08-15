package com.example.apiconnection.Network

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun save(token: String) {
        prefs.edit().putString("token", token).apply()
    }
    fun get(): String? = prefs.getString("token", null)
    fun clear() { prefs.edit().remove("token").apply() }
    fun hasToken(): Boolean = get().isNullOrBlank().not()
}
