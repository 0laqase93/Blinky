package dam.tfg.blinky.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Manager for handling user information
 */
class UserManager(context: Context) {
    companion object {
        private const val PREF_NAME = "BlinkyUserPrefs"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_PASSWORD = "user_password"
        private const val KEY_TOKEN = "user_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ADMIN = "user_admin"

        // Singleton instance
        @Volatile
        private var INSTANCE: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(context).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // State objects that will be observed in the UI
    private val _userEmail = mutableStateOf(prefs.getString(KEY_EMAIL, "") ?: "")
    val userEmail: State<String> = _userEmail

    private val _userName = mutableStateOf(prefs.getString(KEY_NAME, "") ?: "")
    val userName: State<String> = _userName

    private val _userPassword = mutableStateOf(prefs.getString(KEY_PASSWORD, "") ?: "")
    val userPassword: State<String> = _userPassword

    private val _token = mutableStateOf(prefs.getString(KEY_TOKEN, "") ?: "")
    val token: State<String> = _token

    private val _userId = mutableStateOf(prefs.getLong(KEY_USER_ID, -1))
    val userId: State<Long> = _userId

    private val _isAdmin = mutableStateOf(prefs.getBoolean(KEY_ADMIN, false))
    val isAdmin: State<Boolean> = _isAdmin

    /**
     * Save user email
     */
    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
        _userEmail.value = email
    }

    /**
     * Save user name
     */
    fun saveName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
        _userName.value = name
    }

    /**
     * Save user password
     */
    fun savePassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
        _userPassword.value = password
    }

    /**
     * Save user token
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _token.value = token
    }

    /**
     * Save user ID
     */
    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
        _userId.value = userId
    }

    /**
     * Save admin status
     */
    fun saveAdminStatus(isAdmin: Boolean) {
        prefs.edit().putBoolean(KEY_ADMIN, isAdmin).apply()
        _isAdmin.value = isAdmin
    }

    /**
     * Save user data from API response
     */
    fun saveUserData(email: String, password: String, username: String, token: String = "", userId: Long = -1, isAdmin: Boolean = false) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_NAME, username)
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putBoolean(KEY_ADMIN, isAdmin)
            .apply()
        _userEmail.value = email
        _userPassword.value = password
        _userName.value = username
        _token.value = token
        _userId.value = userId
        _isAdmin.value = isAdmin
    }

    /**
     * Save complete user data from login response
     */
    fun saveCompleteUserData(token: String, userId: Long, email: String, username: String, isAdmin: Boolean, password: String = "") {
        saveUserData(email, password, username, token, userId, isAdmin)
    }

    /**
     * Save user data from API response (legacy method)
     */
    fun saveUserData(email: String, password: String) {
        // Extract username from email (everything before @)
        val username = email.split("@").firstOrNull() ?: email
        saveUserData(email, password, username)
    }

    /**
     * Clear user data
     */
    fun clearUserData() {
        prefs.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ADMIN)
            .apply()
        _userEmail.value = ""
        _userName.value = ""
        _userPassword.value = ""
        _token.value = ""
        _userId.value = -1
        _isAdmin.value = false
    }
}
