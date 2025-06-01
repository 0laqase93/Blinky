package dam.tfg.blinky.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility class for handling permissions
 */
object PermissionsUtils {
    private const val MICROPHONE_PERMISSION_REQUEST_CODE = 101
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 102

    /**
     * Checks if the app has microphone permission
     *
     * @param context The context to check permissions in
     * @return True if the app has microphone permission, false otherwise
     */
    fun hasMicrophonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests microphone permission
     *
     * @param activity The activity requesting permission
     */
    fun requestMicrophonePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            MICROPHONE_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Checks if the app has notification permission (Android 13+)
     *
     * @param context The context to check permissions in
     * @return True if the app has notification permission or if the device is running Android 12 or lower, false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        // Notification permission is only required for Android 13+ (API level 33+)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notification permission is granted by default
            true
        }
    }

    /**
     * Requests notification permission (Android 13+)
     *
     * @param activity The activity requesting permission
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Opens the app settings page
     *
     * @param context The context to open settings from
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}