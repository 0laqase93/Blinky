package dam.tfg.blinky.dataclass

data class LoginUser(
    val id: Long,
    val email: String,
    val password: String?,
    val username: String,
    val admin: Boolean
)