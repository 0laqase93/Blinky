package dam.tfg.blinky.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import dam.tfg.blinky.R
import dam.tfg.blinky.dataclass.WrenchEmotion
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import android.util.Log
import dam.tfg.blinky.presentation.activities.MainActivity

/**
 * Implementation of App Widget functionality for Blinky's eyes.
 * This widget displays Blinky's eyes on the home screen and allows cycling through different emotions.
 */
class BlinkyWidgetProvider : AppWidgetProvider() {

    companion object {
        // Tag for logging
        private const val TAG = "BlinkyWidgetProvider"

        // Actions
        private const val ACTION_CHANGE_EMOTION = "dam.tfg.blinky.widget.ACTION_CHANGE_EMOTION"
        private const val ACTION_AUTO_UPDATE = "dam.tfg.blinky.widget.ACTION_AUTO_UPDATE"

        // Preference name for storing current emotion
        private const val PREF_NAME = "BlinkyWidgetPrefs"
        private const val PREF_EMOTION_INDEX = "emotionIndex"

        // Update interval in milliseconds (default: 10 seconds)
        private const val UPDATE_INTERVAL_MS = 10000L

        // List of emotions to cycle through
        private val EMOTIONS = listOf(
            WrenchEmotion.NEUTRAL,
            WrenchEmotion.HAPPY,
            WrenchEmotion.SAD,
            WrenchEmotion.ANGRY,
            WrenchEmotion.CONFUSED,
            WrenchEmotion.QUESTION,
            WrenchEmotion.ERROR,
            WrenchEmotion.MEH
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Make sure the auto-update is scheduled
        // This ensures the alarm is set even after device reboot
        scheduleAutoUpdate(context)

        Log.d(TAG, "Widget onUpdate called, ensuring auto-update is scheduled")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            // Handle automatic emotion change (triggered by AlarmManager)
            ACTION_AUTO_UPDATE -> {
                changeEmotion(context)
                Log.d(TAG, "Emotion changed automatically (timer triggered)")
            }
        }
    }

    /**
     * Change to the next emotion and update all widgets
     */
    private fun changeEmotion(context: Context) {
        // Get the current emotion index
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentIndex = prefs.getInt(PREF_EMOTION_INDEX, 0)

        // Calculate the next emotion index (cycle through the list)
        val nextIndex = (currentIndex + 1) % EMOTIONS.size

        // Save the new emotion index
        prefs.edit().putInt(PREF_EMOTION_INDEX, nextIndex).apply()

        // Update all widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, BlinkyWidgetProvider::class.java)
        )

        // Update all widgets with the new emotion
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        Log.d(TAG, "Changed emotion to: ${EMOTIONS[nextIndex].description}")
    }

    override fun onEnabled(context: Context) {
        // Initialize preferences when the first widget is created
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_EMOTION_INDEX, 0).apply()

        // Set up the alarm for automatic updates
        scheduleAutoUpdate(context)

        Log.d(TAG, "Blinky widget enabled and auto-update scheduled")
    }

    override fun onDisabled(context: Context) {
        // Clean up preferences when the last widget is disabled
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Cancel the alarm for automatic updates
        cancelAutoUpdate(context)

        Log.d(TAG, "Blinky widget disabled and auto-update canceled")
    }

    /**
     * Schedule automatic updates for the widget
     */
    private fun scheduleAutoUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BlinkyWidgetProvider::class.java)
        intent.action = ACTION_AUTO_UPDATE

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Schedule the first alarm to trigger after the update interval
        val triggerTime = SystemClock.elapsedRealtime() + UPDATE_INTERVAL_MS

        // Set a repeating alarm
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            triggerTime,
            UPDATE_INTERVAL_MS,
            pendingIntent
        )

        Log.d(TAG, "Scheduled auto-update every ${UPDATE_INTERVAL_MS / 1000} seconds")
    }

    /**
     * Cancel automatic updates for the widget
     */
    private fun cancelAutoUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BlinkyWidgetProvider::class.java)
        intent.action = ACTION_AUTO_UPDATE

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        Log.d(TAG, "Canceled auto-update")
    }

    /**
     * Update the widget with Blinky's eyes
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Get the custom layout for the widget
        val views = RemoteViews(context.packageName, R.layout.blinky_widget)

        // Get the current emotion from preferences
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val emotionIndex = prefs.getInt(PREF_EMOTION_INDEX, 0)
        val emotion = EMOTIONS[emotionIndex]

        // Set the text for the left and right eyes
        views.setTextViewText(R.id.leftEyeText, emotion.leftEye)
        views.setTextViewText(R.id.rightEyeText, emotion.rightEye)

        // Create an Intent to open the app when the widget is clicked
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Set the click listener for the widget
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views)

        Log.d(TAG, "Widget updated with emotion: ${emotion.description}")
    }
}
