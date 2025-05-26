package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import dam.tfg.blinky.dataclass.PersonalityResponseDTO
import dam.tfg.blinky.dataclass.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {
    @POST("/api/llama/send_prompt")
    fun sendPrompt(@Body request: ChatDTO): Call<ChatResponse>

    @GET("/api/user/email/{email}")
    fun getUserByEmail(@Path("email") email: String): Call<UserResponse>

    @GET("/api/personalities")
    fun getPersonalities(): Call<List<PersonalityResponseDTO>>
}
