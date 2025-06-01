package dam.tfg.blinky.dataclass

import org.threeten.bp.LocalDateTime

/**
 * Data class for event creation request and response.
 * 
 * @property id The event ID (only present in responses from the API)
 * @property title The event title (required, max 255 characters)
 * @property startTime The event start date and time (required)
 * @property endTime The event end date and time (required)
 * @property location The event location (optional, max 255 characters)
 * @property description The event description (optional, max 1000 characters)
 * @property userId The ID of the user who created the event (required)
 */
data class EventDTO(
    val id: Long? = null,

    val title: String,

    val startTime: LocalDateTime?,

    val endTime: LocalDateTime?,

    val location: String? = null,

    val description: String? = null,

    val userId: Long
)
