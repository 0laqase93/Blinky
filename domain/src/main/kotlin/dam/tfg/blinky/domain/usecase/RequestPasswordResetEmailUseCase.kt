package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for requesting a password reset email.
 */
class RequestPasswordResetEmailUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to request a password reset email.
     * @param email The email address to send the reset link to.
     * @return True if the request was successful, false otherwise.
     */
    suspend operator fun invoke(email: String): Boolean {
        return userRepository.requestPasswordResetEmail(email)
    }
}