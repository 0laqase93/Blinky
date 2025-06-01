package dam.tfg.blinky.domain.repository

import dam.tfg.blinky.domain.model.CalendarEvent
import retrofit2.Call
import dam.tfg.blinky.dataclass.EventResponseDTO
import dam.tfg.blinky.dataclass.EventDTO

/**
 * Repository interface for event-related operations
 */
interface EventRepository {
    /**
     * Get all events for the current user
     * @return A Call object with a list of EventDTO
     */
    fun getUserEvents(): Call<List<EventDTO>>
    
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
    fun createEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?
    ): Call<EventResponseDTO>
    
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
    fun updateEvent(
        eventId: Long,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?
    ): Call<EventResponseDTO>
    
    /**
     * Delete an event
     * @param eventId The ID of the event to delete
     * @return A Call object with the deleted event response
     */
    fun deleteEvent(eventId: Long): Call<EventResponseDTO>
}