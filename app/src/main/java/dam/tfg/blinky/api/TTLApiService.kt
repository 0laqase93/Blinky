package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TTLApiService {
    @POST("/api/llama/send_prompt")
    fun sendPrompt(@Body request: ChatDTO): Call<ChatResponse>
}
