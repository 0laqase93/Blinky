package dam.tfg.blinky.data.dto

/**
 * Data Transfer Object for login requests.
 */
data class LoginRequest(
    val email: String,
    val password: String
)