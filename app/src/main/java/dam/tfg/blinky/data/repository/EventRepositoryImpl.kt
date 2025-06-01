package dam.tfg.blinky.data.repository

import dam.tfg.blinky.api.EventApiService
import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import dam.tfg.blinky.domain.repository.EventRepository
import dam.tfg.blinky.utils.UserManager
import retrofit2.Call
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Implementation of the EventRepository interface
 */
class EventRepositoryImpl(
    private val eventApiService: EventApiService,
    private val userManager: UserManager
) : EventRepository {

    /**
     * Get all events for the current user
     * @return A Call object with a list of EventDTO
     */
    override fun getUserEvents(): Call<List<EventDTO>> {
        val userId = userManager.userId.value
        if (userId <= 0) {
            throw IllegalStateException("User ID not found")
        }
        return eventApiService.getUserEvents(userId)
    }

    /**
     * Create a new event
     * @param title The title of the event
     * @param date The date of the event in ISO format (yyyy-MM-dd)
     * @param startTime The start time of the event in ISO format (HH:mm)
     * @param endTime The end time of the event in ISO format (HH:mm)
     * @param description The description of the event (optional)
     * @param location The location of the event (optional)
     * @return A Call object with the created event response
     */
    override fun createEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?
    ): Call<EventResponseDTO> {
        val userId = userManager.userId.value
        if (userId <= 0) {
            throw IllegalStateException("User ID not found")
        }

        val startDateTime = LocalDateTime.of(
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
            LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME)
        )

        val endDateTime = LocalDateTime.of(
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
            LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME)
        )

        val eventDTO = EventDTO(
            title = title,
            startTime = startDateTime,
            endTime = endDateTime,
            description = description,
            location = location,
            userId = userId
        )

        return eventApiService.createEvent(eventDTO)
    }

    /**
     * Update an existing event
     * @param eventId The ID of the event to update
     * @param title The new title of the event
     * @param date The new date of the event in ISO format (yyyy-MM-dd)
     * @param startTime The new start time of the event in ISO format (HH:mm)
     * @param endTime The new end time of the event in ISO format (HH:mm)
     * @param description The new description of the event (optional)
     * @param location The new location of the event (optional)
     * @return A Call object with the updated event response
     */
    override fun updateEvent(
        eventId: Long,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?
    ): Call<EventResponseDTO> {
        val userId = userManager.userId.value
        if (userId <= 0) {
            throw IllegalStateException("User ID not found")
        }

        val startDateTime = LocalDateTime.of(
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
            LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME)
        )

        val endDateTime = LocalDateTime.of(
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
            LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME)
        )

        val eventDTO = EventDTO(
            id = eventId,
            title = title,
            startTime = startDateTime,
            endTime = endDateTime,
            description = description,
            location = location,
            userId = userId
        )

        return eventApiService.updateEvent(eventId, eventDTO)
    }

    /**
     * Delete an event
     * @param eventId The ID of the event to delete
     * @return A Call object with the deleted event response
     */
    override fun deleteEvent(eventId: Long): Call<EventResponseDTO> {
        return eventApiService.deleteEvent(eventId)
    }
}
