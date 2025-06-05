package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.model.User
import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for updating the user profile.
 */
class UpdateUserProfileUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to update the user profile.
     * @param user The updated user profile.
     * @return True if the update was successful, false otherwise.
     */
    suspend operator fun invoke(user: User): Boolean {
        return userRepository.updateUserProfile(user)
    }
}