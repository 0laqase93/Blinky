package dam.tfg.blinky.dataclass

/**
 * Data class for registration request.
 * 
 * @property email The user's email address (must be a valid email)
 * @property password The user's password (must be at least 6 characters)
 * @property username The user's username
 */
data class RegisterDTO(
    val email: String,
    val password: String,
    val username: String
)