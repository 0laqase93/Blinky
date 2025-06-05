package dam.tfg.blinky.data.dto

/**
 * Data Transfer Object for User data from the API.
 */
data class UserDTO(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null
)