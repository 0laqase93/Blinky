package dam.tfg.blinky.api

import dam.tfg.blinky.dataclass.LoginRequest
import dam.tfg.blinky.dataclass.LoginResponse
import dam.tfg.blinky.dataclass.RegisterDTO
import dam.tfg.blinky.dataclass.ResetPasswordDTO
import dam.tfg.blinky.dataclass.VerifyPasswordDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/auth/register")
    fun register(@Body request: RegisterDTO): Call<LoginResponse>

    @POST("/api/auth/verify-password")
    fun verifyPassword(@Body request: VerifyPasswordDTO): Call<Boolean>

    @POST("/api/auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordDTO): Call<Boolean>
}
