package dam.tfg.blinky.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    companion object {
        private const val PREF_NAME = "BlinkyPrefs"
        private const val KEY_TOKEN = "auth_token"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun hasToken(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}