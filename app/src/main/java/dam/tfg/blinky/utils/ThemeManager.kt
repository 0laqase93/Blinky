package dam.tfg.blinky.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

/**
 * Manager for handling theme preferences (dark/light mode)
 */
class ThemeManager(context: Context) {
    companion object {
        private const val PREF_NAME = "BlinkyThemePrefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FOLLOW_SYSTEM = "follow_system"

        // Singleton instance
        @Volatile
        private var INSTANCE: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // State objects that will be observed in the UI
    private val _isDarkMode = mutableStateOf(loadDarkModePreference())
    val isDarkMode: State<Boolean> = _isDarkMode

    private val _followSystem = mutableStateOf(loadFollowSystemPreference())
    val followSystem: State<Boolean> = _followSystem

    /**
     * Load dark mode preference from SharedPreferences
     */
    private fun loadDarkModePreference(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * Load follow system preference from SharedPreferences
     */
    private fun loadFollowSystemPreference(): Boolean {
        return prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
    }

    /**
     * Set dark mode preference
     */
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).commit()
        _isDarkMode.value = isDark
    }

    /**
     * Set whether to follow system theme
     */
    fun setFollowSystem(follow: Boolean) {
        prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, follow).commit()
        _followSystem.value = follow
    }

    /**
     * Toggle dark mode
     */
    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    /**
     * Toggle follow system setting
     */
    fun toggleFollowSystem() {
        setFollowSystem(!_followSystem.value)
    }

    /**
     * Clear all theme preferences and reset to defaults
     */
    fun clearPreferences() {
        prefs.edit().clear().commit()
        _isDarkMode.value = false
        _followSystem.value = true
    }
}
