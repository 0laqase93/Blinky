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
    private const val DEFAULT_SERVER_IP = "192.168.1.20"
    private const val DEFAULT_SERVER_PORT = "8080"

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
}