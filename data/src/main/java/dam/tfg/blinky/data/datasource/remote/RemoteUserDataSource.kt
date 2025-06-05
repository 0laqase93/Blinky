package dam.tfg.blinky.data.datasource.remote

import dam.tfg.blinky.data.datasource.UserDataSource
import dam.tfg.blinky.data.dto.UserDTO
import dam.tfg.blinky.data.dto.LoginRequest
import dam.tfg.blinky.data.dto.ResetPasswordRequest
import dam.tfg.blinky.data.dto.VerifyPasswordRequest
import dam.tfg.blinky.data.service.AuthApiService
import dam.tfg.blinky.data.service.UserApiService

/**
 * Implementation of UserDataSource that fetches data from a remote API.
 */
class RemoteUserDataSource(
    private val userApiService: UserApiService,
    private val authApiService: AuthApiService
) : UserDataSource {

    override suspend fun getUserProfile(): UserDTO {
        return userApiService.getUserProfile()
    }

    override suspend fun updateUserProfile(userDTO: UserDTO): Boolean {
        return userApiService.updateUserProfile(userDTO)
    }

    override suspend fun updateUserName(name: String): Boolean {
        return userApiService.updateUserName(name)
    }

    override suspend fun verifyPassword(password: String): Boolean {
        val request = VerifyPasswordRequest(password)
        return authApiService.verifyPassword(request)
    }

    override suspend fun resetPassword(newPassword: String): Boolean {
        val request = ResetPasswordRequest(newPassword)
        return authApiService.resetPassword(request)
    }

    override suspend fun requestPasswordResetEmail(email: String): Boolean {
        return authApiService.requestPasswordReset(email)
    }

    override suspend fun logout(): Boolean {
        return authApiService.logout()
    }
}