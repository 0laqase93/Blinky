package dam.tfg.blinky.data.service

import dam.tfg.blinky.data.dto.UserDTO

/**
 * Service interface for user-related API endpoints.
 */
interface UserApiService {
    suspend fun getUserProfile(): UserDTO
    suspend fun updateUserProfile(userDTO: UserDTO): Boolean
    suspend fun updateUserName(name: String): Boolean
}