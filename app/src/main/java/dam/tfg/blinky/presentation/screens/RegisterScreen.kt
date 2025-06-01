package dam.tfg.blinky.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import dam.tfg.blinky.R
import dam.tfg.blinky.dataclass.WrenchEmotion

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

    // State for avatar emotion
    var emotion by remember { mutableStateOf(WrenchEmotion.DEFAULT) }

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
                // If registration was successful (loading stopped and no errors)
                if (!newLoadingState && emailError.isEmpty() && usernameError.isEmpty() && passwordError.isEmpty() && generalError.isEmpty()) {
                    // Change emotion to HAPPY
                    emotion = WrenchEmotion.HAPPY
                }
            }, { errorMessage ->
                setErrorMessage(errorMessage)
                // Set emotion to ERROR if there was an error
                emotion = WrenchEmotion.ERROR
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
            // Set emotion to ERROR if there was an error
            emotion = WrenchEmotion.ERROR
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

        // Define custom font for the avatar eyes
        val avatarFont = FontFamily(Font(R.font.vt323regular))

        // Avatar eyes
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left eye with animation
                AnimatedContent(
                    targetState = emotion.leftEye,
                    transitionSpec = {
                        // Define animation for transition
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "LeftEyeAnimation"
                ) { targetEye ->
                    Text(
                        text = targetEye,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 120.sp,
                            fontFamily = avatarFont
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Space between eyes
                Spacer(modifier = Modifier.width(16.dp))

                // Right eye with animation
                AnimatedContent(
                    targetState = emotion.rightEye,
                    transitionSpec = {
                        // Define animation for transition
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "RightEyeAnimation"
                ) { targetEye ->
                    Text(
                        text = targetEye,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 120.sp,
                            fontFamily = avatarFont
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

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
            colors = ButtonDefaults.buttonColors(
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlue
            ),
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = dam.tfg.blinky.ui.theme.GoogleBlue
            )
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
