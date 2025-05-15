package dam.tfg.blinky.dataclass

data class LoginResponse(
    val token: String,
    val user: LoginUser
)
