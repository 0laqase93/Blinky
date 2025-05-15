package dam.tfg.blinky.data.model.auth

data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null
)