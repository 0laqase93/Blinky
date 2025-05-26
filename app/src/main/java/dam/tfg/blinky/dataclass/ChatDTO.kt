package dam.tfg.blinky.dataclass

data class ChatDTO(
    val prompt: String,
    val userId: Long,
    val personalityId: Long? = null
)
