package dam.tfg.blinky.dataclass

/**
 * Data class representing a personality response from the API.
 */
data class PersonalityResponseDTO(
    val id: Long,
    val name: String,
    val basePrompt: String,
    val description: String
)