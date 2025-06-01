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
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginAttempt: (String, String, (Boolean) -> Unit, (String) -> Unit) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // State for avatar emotion
    var emotion by remember { mutableStateOf(WrenchEmotion.DEFAULT) }

    // Error state variables
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var generalError by remember { mutableStateOf("") }

    // Create a focus requester for the password field
    val passwordFocusRequester = remember { FocusRequester() }

    // Function to clear errors when user starts typing
    val clearErrors = {
        emailError = ""
        passwordError = ""
        generalError = ""
    }

    // Function to set error message based on the content
    val setErrorMessage = { message: String ->
        when {
            message.contains("email", ignoreCase = true) || message.contains("correo", ignoreCase = true) -> {
                emailError = message
            }
            message.contains("contraseña", ignoreCase = true) || message.contains("password", ignoreCase = true) -> {
                passwordError = message
            }
            else -> {
                generalError = message
            }
        }
    }

    // Function to attempt login
    val attemptLogin = {
        // Clear previous errors
        clearErrors()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            onLoginAttempt(email, password, { newLoadingState ->
                isLoading = newLoadingState
                // If login was successful (loading stopped and no errors)
                if (!newLoadingState && emailError.isEmpty() && passwordError.isEmpty() && generalError.isEmpty()) {
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
                email.isEmpty() && password.isEmpty() -> {
                    generalError = "Por favor, introduce el correo y la contraseña"
                    Log.e("Blinky", generalError)
                }
                email.isEmpty() -> {
                    emailError = "Por favor, introduce el correo electrónico"
                    Log.e("Blinky", emailError)
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
        verticalArrangement = Arrangement.Top, // Changed from Center to Top
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
            text = "Blinky",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // General error message
        if (generalError.isNotEmpty()) {
            ErrorMessage(generalError)
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Move focus to password field when Enter is pressed
                    passwordFocusRequester.requestFocus()
                }
            ),
            isError = emailError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        // Email error message
        if (emailError.isNotEmpty()) {
            ErrorMessage(emailError)
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
                    // Trigger login when Enter is pressed
                    attemptLogin()
                }
            ),
            isError = passwordError.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
        )

        // Password error message
        if (passwordError.isNotEmpty()) {
            ErrorMessage(passwordError)
        }

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

        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
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
