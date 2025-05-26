package dam.tfg.blinky.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.Activity
import android.net.Uri
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.TimeZone

/**
 * Utility class for Google Calendar integration
 */
class CalendarUtils {
    companion object {
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 100
        private const val GOOGLE_CALENDAR_PACKAGE = "com.google.android.calendar"

        /**
         * Checks if Google Calendar is installed on the device
         *
         * @param context The application context
         * @return True if Google Calendar is installed, false otherwise
         */
        fun isGoogleCalendarInstalled(context: Context): Boolean {
            return try {
                context.packageManager.getPackageInfo(GOOGLE_CALENDAR_PACKAGE, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        /**
         * Checks if the app has calendar permissions
         *
         * @param context The application context
         * @return True if the app has calendar permissions, false otherwise
         */
        fun hasCalendarPermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Requests calendar permissions
         *
         * @param activity The activity requesting permissions
         */
        fun requestCalendarPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                CALENDAR_PERMISSION_REQUEST_CODE
            )
        }

        /**
         * Creates an intent to add an event to Google Calendar
         *
         * @param context The application context
         * @param title The event title
         * @param startDate The event start date
         * @param startTime The event start time
         * @param endDate The event end date (defaults to startDate)
         * @param endTime The event end time (defaults to startTime + 1 hour)
         * @param description The event description (optional)
         * @param location The event location (optional)
         * @return An intent to add the event to Google Calendar
         */
        fun createAddToCalendarIntent(
            context: Context,
            title: String,
            startDate: LocalDate,
            startTime: LocalTime,
            endDate: LocalDate = startDate,
            endTime: LocalTime = startTime.plusHours(1),
            description: String = "",
            location: String = ""
        ): Intent {
            // Convert LocalDate and LocalTime to milliseconds since epoch
            val startMillis = startDate.atTime(startTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val endMillis = endDate.atTime(endTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            return Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                putExtra(CalendarContract.Events.ALL_DAY, false)

                // Set the time zone
                putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

                // Specify Google Calendar package
                if (isGoogleCalendarInstalled(context)) {
                    setPackage(GOOGLE_CALENDAR_PACKAGE)
                }
            }
        }

        /**
         * Shows a dialog asking if the user wants to install Google Calendar
         * and opens the Play Store if confirmed
         *
         * @param context The application context
         */
        fun showInstallGoogleCalendarDialog(context: Context) {
            // Mostrar un diálogo preguntando si quiere instalar Google Calendar
            // Al confirmar, abrir Play Store en la página de Google Calendar
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$GOOGLE_CALENDAR_PACKAGE")
            }

            try {
                context.startActivity(playStoreIntent)
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Opening Play Store to install Google Calendar")
            } catch (e: Exception) {
                android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Failed to open Play Store: ${e.message}")
                // If Play Store is not installed, open in browser
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=$GOOGLE_CALENDAR_PACKAGE")
                    }
                    context.startActivity(browserIntent)
                    android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Opening browser to install Google Calendar")
                } catch (e2: Exception) {
                    android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Failed to open browser: ${e2.message}")
                }
            }
        }

        /**
         * Exports an event to Google Calendar
         *
         * @param activity The activity context
         * @param title The event title
         * @param startDate The event start date
         * @param startTime The event start time
         * @param endDate The event end date (defaults to startDate)
         * @param endTime The event end time (defaults to startTime + 1 hour)
         * @param description The event description (optional)
         * @param location The event location (optional)
         * @return True if the export was initiated, false otherwise
         */
        fun exportToGoogleCalendar(
            activity: Activity,
            title: String,
            startDate: LocalDate,
            startTime: LocalTime,
            endDate: LocalDate = startDate,
            endTime: LocalTime = startTime.plusHours(1),
            description: String = "",
            location: String = ""
        ): Boolean {
            try {
                // Log event details for debugging
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Attempting to export event to Google Calendar:")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] - Title: $title")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] - Start: $startDate $startTime")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] - End: $endDate $endTime")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] - Description: $description")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] - Location: $location")

                // Validate required fields
                if (title.isBlank()) {
                    android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Export failed: Title is blank")
                    Toast.makeText(activity, "Error: El título del evento no puede estar vacío", Toast.LENGTH_LONG).show()
                    return false
                }

                // Check if Google Calendar is installed
                if (!isGoogleCalendarInstalled(activity)) {
                    android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Export failed: Google Calendar not installed")
                    Toast.makeText(
                        activity,
                        "Google Calendar no está instalado en este dispositivo",
                        Toast.LENGTH_LONG
                    ).show()

                    // Show dialog to suggest installing Google Calendar
                    showInstallGoogleCalendarDialog(activity)
                    return false
                }

                // Check for calendar permissions
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) 
                    != PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Export failed: Missing WRITE_CALENDAR permission")
                    Toast.makeText(
                        activity,
                        "Se requieren permisos de calendario para esta acción",
                        Toast.LENGTH_LONG
                    ).show()
                    requestCalendarPermissions(activity)
                    return false
                }

                // Create the intent
                val intent = createAddToCalendarIntent(
                    activity, title, startDate, startTime, endDate, endTime, description, location
                )

                // Debug logs as specified in the issue description
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Intent URI: ${intent.data}")
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Package manager can resolve: ${intent.resolveActivity(activity.packageManager) != null}")

                // Verify that the intent can be resolved
                if (intent.resolveActivity(activity.packageManager) == null) {
                    android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Export failed: No activity found to handle intent")
                    Toast.makeText(
                        activity,
                        "No se encontró una aplicación para manejar eventos de calendario",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }

                // Start the activity
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Starting activity with calendar intent")
                activity.startActivity(intent)
                android.util.Log.d("CalendarUtils", "[DEBUG_LOG] Calendar intent started successfully")
                return true
            } catch (e: Exception) {
                android.util.Log.e("CalendarUtils", "[DEBUG_LOG] Export failed with exception: ${e.message}")
                e.printStackTrace()
                Toast.makeText(
                    activity,
                    "Error al abrir Google Calendar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }
    }
}
