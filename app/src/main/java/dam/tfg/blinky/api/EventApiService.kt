package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

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
}
