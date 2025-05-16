package dam.tfg.blinky

import android.content.Intent
import android.os.Bundle
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
import dam.tfg.blinky.ui.theme.BlinkyTheme
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
                            userManager.saveUserData(user.email, user.password ?: "", user.username)

                            // Navigate to MainActivity
                            navigateToMainActivity()
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

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterAttempt: (String, String, String, (Boolean) -> Unit, (String) -> Unit) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Error state variables
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var generalError by remember { mutableStateOf("") }

    // Create focus requesters for the fields
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Function to clear errors when user starts typing
    val clearErrors = {
        emailError = ""
        usernameError = ""
        passwordError = ""
        generalError = ""
    }

    // Function to set error message based on the content
    val setErrorMessage = { message: String ->
        when {
            message.contains("email", ignoreCase = true) || message.contains("correo", ignoreCase = true) -> {
                emailError = message
            }
            message.contains("usuario", ignoreCase = true) || message.contains("username", ignoreCase = true) -> {
                usernameError = message
            }
            message.contains("contraseña", ignoreCase = true) || message.contains("password", ignoreCase = true) -> {
                passwordError = message
            }
            else -> {
                generalError = message
            }
        }
    }

    // Function to attempt registration
    val attemptRegister = {
        // Clear previous errors
        clearErrors()

        if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            onRegisterAttempt(email, username, password, { newLoadingState ->
                isLoading = newLoadingState
            }, { errorMessage ->
                setErrorMessage(errorMessage)
            })
        } else {
            // Set error messages based on empty fields
            when {
                email.isEmpty() && username.isEmpty() && password.isEmpty() -> {
                    generalError = "Por favor, completa todos los campos"
                    Log.e("Blinky", generalError)
                }
                email.isEmpty() -> {
                    emailError = "Por favor, introduce el correo electrónico"
                    Log.e("Blinky", emailError)
                }
                username.isEmpty() -> {
                    usernameError = "Por favor, introduce el nombre de usuario"
                    Log.e("Blinky", usernameError)
                }
                password.isEmpty() -> {
                    passwordError = "Por favor, introduce la contraseña"
                    Log.e("Blinky", passwordError)
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
        verticalArrangement = Arrangement.Top,
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
            text = "Registro en Blinky",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // General error message
        if (generalError.isNotEmpty()) {
            RegisterErrorMessage(generalError)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                // Clear email error when user starts typing
                emailError = ""
                generalError = ""
            },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Move focus to username field when Next is pressed
                    usernameFocusRequester.requestFocus()
                }
            ),
            isError = emailError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        // Email error message
        if (emailError.isNotEmpty()) {
            RegisterErrorMessage(emailError)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                // Clear username error when user starts typing
                usernameError = ""
                generalError = ""
            },
            label = { Text("Nombre de usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Move focus to password field when Next is pressed
                    passwordFocusRequester.requestFocus()
                }
            ),
            isError = usernameError.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(usernameFocusRequester)
        )

        // Username error message
        if (usernameError.isNotEmpty()) {
            RegisterErrorMessage(usernameError)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                // Clear password error when user starts typing
                passwordError = ""
                generalError = ""
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Trigger registration when Done is pressed
                    attemptRegister()
                }
            ),
            isError = passwordError.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
        )

        // Password error message
        if (passwordError.isNotEmpty()) {
            RegisterErrorMessage(passwordError)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Register Button
        Button(
            onClick = {
                attemptRegister()
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
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login Button
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a Iniciar Sesión")
        }
    }
}

@Composable
fun RegisterErrorMessage(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
