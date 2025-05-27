package dam.tfg.blinky.dataclass

/**
 * Data class for event creation response.
 * 
 * This class represents the response from the server when creating a new event.
 * The server returns a JSON object instead of a simple boolean.
 */
data class EventResponseDTO(
    val success: Boolean,
    val message: String? = null,
    val eventId: Long? = null
)