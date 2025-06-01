package dam.tfg.blinky.presentation.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.tfg.blinky.presentation.activities.LoginActivity
import dam.tfg.blinky.R
import dam.tfg.blinky.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun EnhancedProfileScreen() {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(factory = UserViewModel.Factory(context))
    val userState by viewModel.userState.collectAsState(initial = null)
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val successMessage by viewModel.successMessage
    val scope = rememberCoroutineScope()

    // State for edit name dialog
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // State for confirmation dialogs
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showVerifyPasswordDialog by remember { mutableStateOf(false) }
    var showNewPasswordDialog by remember { mutableStateOf(false) }
    var showResetPasswordConfirmation by remember { mutableStateOf(false) }

    // State for password reset
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Effect to show toast messages for errors and success
    LaunchedEffect(error, successMessage) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }

        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { /* TODO: Implement image picker */ },
            contentAlignment = Alignment.Center
        ) {
            if (userState?.profilePictureUrl != null) {
                // If there's a profile picture URL, load it (not implemented in this example)
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Default icon if no profile picture
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Name with edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nombre",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = userState?.name ?: "Cargando...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { 
                        newName = userState?.name ?: ""
                        showEditNameDialog = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = dam.tfg.blinky.ui.theme.GoogleBlue
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Correo electrónico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = userState?.email ?: "Cargando...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = { 
                currentPassword = ""
                showVerifyPasswordDialog = true 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Reset Password",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restablecer Contraseña")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showLogoutConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Editar Nombre") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updateUserName(newName)
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        val success = viewModel.logout()
                        if (success) {
                            // Navigate to login screen
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset Password Confirmation Dialog (Email method)
    if (showResetPasswordConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetPasswordConfirmation = false },
            title = { Text("Restablecer Contraseña") },
            text = { Text("Se enviará un correo electrónico a ${userState?.email} con instrucciones para restablecer tu contraseña.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetPasswordConfirmation = false
                        viewModel.requestPasswordResetEmail()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetPasswordConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Verify Password Dialog
    if (showVerifyPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyPasswordDialog = false },
            title = { Text("Verificar Contraseña") },
            text = {
                Column {
                    Text("Introduce tu contraseña actual para verificar tu identidad")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPassword.isNotBlank()) {
                            viewModel.verifyPassword(currentPassword) {
                                // On success
                                showVerifyPasswordDialog = false
                                newPassword = ""
                                showNewPasswordDialog = true
                            }
                        }
                    },
                    enabled = currentPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Verificar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVerifyPasswordDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // New Password Dialog
    if (showNewPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showNewPasswordDialog = false },
            title = { Text("Nueva Contraseña") },
            text = {
                Column {
                    Text("Introduce tu nueva contraseña")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.isNotBlank()) {
                            viewModel.resetPassword(newPassword)
                            showNewPasswordDialog = false
                        }
                    },
                    enabled = newPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNewPasswordDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
