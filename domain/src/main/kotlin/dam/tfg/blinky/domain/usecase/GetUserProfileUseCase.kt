package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.model.User
import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for retrieving the user profile.
 */
class GetUserProfileUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to get the user profile.
     * @return The user profile.
     */
    suspend operator fun invoke(): User {
        return userRepository.getUserProfile()
    }
}