package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API service interface for event-related operations.
 */
interface EventApiService {
    /**
     * Creates a new event.
     *
     * @param eventDTO The event data to create
     * @return A Call object with an EventResponseDTO containing success status and event details
     */
    @POST("/api/events")
    fun createEvent(@Body eventDTO: EventDTO): Call<EventResponseDTO>

    /**
     * Updates an existing event.
     *
     * @param eventId The ID of the event to update
     * @param eventDTO The updated event data
     * @return A Call object with an EventResponseDTO containing success status and event details
     */
    @PUT("/api/events/{eventId}")
    fun updateEvent(@Path("eventId") eventId: Long, @Body eventDTO: EventDTO): Call<EventResponseDTO>

    /**
     * Fetches all events for a specific user.
     *
     * @param userId The ID of the user whose events to fetch
     * @return A Call object with a List of EventDTO containing the user's events
     */
    @GET("/api/events/user/{userId}")
    fun getUserEvents(@Path("userId") userId: Long): Call<List<EventDTO>>

    /**
     * Deletes an existing event.
     *
     * @param eventId The ID of the event to delete
     * @return A Call object with an EventResponseDTO containing success status and message
     */
    @DELETE("/api/events/{eventId}")
    fun deleteEvent(@Path("eventId") eventId: Long): Call<EventResponseDTO>
}
