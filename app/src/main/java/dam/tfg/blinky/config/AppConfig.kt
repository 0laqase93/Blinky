package dam.tfg.blinky.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuration class for the application.
 * This class provides a centralized place to manage configuration values like server IP address.
 */
object AppConfig {
    private const val PREFS_NAME = "app_config"
    private const val KEY_SERVER_IP = "server_ip"
    private const val DEFAULT_SERVER_IP = "89.39.156.185"
    private const val DEFAULT_SERVER_PORT = "8080"
    private const val KEY_AI_PERSONALITY = "ai_personality"
    private const val DEFAULT_AI_PERSONALITY = "Motivadora"
    private const val KEY_AI_PERSONALITY_ID = "ai_personality_id"
    private const val DEFAULT_AI_PERSONALITY_ID = -1L
    private const val KEY_DEAF_MODE = "deaf_mode"
    private const val DEFAULT_DEAF_MODE = false
    private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    private const val DEFAULT_NOTIFICATION_ENABLED = true
    private const val KEY_NOTIFICATION_HOURS = "notification_hours"
    private const val DEFAULT_NOTIFICATION_HOURS = 1
    private const val KEY_NOTIFICATION_MINUTES = "notification_minutes"
    private const val DEFAULT_NOTIFICATION_MINUTES = 0

    private lateinit var prefs: SharedPreferences

    /**
     * Initialize the AppConfig with a context.
     * This should be called in the Application class or MainActivity.
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get the server IP address.
     * @return The server IP address from SharedPreferences or the default value.
     */
    fun getServerIp(): String {
        return prefs.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    /**
     * Set the server IP address.
     * @param ip The new server IP address.
     */
    fun setServerIp(ip: String) {
        prefs.edit().putString(KEY_SERVER_IP, ip).apply()
    }

    /**
     * Get the server port.
     * @return The server port.
     */
    fun getServerPort(): String {
        return DEFAULT_SERVER_PORT
    }

    /**
     * Get the full server URL.
     * @return The full server URL with protocol, IP, and port.
     */
    fun getServerUrl(): String {
        return "http://${getServerIp()}:${getServerPort()}"
    }

    /**
     * Get the AI personality setting.
     * @return The AI personality from SharedPreferences or the default value.
     */
    fun getAIPersonality(): String {
        return prefs.getString(KEY_AI_PERSONALITY, DEFAULT_AI_PERSONALITY) ?: DEFAULT_AI_PERSONALITY
    }

    /**
     * Set the AI personality setting.
     * @param personality The new AI personality value.
     */
    fun setAIPersonality(personality: String) {
        prefs.edit().putString(KEY_AI_PERSONALITY, personality).apply()
    }

    /**
     * Get the AI personality ID setting.
     * @return The AI personality ID from SharedPreferences or the default value.
     */
    fun getAIPersonalityId(): Long {
        return prefs.getLong(KEY_AI_PERSONALITY_ID, DEFAULT_AI_PERSONALITY_ID)
    }

    /**
     * Set the AI personality ID setting.
     * @param personalityId The new AI personality ID value.
     */
    fun setAIPersonalityId(personalityId: Long) {
        prefs.edit().putLong(KEY_AI_PERSONALITY_ID, personalityId).apply()
    }

    /**
     * Set both the AI personality name and ID.
     * @param personality The new AI personality name.
     * @param personalityId The new AI personality ID.
     */
    fun setAIPersonalityWithId(personality: String, personalityId: Long) {
        prefs.edit()
            .putString(KEY_AI_PERSONALITY, personality)
            .putLong(KEY_AI_PERSONALITY_ID, personalityId)
            .apply()
    }

    /**
     * Get the deaf mode setting.
     * @return The deaf mode state from SharedPreferences or the default value.
     */
    fun getDeafMode(): Boolean {
        return prefs.getBoolean(KEY_DEAF_MODE, DEFAULT_DEAF_MODE)
    }

    /**
     * Set the deaf mode setting.
     * @param enabled The new deaf mode state.
     */
    fun setDeafMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEAF_MODE, enabled).apply()
    }

    /**
     * Get whether notifications are enabled.
     * @return Whether notifications are enabled from SharedPreferences or the default value.
     */
    fun getNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, DEFAULT_NOTIFICATION_ENABLED)
    }

    /**
     * Set whether notifications are enabled.
     * @param enabled The new notification enabled state.
     */
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    /**
     * Get the notification hours setting.
     * @return The notification hours from SharedPreferences or the default value.
     */
    fun getNotificationHours(): Int {
        return prefs.getInt(KEY_NOTIFICATION_HOURS, DEFAULT_NOTIFICATION_HOURS)
    }

    /**
     * Set the notification hours setting.
     * @param hours The new notification hours value.
     */
    fun setNotificationHours(hours: Int) {
        prefs.edit().putInt(KEY_NOTIFICATION_HOURS, hours).apply()
    }

    /**
     * Get the notification minutes setting.
     * @return The notification minutes from SharedPreferences or the default value.
     */
    fun getNotificationMinutes(): Int {
        return prefs.getInt(KEY_NOTIFICATION_MINUTES, DEFAULT_NOTIFICATION_MINUTES)
    }

    /**
     * Set the notification minutes setting.
     * @param minutes The new notification minutes value.
     */
    fun setNotificationMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_NOTIFICATION_MINUTES, minutes).apply()
    }

    /**
     * Set both notification hours and minutes.
     * @param hours The new notification hours value.
     * @param minutes The new notification minutes value.
     */
    fun setNotificationTime(hours: Int, minutes: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOURS, hours)
            .putInt(KEY_NOTIFICATION_MINUTES, minutes)
            .apply()
    }
}
