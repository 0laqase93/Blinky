package dam.tfg.blinky.presentation.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.tfg.blinky.presentation.activities.MainActivity
import dam.tfg.blinky.R
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.dataclass.WrenchEmotion

@Composable
fun InitialScreen(
    modifier: Modifier = Modifier,
    tts: TextToSpeech,
    onMicClick: () -> Unit
) {
    // Define custom font for the avatar eyes
    val avatarFont = FontFamily(Font(R.font.vt323regular))
    // Observar el estado del texto reconocido, la respuesta de la API y la emoción
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val promptFlow = (context as MainActivity).getPromptStateFlow()
    val promptState = promptFlow.collectAsState(initial = "")
    val responseFlow = context.getResponseStateFlow()
    val responseState = responseFlow.collectAsState(initial = "")
    val emotionFlow = context.getEmotionStateFlow()
    val emotionState = emotionFlow.collectAsState(initial = WrenchEmotion.DEFAULT)

    // Estado para el campo de texto y el diálogo
    val textInputState = remember { mutableStateOf("") }
    val showTextDialog = remember { mutableStateOf(false) }

    // Initialize AppConfig
    AppConfig.initialize(context)

    // Get deaf mode preference
    val isDeafMode = remember { mutableStateOf(AppConfig.getDeafMode()) }

    // Focus requester para el campo de texto
    val textFieldFocusRequester = remember { FocusRequester() }

    // Keyboard controller para mostrar el teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sección para los ojos (estilo Wrench) - ocupando la mitad de la pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ojo izquierdo con animación
                AnimatedContent(
                    targetState = emotionState.value.leftEye,
                    transitionSpec = {
                        // Definir la animación para la transición
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "LeftEyeAnimation"
                ) { targetEye ->
                    Text(
                        text = targetEye,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 300.sp,
                            fontFamily = avatarFont
                        )
                    )
                }

                // Espaciado entre los ojos
                Spacer(modifier = Modifier.width(48.dp))

                // Ojo derecho con animación
                AnimatedContent(
                    targetState = emotionState.value.rightEye,
                    transitionSpec = {
                        // Definir la animación para la transición
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "RightEyeAnimation"
                ) { targetEye ->
                    Text(
                        text = targetEye,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 300.sp,
                            fontFamily = avatarFont
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Only show text sections if deaf mode is enabled
        if (isDeafMode.value) {
            // Sección para el texto reconocido
            Text(
                text = "Texto reconocido:",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = promptState.value,
                style = MaterialTheme.typography.bodyMedium
            )

            // Sección para la respuesta de la API
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Respuesta de Ollama:",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = responseState.value,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Botones FAB para escribir y hablar
        Spacer(modifier = Modifier.weight(1f))

        // Use a fixed height Box to ensure consistent space for FABs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), // Fixed height to ensure consistent space
            contentAlignment = Alignment.TopEnd
        ) {
            // FAB para escribir (encima del micrófono)
            FloatingActionButton(
                onClick = { showTextDialog.value = true },
                modifier = Modifier
                    .padding(top = 0.dp, end = 16.dp)
                    .size(70.dp), // Fixed size
                shape = RoundedCornerShape(16.dp) // Slightly rounded corners instead of circle
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Escribir mensaje",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            // FAB para hablar (micrófono)
            FloatingActionButton(
                onClick = onMicClick,
                modifier = Modifier
                    .padding(top = 80.dp, end = 16.dp) // Position below the write FAB
                    .size(70.dp), // Fixed size
                shape = RoundedCornerShape(16.dp), // Slightly rounded corners instead of circle
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Micrófono",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    }

    // Popup de texto cuando se presiona el FAB de edición
    if (showTextDialog.value) {
        // Fondo oscurecido que ocupa toda la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { showTextDialog.value = false },
            contentAlignment = Alignment.Center
        ) {
            // Tarjeta del diálogo que no propaga clics al fondo
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
                    .clickable(onClick = { /* Consumir el clic para evitar que se cierre */ }, enabled = true),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escribe tu mensaje",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = textInputState.value,
                        onValueChange = { newValue -> textInputState.value = newValue },
                        label = { Text(text = "Mensaje") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFieldFocusRequester),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5
                    )

                    // Solicitar el foco y mostrar el teclado cuando aparece el diálogo
                    LaunchedEffect(showTextDialog.value) {
                        if (showTextDialog.value) {
                            textFieldFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTextDialog.value = false }
                        ) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (textInputState.value.isNotBlank()) {
                                    context.updatePrompt(textInputState.value)
                                    textInputState.value = ""
                                    showTextDialog.value = false
                                }
                            },
                            enabled = textInputState.value.isNotBlank()
                        ) {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}
