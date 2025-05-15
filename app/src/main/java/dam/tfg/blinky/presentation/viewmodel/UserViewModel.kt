package dam.tfg.blinky.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dam.tfg.blinky.data.model.auth.User
import dam.tfg.blinky.data.repository.UserRepositoryImpl
import dam.tfg.blinky.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    // User profile state
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    // Loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Error state
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    // Success message state
    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = userRepository.getUserProfile()
                _userState.value = user
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val success = userRepository.updateUserName(name)
                if (success) {
                    _userState.value = _userState.value?.copy(name = name)
                    _successMessage.value = "Name updated successfully"
                } else {
                    _error.value = "Failed to update name"
                }
            } catch (e: Exception) {
                _error.value = "Error updating name: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfilePicture(pictureUrl: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val currentUser = _userState.value ?: return@launch
                val updatedUser = currentUser.copy(profilePictureUrl = pictureUrl)
                val success = userRepository.updateUserProfile(updatedUser)
                if (success) {
                    _userState.value = updatedUser
                    _successMessage.value = "Profile picture updated successfully"
                } else {
                    _error.value = "Failed to update profile picture"
                }
            } catch (e: Exception) {
                _error.value = "Error updating profile picture: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val email = _userState.value?.email ?: return@launch
                val success = userRepository.resetPassword(email)
                if (success) {
                    _successMessage.value = "Password reset email sent to $email"
                } else {
                    _error.value = "Failed to send password reset email"
                }
            } catch (e: Exception) {
                _error.value = "Error resetting password: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(): Boolean {
        // Perform logout synchronously to ensure it completes before returning
        try {
            _isLoading.value = true
            // This is a blocking call, but it's necessary to ensure logout completes
            val result = kotlinx.coroutines.runBlocking {
                try {
                    userRepository.logout()
                } catch (e: Exception) {
                    _error.value = "Error logging out: ${e.message}"
                    false
                }
            }
            return result
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    // Factory for creating the ViewModel with dependencies
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                val repository = UserRepositoryImpl(context)
                return UserViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
