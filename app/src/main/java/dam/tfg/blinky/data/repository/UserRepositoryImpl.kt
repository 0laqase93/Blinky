package dam.tfg.blinky.data.repository

import android.content.Context
import android.util.Log
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
        // Clear all shared preferences except theme preferences
        tokenManager.clearToken()
        userManager.clearUserData()
        // Removed themeManager.clearPreferences() to preserve theme settings after logout

        // Clear any other app state as needed

        return@withContext true
    }

    override suspend fun validateToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if token exists
            if (!tokenManager.hasToken()) {
                Log.d("UserRepository", "No token found")
                return@withContext false
            }

            // Get user ID
            val userId = userManager.userId.value
            if (userId == -1L) {
                Log.d("UserRepository", "Invalid user ID")
                return@withContext false
            }

            // Make a request to validate the token
            val response = RetrofitClient.eventApi.getUserEvents(userId).execute()

            // Check if the request was successful
            val isValid = response.isSuccessful
            Log.d("UserRepository", "Token validation result: $isValid (code: ${response.code()})")

            return@withContext isValid
        } catch (e: Exception) {
            Log.e("UserRepository", "Error validating token", e)
            return@withContext false
        }
    }
}
