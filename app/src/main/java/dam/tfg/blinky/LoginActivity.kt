package dam.tfg.blinky

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import dam.tfg.blinky.dataclass.LoginRequest
import dam.tfg.blinky.ui.theme.BlinkyTheme
import dam.tfg.blinky.utils.TokenManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize TokenManager
        tokenManager = TokenManager(this)

        // Initialize RetrofitClient
        RetrofitClient.initialize(this)

        // Check if user is already logged in
        if (tokenManager.hasToken()) {
            navigateToMainActivity()
            return
        }

        setContent {
            BlinkyTheme {
                LoginScreen(
                    onLoginAttempt = { email, password, setLoading ->
                        loginUser(email, password, setLoading)
                    }
                )
            }
        }
    }

    private fun loginUser(email: String, password: String, setLoading: (Boolean) -> Unit) {
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.authApi.login(loginRequest).enqueue(object : Callback<dam.tfg.blinky.dataclass.LoginResponse> {
            override fun onResponse(
                call: Call<dam.tfg.blinky.dataclass.LoginResponse>,
                response: Response<dam.tfg.blinky.dataclass.LoginResponse>
            ) {
                // Set loading to false regardless of response
                setLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        // Save token to SharedPreferences
                        tokenManager.saveToken(loginResponse.token)

                        // Navigate to MainActivity
                        navigateToMainActivity()
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
                                401 -> "Credenciales inválidas"
                                else -> "Error en el servidor: ${response.code()}"
                            }

                            // If there are specific errors, check for email or user existence issues
                            if (errorResponse.errors.isNotEmpty()) {
                                // Check for specific error messages about email or user existence
                                val emailError = errorResponse.errors.find { it.contains("email", ignoreCase = true) || it.contains("correo", ignoreCase = true) }
                                val userError = errorResponse.errors.find { it.contains("usuario", ignoreCase = true) || it.contains("user", ignoreCase = true) }

                                when {
                                    emailError != null -> {
                                        // Email-specific error
                                        errorMessage = emailError
                                    }
                                    userError != null -> {
                                        // User-specific error
                                        errorMessage = userError
                                    }
                                    else -> {
                                        // Other errors
                                        val specificErrors = errorResponse.errors.joinToString("\n")
                                        errorMessage = "$errorMessage\n$specificErrors"
                                    }
                                }
                            }

                            Log.e("Blinky", "Error response: $errorMessage")
                            Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                        } else {
                            // Fallback to default error handling
                            val errorMessage = when (response.code()) {
                                401 -> "Credenciales inválidas"
                                else -> "Error en el servidor: ${response.code()}"
                            }
                            Log.e("Blinky", errorMessage)
                            Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // If parsing fails, use default error handling
                        Log.e("Blinky", "Error parsing error response", e)
                        val errorMessage = when (response.code()) {
                            401 -> "Credenciales inválidas"
                            else -> "Error en el servidor: ${response.code()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<dam.tfg.blinky.dataclass.LoginResponse>, t: Throwable) {
                // Set loading to false on failure
                setLoading(false)

                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("Blinky", errorMsg, t)
                Toast.makeText(
                    this@LoginActivity,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginAttempt: (String, String, (Boolean) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Create a focus requester for the password field
    val passwordFocusRequester = remember { FocusRequester() }

    // Mueve el contexto aquí, dentro del cuerpo composable
    val context = androidx.compose.ui.platform.LocalContext.current

    // Function to attempt login
    val attemptLogin = {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            onLoginAttempt(email, password) { newLoadingState ->
                isLoading = newLoadingState
            }
        } else {
            // Mostrar mensaje de error basado en los campos vacíos
            when {
                email.isEmpty() && password.isEmpty() -> {
                    val errorMsg = "Por favor, introduce el correo y la contraseña"
                    Log.e("Blinky", errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    val errorMsg = "Por favor, introduce el correo electrónico"
                    Log.e("Blinky", errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    val errorMsg = "Por favor, introduce la contraseña"
                    Log.e("Blinky", errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create a scroll state to allow scrolling when keyboard is visible
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // This will push the content up when the keyboard appears
            .navigationBarsPadding() // This ensures content doesn't overlap with navigation bars
            .statusBarsPadding() // This ensures content doesn't overlap with status bar
            .verticalScroll(scrollState) // Allow scrolling when keyboard reduces available space
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // Changed from Center to Top
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add top padding to push content down when keyboard is not visible
        Spacer(modifier = Modifier.height(48.dp))
        // App Logo and Title
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Blinky Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Title
        Text(
            text = "Blinky",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Move focus to password field when Enter is pressed
                    passwordFocusRequester.requestFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Trigger login when Enter is pressed
                    attemptLogin()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = {
                attemptLogin()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar Sesión")
            }
        }
    }
}
