package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.LoginRequest
import dam.tfg.blinky.dataclass.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}