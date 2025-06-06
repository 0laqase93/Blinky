package dam.tfg.blinky.domain.repository

import dam.tfg.blinky.domain.model.User

/**
 * Repository interface for user-related operations.
 */
interface UserRepository {
    suspend fun getUserProfile(): User
    suspend fun updateUserProfile(user: User): Boolean
    suspend fun updateUserName(name: String): Boolean
    suspend fun verifyPassword(password: String): Boolean
    suspend fun resetPassword(newPassword: String): Boolean
    suspend fun requestPasswordResetEmail(email: String): Boolean
    suspend fun logout(): Boolean
}