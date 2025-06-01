package dam.tfg.blinky.domain.model

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Domain model representing a calendar event
 */
data class CalendarEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val apiId: Long? = null,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val endTime: LocalTime = time.plusHours(1),
    val description: String = "",
    val location: String = "",
    val notificationTime: LocalTime? = null // Time before the event to show notification
)