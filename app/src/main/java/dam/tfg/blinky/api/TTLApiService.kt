package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import dam.tfg.blinky.dataclass.EventDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TTLApiService {
    @POST("/api/llama/send_prompt")
    fun sendPrompt(@Body request: ChatDTO): Call<ChatResponse>

    @POST("/api/llama/create_event")
    fun createEvent(@Body request: ChatDTO): Call<EventDTO>
}
