package dam.tfg.blinky.data.repository

import android.content.Context
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.data.model.auth.User
import dam.tfg.blinky.dataclass.ResetPasswordDTO
import dam.tfg.blinky.dataclass.VerifyPasswordDTO
import dam.tfg.blinky.domain.repository.UserRepository
import dam.tfg.blinky.utils.ThemeManager
import dam.tfg.blinky.utils.TokenManager
import dam.tfg.blinky.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await

class UserRepositoryImpl(private val context: Context) : UserRepository {
    private val tokenManager = TokenManager(context)
    private val userManager = UserManager.getInstance(context)
    private val themeManager = ThemeManager.getInstance(context)

    // Get user data from UserManager
    private fun getCurrentUser(): User {
        return User(
            id = 1,
            name = userManager.userName.value,
            email = userManager.userEmail.value,
            profilePictureUrl = null
        )
    }

    override suspend fun getUserProfile(): User = withContext(Dispatchers.IO) {
        // Get user data from UserManager
        return@withContext getCurrentUser()
    }

    override suspend fun updateUserProfile(user: User): Boolean = withContext(Dispatchers.IO) {
        // Update user data in UserManager
        userManager.saveEmail(user.email)
        userManager.saveName(user.name)
        return@withContext true
    }

    override suspend fun updateUserName(name: String): Boolean = withContext(Dispatchers.IO) {
        // Update user name in UserManager
        userManager.saveName(name)
        return@withContext true
    }

    override suspend fun verifyPassword(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = VerifyPasswordDTO(password)
            val response = RetrofitClient.authApi.verifyPassword(request).await()
            return@withContext response
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun resetPassword(newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = ResetPasswordDTO(newPassword)
            val response = RetrofitClient.authApi.resetPassword(request).await()
            return@withContext response
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun requestPasswordResetEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        // In a real app, this would make an API call to request password reset
        // For now, just simulate success
        return@withContext true
    }

    override suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        // Clear all shared preferences
        tokenManager.clearToken()
        userManager.clearUserData()
        themeManager.clearPreferences()

        // Clear any other app state as needed

        return@withContext true
    }
}
