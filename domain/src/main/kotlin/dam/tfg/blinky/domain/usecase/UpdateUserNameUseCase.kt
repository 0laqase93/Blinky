package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for updating the user name.
 */
class UpdateUserNameUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to update the user name.
     * @param name The new user name.
     * @return True if the update was successful, false otherwise.
     */
    suspend operator fun invoke(name: String): Boolean {
        return userRepository.updateUserName(name)
    }
}