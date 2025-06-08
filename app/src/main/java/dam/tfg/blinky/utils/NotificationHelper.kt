package dam.tfg.blinky.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dam.tfg.blinky.domain.model.CalendarEvent
import dam.tfg.blinky.presentation.activities.MainActivity
import dam.tfg.blinky.receivers.NotificationReceiver
import java.util.UUID
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

/**
 * Helper class for managing notifications for calendar events
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "event_notifications"
        private const val CHANNEL_NAME = "Event Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for calendar events"
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Create the notification channel for Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule a notification for an event
     * @param event The calendar event to schedule a notification for
     */
    fun scheduleNotification(event: CalendarEvent) {
        // If notification time is null, don't schedule a notification
        if (event.notificationTime == null) {
            Log.d(TAG, "No notification time set for event: ${event.title}")
            return
        }

        try {
            // Calculate notification time
            val eventDateTime = LocalDateTime.of(event.date, event.time)

            // Calculate how many hours and minutes before the event to show the notification
            val notificationHours = event.notificationTime.hour
            val notificationMinutes = event.notificationTime.minute

            // Calculate the actual time to show the notification
            val notificationDateTime = eventDateTime.minusHours(notificationHours.toLong())
                .minusMinutes(notificationMinutes.toLong())

            // Convert to milliseconds
            val notificationTimeMillis = notificationDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Create intent for the notification receiver
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("eventTitle", event.title)
            intent.putExtra("eventDescription", event.description)
            intent.putExtra("eventLocation", event.location)
            intent.putExtra("eventTime", "${event.time}")

            // Create unique request code based on event id
            val requestCode = event.id.hashCode()

            // Create pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get alarm manager
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Schedule the notification
            try {
                // Check if we can schedule exact alarms on Android S and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
                    } else {
                        Log.w(TAG, "Cannot schedule exact alarms. Using inexact alarm instead.")
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }
                } else {
                    // For Android R and below, we can schedule exact alarms
                    scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException when scheduling alarm", e)
                // Fall back to inexact alarm
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Notification scheduled for event: ${event.title} at $notificationDateTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification for event: ${event.title}", e)
        }
    }

    /**
     * Schedule an exact alarm using the appropriate method based on Android version
     */
    private fun scheduleExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancel a scheduled notification for an event
     * @param event The calendar event to cancel the notification for
     */
    fun cancelNotification(event: CalendarEvent) {
        try {
            // Create intent for the notification receiver
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("eventTitle", event.title)

            // Create unique request code based on event id
            val requestCode = event.id.hashCode()

            // Create pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get alarm manager
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Cancel the alarm
            alarmManager.cancel(pendingIntent)

            Log.d(TAG, "Notification cancelled for event: ${event.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification for event: ${event.title}", e)
        }
    }

    /**
     * Send a test notification immediately
     */
    fun sendTestNotification() {
        sendTestNotification(null)
    }

    /**
     * Send a test notification with a delay
     * @param notificationTime The time to wait before showing the notification (null for immediate)
     */
    fun sendTestNotification(notificationTime: org.threeten.bp.LocalTime?) {
        try {
            // Create a unique ID for the test notification
            val testNotificationId = "test_notification_${System.currentTimeMillis()}"

            // Create intent for the notification receiver
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("eventId", testNotificationId)
            intent.putExtra("eventTitle", "Notificación de prueba")
            intent.putExtra("eventDescription", "Esta es una notificación de prueba para verificar que las notificaciones funcionan correctamente.")
            intent.putExtra("eventLocation", "Aplicación Blinky")

            // If notification time is null, send immediately
            if (notificationTime == null) {
                intent.putExtra("eventTime", "Ahora")

                // Send the broadcast directly to show notification immediately
                try {
                    context.sendBroadcast(intent)
                    Log.d(TAG, "Test notification sent immediately")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending immediate test notification", e)
                }
            } else {
                // Schedule the notification for the specified time
                intent.putExtra("eventTime", "Después de ${notificationTime.hour}h ${notificationTime.minute}m")

                // Create unique request code based on notification ID
                val requestCode = testNotificationId.hashCode()

                // Create pending intent
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Calculate notification time (current time + delay)
                val now = org.threeten.bp.LocalDateTime.now()
                val notificationDateTime = now.plusHours(notificationTime.hour.toLong())
                    .plusMinutes(notificationTime.minute.toLong())

                // Convert to milliseconds
                val notificationTimeMillis = notificationDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Get alarm manager
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Schedule the notification
                try {
                    // Check if we can schedule exact alarms on Android S and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
                        } else {
                            Log.w(TAG, "Cannot schedule exact alarms. Using inexact alarm instead.")
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                notificationTimeMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        // For Android R and below, we can schedule exact alarms
                        scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
                    }
                    Log.d(TAG, "Test notification scheduled for: $notificationDateTime")
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException when scheduling test notification", e)
                    // Fall back to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        notificationTimeMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating test notification", e)
        }
    }
}
