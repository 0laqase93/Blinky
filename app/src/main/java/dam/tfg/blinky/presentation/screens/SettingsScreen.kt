package dam.tfg.blinky.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.dataclass.PersonalityResponseDTO
import dam.tfg.blinky.presentation.viewmodel.PersonalityViewModel
import dam.tfg.blinky.utils.PermissionsUtils
import dam.tfg.blinky.utils.ThemeManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)

    // Get theme preferences from ThemeManager
    val followSystem by themeManager.followSystem
    val isDarkMode by themeManager.isDarkMode

    // Initialize AppConfig
    AppConfig.initialize(context)

    // State for server IP address
    var serverIp by remember { mutableStateOf(AppConfig.getServerIp()) }
    var showEditIpDialog by remember { mutableStateOf(false) }
    var newIpAddress by remember { mutableStateOf("") }

    // State for permissions
    var hasMicPermission by remember { mutableStateOf(PermissionsUtils.hasMicrophonePermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionsUtils.hasNotificationPermission(context)) }

    // State to trigger permission check
    var permissionCheckTrigger by remember { mutableStateOf(0) }

    // Cast context to Activity for permission requests
    // Force cast to ComponentActivity since we know the context is from MainActivity
    val activity = context as androidx.activity.ComponentActivity

    // Update permission states when the screen is displayed or when permissionCheckTrigger changes
    LaunchedEffect(permissionCheckTrigger) {
        hasMicPermission = PermissionsUtils.hasMicrophonePermission(context)
        hasNotificationPermission = PermissionsUtils.hasNotificationPermission(context)
    }

    // Initialize PersonalityViewModel
    val personalityViewModel = viewModel<PersonalityViewModel>(
        factory = PersonalityViewModel.Factory()
    )
    val personalitiesList by personalityViewModel.personalities.collectAsState<List<PersonalityResponseDTO>, List<PersonalityResponseDTO>>(initial = emptyList())

    // Access state values
    val isLoadingPersonalities by remember { personalityViewModel.isLoading }
    val personalitiesError by remember { personalityViewModel.error }

    // State for AI personality
    var aiPersonality by remember { mutableStateOf(AppConfig.getAIPersonality()) }
    var isPersonalityMenuExpanded by remember { mutableStateOf(false) }

    // State for deaf mode
    var isDeafMode by remember { mutableStateOf(AppConfig.getDeafMode()) }

    // Fallback list of personalities if API fails
    val fallbackPersonalities = listOf("Normal", "Amigable", "Profesional", "Divertido", "Sarcástico")

    // Use API personalities if available, otherwise use fallback
    val personalities = if (personalitiesList.isNotEmpty()) personalitiesList else fallbackPersonalities.map { 
        // Create dummy PersonalityResponseDTO objects for fallback personalities
        PersonalityResponseDTO(
            id = -1, // Use -1 as ID for fallback personalities
            name = it,
            basePrompt = "",
            description = ""
        )
    }

    // Create a scroll state that can be used to programmatically scroll
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Theme settings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tema",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Follow system theme toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (followSystem) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Follow System Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Usar tema del sistema",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "El tema cambiará automáticamente según la configuración de tu dispositivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = followSystem,
                        onCheckedChange = { themeManager.setFollowSystem(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Dark mode toggle (only enabled if not following system)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (followSystem) 0.5f else 1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = if (isDarkMode) "Modo oscuro" else "Modo claro",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (followSystem) 0.5f else 1f)
                            )
                            Text(
                                text = if (isDarkMode) "Utiliza un tema oscuro para reducir el cansancio visual" 
                                       else "Utiliza un tema claro para mejor visibilidad",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (followSystem) 0.35f else 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeManager.setDarkMode(it) },
                        enabled = !followSystem
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Server settings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Servidor",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Server IP address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Server IP",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Dirección IP del servidor",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = serverIp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            newIpAddress = serverIp
                            showEditIpDialog = true 
                        }
                    ) {
                        Text("Editar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI settings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "IA",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // AI Personality dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Personality",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Personalidad",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Selecciona el estilo de comunicación de la IA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    // Dropdown menu for personality selection
                    Box {
                        Button(
                            onClick = { isPersonalityMenuExpanded = true },
                            enabled = !isLoadingPersonalities
                        ) {
                            if (isLoadingPersonalities) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text("Cargando...")
                                }
                            } else {
                                Text(aiPersonality)
                            }
                        }

                        DropdownMenu(
                            expanded = isPersonalityMenuExpanded,
                            onDismissRequest = { isPersonalityMenuExpanded = false }
                        ) {
                            personalities.forEach { personality ->
                                DropdownMenuItem(
                                    text = { Text(personality.name) },
                                    onClick = {
                                        aiPersonality = personality.name
                                        AppConfig.setAIPersonalityWithId(personality.name, personality.id)
                                        isPersonalityMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Show error message if API call fails
                    if (personalitiesError != null && personalitiesList.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Error al cargar personalidades. Usando valores predeterminados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Deaf mode switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Deaf Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Modo sordo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Mostrar texto reconocido y respuesta solo cuando está activado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isDeafMode,
                        onCheckedChange = { 
                            isDeafMode = it
                            AppConfig.setDeafMode(it)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permissions settings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Permisos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Microphone permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone Permission",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Micrófono",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (hasMicPermission) "Permiso concedido" else "Permiso no concedido",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (!hasMicPermission) {
                                PermissionsUtils.requestMicrophonePermission(activity)
                                // Trigger permission check after request
                                permissionCheckTrigger++
                            }
                        },
                        enabled = !hasMicPermission
                    ) {
                        Text(if (hasMicPermission) "Concedido" else "Conceder")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Notification permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Permission",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "Notificaciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (hasNotificationPermission) "Permiso concedido" else "Permiso no concedido",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if (!hasNotificationPermission) {
                                PermissionsUtils.requestNotificationPermission(activity)
                                // Trigger permission check after request
                                permissionCheckTrigger++
                            }
                        },
                        enabled = !hasNotificationPermission
                    ) {
                        Text(if (hasNotificationPermission) "Concedido" else "Conceder")
                    }
                }
            }
        }
    }

    // Edit IP Dialog
    if (showEditIpDialog) {
        AlertDialog(
            onDismissRequest = { showEditIpDialog = false },
            title = { Text("Editar dirección IP del servidor") },
            text = {
                Column {
                    Text("Introduce la nueva dirección IP del servidor")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newIpAddress,
                        onValueChange = { newIpAddress = it },
                        label = { Text("Dirección IP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nota: Es necesario reiniciar la aplicación para que los cambios surtan efecto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newIpAddress.isNotBlank()) {
                            AppConfig.setServerIp(newIpAddress)
                            serverIp = newIpAddress
                            showEditIpDialog = false
                        }
                    },
                    enabled = newIpAddress.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditIpDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
