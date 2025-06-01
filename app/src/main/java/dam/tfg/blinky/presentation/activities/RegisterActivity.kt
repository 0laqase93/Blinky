package dam.tfg.blinky.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.dataclass.ErrorResponse
import dam.tfg.blinky.dataclass.LoginResponse
import dam.tfg.blinky.dataclass.RegisterDTO
import dam.tfg.blinky.presentation.screens.RegisterScreen
import dam.tfg.blinky.ui.theme.BlinkyTheme
import dam.tfg.blinky.presentation.activities.MainActivity
import dam.tfg.blinky.utils.TokenManager
import dam.tfg.blinky.utils.UserManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : ComponentActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize TokenManager
        tokenManager = TokenManager(this)

        // Initialize UserManager
        userManager = UserManager.getInstance(this)

        // Initialize RetrofitClient
        RetrofitClient.initialize(this)

        setContent {
            BlinkyTheme {
                RegisterScreen(
                    onRegisterAttempt = { email, username, password, setLoading, setErrorMessage ->
                        registerUser(email, username, password, setLoading, setErrorMessage)
                    },
                    onBackToLogin = {
                        // Navigate back to login screen
                        finish()
                    }
                )
            }
        }
    }

    private fun registerUser(
        email: String,
        username: String,
        password: String,
        setLoading: (Boolean) -> Unit,
        setErrorMessage: (String) -> Unit
    ) {
        val registerRequest = RegisterDTO(email, password, username)

        RetrofitClient.authApi.register(registerRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                // Set loading to false regardless of response
                setLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        // Save token to SharedPreferences
                        tokenManager.saveToken(loginResponse.token)

                        // Save user data from login response
                        loginResponse.user.let { user ->
                            // Save complete user data including token, id, email, admin status, and username
                            userManager.saveCompleteUserData(
                                token = loginResponse.token,
                                userId = user.id,
                                email = user.email,
                                username = user.username,
                                isAdmin = user.admin,
                                password = user.password ?: ""
                            )

                            // Add a delay to appreciate the HAPPY emotion before navigating
                            Handler(Looper.getMainLooper()).postDelayed({
                                navigateToMainActivity()
                            }, 500) // 0.5 seconds delay
                        }
                    }
                } else {
                    // Handle error
                    try {
                        // Try to parse the error response
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
                            val errorResponse = objectMapper.readValue(errorBody, ErrorResponse::class.java)

                            // Get the main error message or use a default one
                            var errorMessage = errorResponse.message ?: when (response.code()) {
                                409 -> "El usuario ya existe"
                                else -> "Error en el servidor: ${response.code()}"
                            }

                            // If there are specific errors, check for email or user existence issues
                            if (errorResponse.errors.isNotEmpty()) {
                                // Check for specific error messages about email or user existence
                                val emailError = errorResponse.errors.find { it.contains("email", ignoreCase = true) || it.contains("correo", ignoreCase = true) }
                                val userError = errorResponse.errors.find { it.contains("usuario", ignoreCase = true) || it.contains("user", ignoreCase = true) }
                                val passwordError = errorResponse.errors.find { it.contains("contraseña", ignoreCase = true) || it.contains("password", ignoreCase = true) }

                                when {
                                    emailError != null -> {
                                        // Email-specific error
                                        errorMessage = emailError
                                    }
                                    userError != null -> {
                                        // User-specific error
                                        errorMessage = userError
                                    }
                                    passwordError != null -> {
                                        // Password-specific error
                                        errorMessage = passwordError
                                    }
                                    else -> {
                                        // Other errors
                                        val specificErrors = errorResponse.errors.joinToString("\n")
                                        errorMessage = "$errorMessage\n$specificErrors"
                                    }
                                }
                            }

                            Log.e("Blinky", "Error response: $errorMessage")
                            setErrorMessage(errorMessage)
                        } else {
                            // Fallback to default error handling
                            val errorMessage = when (response.code()) {
                                409 -> "El usuario ya existe"
                                else -> "Error en el servidor: ${response.code()}"
                            }
                            Log.e("Blinky", errorMessage)
                            setErrorMessage(errorMessage)
                        }
                    } catch (e: Exception) {
                        // If parsing fails, use default error handling
                        Log.e("Blinky", "Error parsing error response", e)
                        val errorMessage = when (response.code()) {
                            409 -> "El usuario ya existe"
                            else -> "Error en el servidor: ${response.code()}"
                        }
                        setErrorMessage(errorMessage)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Set loading to false on failure
                setLoading(false)

                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("Blinky", errorMsg, t)
                setErrorMessage(errorMsg)
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close RegisterActivity
    }
}
