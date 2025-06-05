package dam.tfg.blinky.domain.usecase

import dam.tfg.blinky.domain.repository.UserRepository

/**
 * Use case for verifying the user password.
 */
class VerifyPasswordUseCase(private val userRepository: UserRepository) {
    
    /**
     * Execute the use case to verify the user password.
     * @param password The password to verify.
     * @return True if the password is correct, false otherwise.
     */
    suspend operator fun invoke(password: String): Boolean {
        return userRepository.verifyPassword(password)
    }
}