package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for logging out the user.
 */
class LogoutUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to log out the user.
     * @return True if the logout was successful, false otherwise.
     */
    suspend operator fun invoke(): Boolean {
        return userRepository.logout()
    }
}