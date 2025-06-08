package dam.tfg.blinky.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import dam.tfg.blinky.R
import dam.tfg.blinky.presentation.activities.MainActivity

/**
 * BroadcastReceiver for handling event notifications
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "event_notifications"
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            // Extract event details from intent
            val eventId = intent.getStringExtra("eventId") ?: ""
            val eventTitle = intent.getStringExtra("eventTitle") ?: "Evento"
            val eventDescription = intent.getStringExtra("eventDescription") ?: ""
            val eventLocation = intent.getStringExtra("eventLocation") ?: ""
            val eventTime = intent.getStringExtra("eventTime") ?: ""

            // Create intent to open the app when notification is tapped
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Create pending intent
            val pendingIntent = PendingIntent.getActivity(
                context,
                eventId.hashCode(),
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon
                .setContentTitle(eventTitle)
                .setContentText("$eventTime - $eventLocation")
                .setStyle(NotificationCompat.BigTextStyle().bigText("$eventDescription\n$eventTime - $eventLocation"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            // Show notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(eventId.hashCode(), notificationBuilder.build())

            Log.d(TAG, "Notification displayed for event: $eventTitle")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification", e)
        }
    }
}
