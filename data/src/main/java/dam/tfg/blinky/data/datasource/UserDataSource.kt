package dam.tfg.blinky.data.datasource

import dam.tfg.blinky.data.dto.UserDTO

/**
 * Interface for user data sources (remote, local, etc.).
 */
interface UserDataSource {
    suspend fun getUserProfile(): UserDTO
    suspend fun updateUserProfile(userDTO: UserDTO): Boolean
    suspend fun updateUserName(name: String): Boolean
    suspend fun verifyPassword(password: String): Boolean
    suspend fun resetPassword(newPassword: String): Boolean
    suspend fun requestPasswordResetEmail(email: String): Boolean
    suspend fun logout(): Boolean
}