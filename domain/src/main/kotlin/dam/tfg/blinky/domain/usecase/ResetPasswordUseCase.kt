package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for resetting the user password.
 */
class ResetPasswordUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to reset the user password.
     * @param newPassword The new password.
     * @return True if the password reset was successful, false otherwise.
     */
    suspend operator fun invoke(newPassword: String): Boolean {
        return userRepository.resetPassword(newPassword)
    }
}