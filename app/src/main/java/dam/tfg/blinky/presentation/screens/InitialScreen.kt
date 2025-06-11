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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import dam.tfg.blinky.presentation.activities.MainActivity
import dam.tfg.blinky.R
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.data.repository.EventRepositoryImpl
import dam.tfg.blinky.dataclass.WrenchEmotion
import dam.tfg.blinky.presentation.viewmodel.CalendarViewModel
import dam.tfg.blinky.presentation.screens.AddEventDialog
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

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
    val eventFlow = context.getEventStateFlow()
    val eventState = eventFlow.collectAsState(initial = null)

    // Estado para el campo de texto y el diálogo
    val textInputState = remember { mutableStateOf("") }
    val showTextDialog = remember { mutableStateOf(false) }
    val showEventPopup = remember { mutableStateOf(false) }
    val isEventCreationMode = remember { mutableStateOf(false) }

    // Initialize AppConfig
    AppConfig.initialize(context)

    // Get deaf mode preference
    val isDeafMode = remember { mutableStateOf(AppConfig.getDeafMode()) }

    // Focus requester para el campo de texto
    val textFieldFocusRequester = remember { FocusRequester() }

    // Keyboard controller para mostrar el teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // Mostrar el popup de evento cuando se crea un evento
    LaunchedEffect(eventState.value) {
        if (eventState.value != null) {
            showEventPopup.value = true
        }
    }

    // Sincronizar el estado de isEventCreationMode con MainActivity
    LaunchedEffect(isEventCreationMode.value) {
        (context as MainActivity).setEventCreationMode(isEventCreationMode.value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                onClick = { 
                    // Sincronizar el estado de isEventCreationMode con MainActivity antes de mostrar el diálogo
                    (context as MainActivity).setEventCreationMode(isEventCreationMode.value)
                    showTextDialog.value = true 
                },
                modifier = Modifier
                    .padding(top = 0.dp, end = 16.dp)
                    .size(70.dp), // Fixed size
                shape = RoundedCornerShape(16.dp), // Slightly rounded corners instead of circle
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlueLight
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isEventCreationMode.value) "Escribir evento" else "Escribir mensaje",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            // FAB para hablar (micrófono)
            FloatingActionButton(
                onClick = { 
                    // Sincronizar el estado de isEventCreationMode con MainActivity antes de iniciar el reconocimiento de voz
                    (context as MainActivity).setEventCreationMode(isEventCreationMode.value)
                    onMicClick() 
                },
                modifier = Modifier
                    .padding(top = 80.dp, end = 16.dp) // Position below the write FAB
                    .size(70.dp), // Fixed size
                shape = RoundedCornerShape(16.dp), // Slightly rounded corners instead of circle
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlueDark
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isEventCreationMode.value) "Hablar para crear evento" else "Micrófono",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            // FAB para crear evento (a la altura del botón de hablar)
            // Actúa como un switch para activar/desactivar el modo de creación de eventos
            FloatingActionButton(
                onClick = { 
                    // Solo procesar el clic si no hay un evento mostrándose
                    if (eventState.value == null) {
                        // Toggle el modo de creación de eventos
                        isEventCreationMode.value = !isEventCreationMode.value
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 80.dp, start = 16.dp)
                    .size(70.dp), // Fixed size
                shape = RoundedCornerShape(16.dp), // Slightly rounded corners instead of circle
                containerColor = if (eventState.value == null) {
                    // Cuando no hay evento mostrándose, el color depende del modo
                    if (isEventCreationMode.value) 
                        dam.tfg.blinky.ui.theme.GoogleYellow // Opacidad normal cuando está activado
                    else 
                        dam.tfg.blinky.ui.theme.GoogleYellow.copy(alpha = 0.5f) // Semi-transparente cuando está desactivado
                } else {
                    // Siempre semi-transparente cuando hay un evento mostrándose (deshabilitado)
                    dam.tfg.blinky.ui.theme.GoogleYellow.copy(alpha = 0.5f)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Crear evento",
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
                        text = if (isEventCreationMode.value) "Describe el evento a crear" else "Escribe tu mensaje",
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

                        if (isEventCreationMode.value) {
                            // En modo de creación de evento, solo mostrar el botón de crear evento
                            Button(
                                onClick = {
                                    if (textInputState.value.isNotBlank()) {
                                        context.createEventFromPrompt(textInputState.value)
                                        textInputState.value = ""
                                        showTextDialog.value = false
                                    }
                                },
                                enabled = textInputState.value.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = dam.tfg.blinky.ui.theme.GoogleYellow
                                )
                            ) {
                                Text("Crear Evento")
                            }
                        } else {
                            // En modo normal, mostrar el botón de crear evento solo si está habilitado
                            // Obtener el estado actual del modo de creación de eventos desde MainActivity
                            val eventCreationEnabled = (context as MainActivity).getEventCreationMode()

                            // Botón para crear evento (solo se muestra si está habilitado)
                            if (eventCreationEnabled) {
                                Button(
                                    onClick = {
                                        if (textInputState.value.isNotBlank()) {
                                            context.createEventFromPrompt(textInputState.value)
                                            textInputState.value = ""
                                            showTextDialog.value = false
                                        }
                                    },
                                    enabled = textInputState.value.isNotBlank(),
                                    modifier = Modifier.padding(end = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = dam.tfg.blinky.ui.theme.GoogleYellow
                                    )
                                ) {
                                    Text("Crear Evento")
                                }
                            }

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

    // Popup para mostrar los detalles del evento creado
    if (showEventPopup.value && eventState.value != null) {
        // Obtener la fecha del evento
        val eventDate = remember { eventState.value?.startTime?.toLocalDate() ?: LocalDate.now() }

        // Obtener los valores del evento para pre-poblar el diálogo
        val eventTitle = remember { eventState.value?.title ?: "" }
        val eventStartTime = remember { eventState.value?.startTime?.toLocalTime() ?: LocalTime.now() }
        val eventEndTime = remember { eventState.value?.endTime?.toLocalTime() ?: LocalTime.now().plusHours(1) }
        val eventDescription = remember { eventState.value?.description ?: "" }
        val eventLocation = remember { eventState.value?.location ?: "" }

        // Usar el AddEventDialog de CalendarScreen
        AddEventDialog(
            selectedDate = eventDate,
            onDismiss = { 
                showEventPopup.value = false
                context.resetEventState()
            },
            onAddEvent = { title, time, endTime, description, location, notificationTime, selectedDate ->
                // Formatear la fecha y horas para la API
                val dateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
                val startTimeStr = time.format(DateTimeFormatter.ISO_TIME)
                val endTimeStr = endTime.format(DateTimeFormatter.ISO_TIME)

                // Obtener el CalendarViewModel de MainActivity
                val mainActivity = context as MainActivity
                val calendarViewModel = mainActivity.getCalendarViewModel()

                // Verificar si es un evento existente o uno nuevo
                val eventId = eventState.value?.id
                if (eventId != null) {
                    // Usar el método updateEvent de CalendarViewModel para actualizar el evento existente
                    calendarViewModel.updateEvent(
                        eventId = eventId,
                        title = title,
                        date = dateStr,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        description = description,
                        location = location,
                        onSuccess = {
                            // Cerrar el popup después de guardar
                            showEventPopup.value = false
                            // Reset event state to null when popup is closed
                            context.resetEventState()
                        },
                        onError = { errorMessage ->
                            // Cerrar el popup incluso si hay error
                            showEventPopup.value = false
                            // Reset event state to null when popup is closed
                            context.resetEventState()
                        }
                    )
                } else {
                    // Crear un nuevo evento si no hay ID (evento nuevo)
                    calendarViewModel.createEvent(
                        title = title,
                        date = dateStr,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        description = description,
                        location = location,
                        onSuccess = { newEventId ->
                            // Cerrar el popup después de guardar
                            showEventPopup.value = false
                            // Reset event state to null when popup is closed
                            context.resetEventState()
                        },
                        onError = { errorMessage ->
                            // Cerrar el popup incluso si hay error
                            showEventPopup.value = false
                            // Reset event state to null when popup is closed
                            context.resetEventState()
                        }
                    )
                }
            },
            // Pre-poblar los campos con los valores del evento
            initialTitle = eventTitle,
            initialTime = eventStartTime,
            initialEndTime = eventEndTime,
            initialDescription = eventDescription,
            initialLocation = eventLocation
        )
    }
}
