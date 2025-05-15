package dam.tfg.blinky.domain.repository

import dam.tfg.blinky.data.model.auth.User

interface UserRepository {
    suspend fun getUserProfile(): User
    suspend fun updateUserProfile(user: User): Boolean
    suspend fun updateUserName(name: String): Boolean
    suspend fun resetPassword(email: String): Boolean
    suspend fun logout(): Boolean
}