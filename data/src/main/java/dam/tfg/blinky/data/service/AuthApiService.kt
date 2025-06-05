package dam.tfg.blinky.data.service

import dam.tfg.blinky.data.dto.LoginRequest
import dam.tfg.blinky.data.dto.ResetPasswordRequest
import dam.tfg.blinky.data.dto.VerifyPasswordRequest
import kotlinx.coroutines.Deferred

/**
 * Service interface for authentication-related API endpoints.
 */
interface AuthApiService {
    suspend fun login(request: LoginRequest): Boolean
    suspend fun verifyPassword(request: VerifyPasswordRequest): Boolean
    suspend fun resetPassword(request: ResetPasswordRequest): Boolean
    suspend fun requestPasswordReset(email: String): Boolean
    suspend fun logout(): Boolean
}