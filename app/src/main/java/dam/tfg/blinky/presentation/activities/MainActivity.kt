package dam.tfg.blinky.presentation.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.compose.NavHost
import dam.tfg.blinky.R
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import dam.tfg.blinky.dataclass.WrenchEmotion
import dam.tfg.blinky.navigation.BottomNavBar
import dam.tfg.blinky.navigation.Screen
import dam.tfg.blinky.presentation.screens.CalendarScreen
import dam.tfg.blinky.presentation.screens.EnhancedProfileScreen
import dam.tfg.blinky.presentation.screens.InitialScreen
import dam.tfg.blinky.presentation.screens.SettingsScreen
import dam.tfg.blinky.presentation.viewmodel.CalendarViewModel
import dam.tfg.blinky.data.repository.EventRepositoryImpl
import dam.tfg.blinky.data.repository.UserRepositoryImpl
import dam.tfg.blinky.ui.theme.BlinkyTheme
import dam.tfg.blinky.utils.ThemeManager
import dam.tfg.blinky.utils.TokenManager
import dam.tfg.blinky.utils.UserManager
import dam.tfg.blinky.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    // Instancias para TextToSpeech y el reconocimiento de voz
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: ActivityResultLauncher<Intent>

    // UserManager para acceder a los datos del usuario
    private lateinit var userManager: UserManager

    // CalendarViewModel para la pantalla de calendario
    private lateinit var calendarViewModel: CalendarViewModel

    // StateFlow para manejar el estado del texto reconocido
    private val promptStateFlow = MutableStateFlow("")

    // StateFlow para manejar el estado de la respuesta de la API
    private val responseStateFlow = MutableStateFlow("")

    // StateFlow para manejar la emoción actual
    private val emotionStateFlow = MutableStateFlow(WrenchEmotion.DEFAULT)

    // StateFlow para manejar el estado del evento creado
    private val eventStateFlow = MutableStateFlow<EventDTO?>(null)

    // Variable para controlar si TTS está hablando actualmente
    private var isSpeaking = false

    // Variable para controlar si estamos en modo de creación de eventos
    private var isEventCreationMode = false

    // Flag to track if we've already validated the token on startup
    private var initialTokenValidationDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure RetrofitClient is initialized
        RetrofitClient.initialize(applicationContext)

        // Initialize ThemeManager
        ThemeManager.getInstance(applicationContext)

        // Initialize AppConfig
        AppConfig.initialize(applicationContext)

        // Initialize UserManager
        userManager = UserManager.getInstance(applicationContext)

        // Check and request permissions when app starts
        checkAndRequestPermissions()

        // Validate token when app starts
        validateToken()
        initialTokenValidationDone = true

        // Initialize CalendarViewModel
        val eventRepository = EventRepositoryImpl(
            RetrofitClient.eventApi,
            userManager
        )
        calendarViewModel = CalendarViewModel(eventRepository, applicationContext)

        // Check if we need to schedule notifications for future events
        if (intent.getBooleanExtra("SCHEDULE_NOTIFICATIONS", false)) {
            // Load events and schedule notifications
            scheduleNotificationsForFutureEvents()
        }

        // Inicializar TextToSpeech
        tts = TextToSpeech(this, this)

        // Configurar listener para detectar cuando termina de hablar
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // No es necesario hacer nada al inicio
            }

            override fun onDone(utteranceId: String?) {
                // Cuando termina de hablar, volver a la emoción NEUTRAL
                runOnUiThread {
                    Log.d("Blinky", "TextToSpeech terminado, volviendo a emoción NEUTRAL")
                    emotionStateFlow.value = WrenchEmotion.NEUTRAL
                    isSpeaking = false
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                // En caso de error, también volver a NEUTRAL
                runOnUiThread {
                    Log.e("Blinky", "Error en TextToSpeech, volviendo a emoción NEUTRAL")
                    emotionStateFlow.value = WrenchEmotion.NEUTRAL
                    isSpeaking = false
                }
            }
        })

        // Configurar reconocimiento de voz
        speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                spokenText?.let {
                    // Usar el endpoint apropiado según el modo de creación de eventos
                    if (isEventCreationMode) {
                        createEventFromPrompt(it)
                    } else {
                        updatePrompt(it)
                    }
                }
            } else {
                val errorMsg = "Reconocimiento de voz cancelado"
                Log.e("Blinky", errorMsg)
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el contenido usando Jetpack Compose
        setContent {
            BlinkyTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            InitialScreen(
                                tts = tts,
                                onMicClick = { startSpeechRecognition() }
                            )
                        }
                        composable(Screen.Calendar.route) {
                            CalendarScreen(viewModel = calendarViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen()
                        }
                        composable(Screen.Profile.route) {
                            EnhancedProfileScreen()
                        }
                    }
                }
            }
        }
    }

    // Actualizar el texto reconocido, notificar el cambio de estado y enviar a la API
    fun updatePrompt(newPrompt: String) {
        promptStateFlow.value = newPrompt
        // Enviar el texto reconocido a la API
        sendPromptToApi(newPrompt)
    }

    // Función para exponer el StateFlow del prompt al Composable
    fun getPromptStateFlow(): StateFlow<String> = promptStateFlow

    // Función para exponer el StateFlow de la respuesta al Composable
    fun getResponseStateFlow(): StateFlow<String> = responseStateFlow

    // Función para exponer el StateFlow de la emoción al Composable
    fun getEmotionStateFlow(): StateFlow<WrenchEmotion> = emotionStateFlow

    // Función para exponer el StateFlow del evento al Composable
    fun getEventStateFlow(): StateFlow<EventDTO?> = eventStateFlow

    // Método para resetear el estado del evento
    fun resetEventState() {
        eventStateFlow.value = null
    }

    // Update an existing event
    fun updateEvent(eventId: Long, title: String, description: String?, location: String?) {
        // Get the current event from the state flow
        val currentEvent = eventStateFlow.value ?: return

        // Create a new event with updated fields but keeping the same dates
        val updatedEvent = currentEvent.copy(
            title = title,
            description = description,
            location = location
        )

        // Update the event state flow with the updated event
        eventStateFlow.value = updatedEvent

        // Format dates for the API
        val dateStr = updatedEvent.startTime?.toLocalDate()?.format(DateTimeFormatter.ISO_DATE) ?: return
        val startTimeStr = updatedEvent.startTime?.toLocalTime()?.format(DateTimeFormatter.ISO_TIME) ?: return
        val endTimeStr = updatedEvent.endTime?.toLocalTime()?.format(DateTimeFormatter.ISO_TIME) ?: return

        // Use the CalendarViewModel to update the event
        calendarViewModel.updateEvent(
            eventId = eventId,
            title = title,
            date = dateStr,
            startTime = startTimeStr,
            endTime = endTimeStr,
            description = description,
            location = location,
            onSuccess = {
                // Show success message
                Toast.makeText(this@MainActivity, "Evento actualizado correctamente", Toast.LENGTH_SHORT).show()

                // Set emotion to HAPPY for successful update
                emotionStateFlow.value = WrenchEmotion.HAPPY
            },
            onError = { errorMessage ->
                // Show error message
                Toast.makeText(this@MainActivity, "Error al actualizar el evento", Toast.LENGTH_SHORT).show()

                // Set emotion to ERROR for failed update
                emotionStateFlow.value = WrenchEmotion.ERROR
            }
        )
    }

    // Método para establecer el modo de creación de eventos
    fun setEventCreationMode(enabled: Boolean) {
        isEventCreationMode = enabled
    }

    // Método para obtener el estado actual del modo de creación de eventos
    fun getEventCreationMode(): Boolean {
        return isEventCreationMode
    }

    // Método para obtener el CalendarViewModel
    fun getCalendarViewModel(): CalendarViewModel {
        return calendarViewModel
    }

    // Método para actualizar la emoción actual
    fun setEmotion(emotion: WrenchEmotion) {
        emotionStateFlow.value = emotion
    }

    // Función para detectar etiquetas de emoción en el texto de respuesta
    private fun detectEmotionTag(text: String): WrenchEmotion {
        // Buscar etiquetas de emoción en el formato [EMOTION]
        return when {
            text.contains("[ANGRY]", ignoreCase = true) -> WrenchEmotion.ANGRY
            text.contains("[SAD]", ignoreCase = true) -> WrenchEmotion.SAD
            text.contains("[HAPPY]", ignoreCase = true) -> WrenchEmotion.HAPPY
            text.contains("[ERROR]", ignoreCase = true) -> WrenchEmotion.ERROR
            text.contains("[CONFUSED]", ignoreCase = true) -> WrenchEmotion.CONFUSED
            text.contains("[NEUTRAL]", ignoreCase = true) -> WrenchEmotion.NEUTRAL
            else -> WrenchEmotion.HAPPY // Por defecto, si no hay etiqueta, usar HAPPY
        }
    }

    // Función para enviar el texto reconocido a la API
    private fun sendPromptToApi(prompt: String) {
        // Establecer emoción a NEUTRAL mientras se procesa la solicitud
        emotionStateFlow.value = WrenchEmotion.NEUTRAL

        // Obtener el userId del UserManager
        val userId = userManager.userId.value
        // Si el userId no está disponible (valor -1), usar un valor alternativo
        val finalUserId = if (userId != -1L) userId else {
            // Fallback: usar el hashCode del email como userId (convertido a Long positivo)
            val email = userManager.userEmail.value
            Math.abs(email.hashCode().toLong())
        }
        // Get the personality ID from AppConfig
        val personalityId = AppConfig.getAIPersonalityId()
        val personalityName = AppConfig.getAIPersonality()
        Log.d("Blinky", "Sending prompt with personality: $personalityName (ID: $personalityId)")
        val chatDTO = ChatDTO(prompt, finalUserId, personalityId)

        RetrofitClient.ttlApi.sendPrompt(chatDTO).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        // Actualizar el estado de la respuesta
                        responseStateFlow.value = chatResponse.response

                        // Detectar etiqueta de emoción desde el campo reaction y establecer la emoción correspondiente
                        val detectedEmotion = if (chatResponse.reaction.isNotEmpty()) {
                            detectEmotionTag(chatResponse.reaction)
                        } else {
                            // Fallback: intentar detectar en el texto de respuesta si reaction está vacío
                            detectEmotionTag(chatResponse.response)
                        }
                        emotionStateFlow.value = detectedEmotion

                        // Registrar la emoción detectada
                        Log.d("Blinky", "Emoción detectada: ${detectedEmotion.description} (de ${if (chatResponse.reaction.isNotEmpty()) "reaction" else "response"})")

                        // Check if deaf mode is enabled
                        val isDeafMode = AppConfig.getDeafMode()

                        // Only use TextToSpeech if deaf mode is not enabled
                        if (!isDeafMode) {
                            // Leer la respuesta en voz alta
                            val utteranceId = "utterance_${System.currentTimeMillis()}"
                            val params = Bundle()
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                            isSpeaking = true
                            tts.speak(chatResponse.response, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                        } else {
                            // Log that TTS is skipped due to deaf mode
                            Log.d("Blinky", "TextToSpeech skipped due to deaf mode being enabled")
                        }
                    }
                } else {
                    val errorMsg = "Error en la respuesta: ${response.code()}"
                    Log.e("Blinky", errorMsg)

                    // Log the full error response body
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Blinky", "Error response body: $errorBody")

                        // Log additional response details
                        Log.e("Blinky", "Response message: ${response.message()}")
                        Log.e("Blinky", "Response headers: ${response.headers()}")
                        Log.e("Blinky", "Request URL: ${call.request().url()}")
                        Log.e("Blinky", "Request method: ${call.request().method()}")
                        Log.e("Blinky", "Request body: ${call.request().body()}")
                    } catch (e: Exception) {
                        Log.e("Blinky", "Error parsing error response", e)
                    }

                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()

                    // Establecer emoción a ERROR para respuesta fallida
                    emotionStateFlow.value = WrenchEmotion.ERROR
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("Blinky", errorMsg, t)

                // Log additional request details for debugging
                Log.e("Blinky", "Failed request URL: ${call.request().url()}")
                Log.e("Blinky", "Failed request method: ${call.request().method()}")

                // Log the request body if available
                try {
                    val requestBody = call.request().body()
                    Log.e("Blinky", "Failed request body: $requestBody")

                    // Log the stack trace with more details
                    Log.e("Blinky", "Exception stack trace:", t)

                    // Log the cause if available
                    t.cause?.let { cause ->
                        Log.e("Blinky", "Caused by: ${cause.message}", cause)
                    }
                } catch (e: Exception) {
                    Log.e("Blinky", "Error logging request details", e)
                }

                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()

                // Establecer emoción a ERROR para conexión fallida
                emotionStateFlow.value = WrenchEmotion.ERROR
            }
        })
    }

    // Crear un evento a partir del prompt
    fun createEventFromPrompt(prompt: String) {
        promptStateFlow.value = prompt
        // Enviar el texto reconocido a la API para crear un evento
        sendPromptToCreateEvent(prompt)
    }

    // Función para enviar el texto reconocido a la API para crear un evento
    private fun sendPromptToCreateEvent(prompt: String) {
        // Establecer emoción a NEUTRAL mientras se procesa la solicitud
        emotionStateFlow.value = WrenchEmotion.NEUTRAL

        // Reset event state
        eventStateFlow.value = null

        // Obtener el userId del UserManager
        val userId = userManager.userId.value
        // Si el userId no está disponible (valor -1), usar un valor alternativo
        val finalUserId = if (userId != -1L) userId else {
            // Fallback: usar el hashCode del email como userId (convertido a Long positivo)
            val email = userManager.userEmail.value
            Math.abs(email.hashCode().toLong())
        }
        // Get the personality ID from AppConfig
        val personalityId = AppConfig.getAIPersonalityId()
        val personalityName = AppConfig.getAIPersonality()
        Log.d("Blinky", "Creating event from prompt with personality: $personalityName (ID: $personalityId)")
        val chatDTO = ChatDTO(prompt, finalUserId, personalityId)

        RetrofitClient.ttlApi.createEvent(chatDTO).enqueue(object : Callback<EventDTO> {
            override fun onResponse(call: Call<EventDTO>, response: Response<EventDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { eventDTO ->
                        // Actualizar el estado del evento
                        eventStateFlow.value = eventDTO

                        // Actualizar el estado de la respuesta para mostrar confirmación
                        responseStateFlow.value = "Evento creado: ${eventDTO.title}"

                        // Establecer emoción a HAPPY para evento creado exitosamente
                        emotionStateFlow.value = WrenchEmotion.HAPPY

                        Log.d("Blinky", "Evento creado: ${eventDTO.title}")

                        // Check if deaf mode is enabled
                        val isDeafMode = AppConfig.getDeafMode()

                        // Only use TextToSpeech if deaf mode is not enabled
                        if (!isDeafMode) {
                            // Leer la respuesta en voz alta
                            val utteranceId = "utterance_${System.currentTimeMillis()}"
                            val params = Bundle()
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                            isSpeaking = true
                            tts.speak(responseStateFlow.value, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                        } else {
                            // Log that TTS is skipped due to deaf mode
                            Log.d("Blinky", "TextToSpeech skipped due to deaf mode being enabled")
                        }
                    }
                } else {
                    val errorCode = response.code()
                    val errorMsg = "Error al crear evento: $errorCode"
                    Log.e("Blinky", errorMsg)

                    // Log the full error response body
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("Blinky", "Error response body: $errorBody")

                        // Log additional response details
                        Log.e("Blinky", "Response message: ${response.message()}")
                        Log.e("Blinky", "Response headers: ${response.headers()}")
                        Log.e("Blinky", "Request URL: ${call.request().url()}")
                        Log.e("Blinky", "Request method: ${call.request().method()}")
                        Log.e("Blinky", "Request body: ${call.request().body()}")
                    } catch (e: Exception) {
                        Log.e("Blinky", "Error parsing error response", e)
                    }

                    if (errorCode == 403) {
                        // For 403 errors, update the response with a message about event creation not being specified
                        responseStateFlow.value = "No se ha especificado crear el evento. Por favor, activa la creación de eventos primero."
                        // Set emotion to CONFUSED instead of ERROR
                        emotionStateFlow.value = WrenchEmotion.CONFUSED

                        // Check if deaf mode is enabled
                        val isDeafMode = AppConfig.getDeafMode()

                        // Only use TextToSpeech if deaf mode is not enabled
                        if (!isDeafMode) {
                            // Leer la respuesta en voz alta
                            val utteranceId = "utterance_${System.currentTimeMillis()}"
                            val params = Bundle()
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                            isSpeaking = true
                            tts.speak(responseStateFlow.value, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                        } else {
                            // Log that TTS is skipped due to deaf mode
                            Log.d("Blinky", "TextToSpeech skipped due to deaf mode being enabled")
                        }
                    } else {
                        // For other errors, show the toast and set emotion to ERROR
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        emotionStateFlow.value = WrenchEmotion.ERROR
                    }
                }
            }

            override fun onFailure(call: Call<EventDTO>, t: Throwable) {
                val errorMsg = "Error de conexión al crear evento: ${t.message}"
                Log.e("Blinky", errorMsg, t)

                // Log additional request details for debugging
                Log.e("Blinky", "Failed request URL: ${call.request().url()}")
                Log.e("Blinky", "Failed request method: ${call.request().method()}")

                // Log the request body if available
                try {
                    val requestBody = call.request().body()
                    Log.e("Blinky", "Failed request body: $requestBody")

                    // Log the stack trace with more details
                    Log.e("Blinky", "Exception stack trace:", t)

                    // Log the cause if available
                    t.cause?.let { cause ->
                        Log.e("Blinky", "Caused by: ${cause.message}", cause)
                    }
                } catch (e: Exception) {
                    Log.e("Blinky", "Error logging request details", e)
                }

                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()

                // Establecer emoción a ERROR para conexión fallida
                emotionStateFlow.value = WrenchEmotion.ERROR
            }
        })
    }

    // Función para detener TTS si está hablando
    fun stopSpeaking() {
        if (isSpeaking && tts.isSpeaking) {
            tts.stop()
            isSpeaking = false
            Log.d("Blinky", "TextToSpeech detenido manualmente")
            // Volver a la emoción NEUTRAL cuando se detiene manualmente
            emotionStateFlow.value = WrenchEmotion.NEUTRAL
        }
    }

    // Función para iniciar el reconocimiento de voz o detener TTS si está hablando
    private fun startSpeechRecognition() {
        // Si TTS está hablando, detenerlo
        if (isSpeaking && tts.isSpeaking) {
            stopSpeaking()
            // No return here, continue to start speech recognition
        }

        // Establecer emoción a QUESTION para indicar que está escuchando
        emotionStateFlow.value = WrenchEmotion.QUESTION

        // Iniciar reconocimiento de voz (siempre, incluso después de detener TTS)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora")
        }
        speechRecognizer.launch(intent)
    }

    // Método llamado cuando TextToSpeech está listo
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = tts.setLanguage(Locale.getDefault())
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val errorMsg = "Idioma no soportado para TTS"
                Log.e("Blinky", errorMsg)
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val errorMsg = "Error al inicializar TTS"
            Log.e("Blinky", errorMsg)
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // App resume handler
    override fun onResume() {
        super.onResume()

        // Token validation on resume has been removed as per requirements
        // The token is now only validated when the app starts
    }

    // Liberar recursos en onDestroy
    override fun onDestroy() {
        isSpeaking = false
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    // Validate token and redirect to login if invalid
    private fun validateToken() {
        // Create UserRepository
        val userRepository = UserRepositoryImpl(applicationContext)

        // Launch coroutine to validate token
        CoroutineScope(Dispatchers.Main).launch {
            val isValid = userRepository.validateToken()

            if (!isValid) {
                Log.d("MainActivity", "Token validation failed, redirecting to login")
                // Token is invalid, redirect to login
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Close MainActivity
            } else {
                Log.d("MainActivity", "Token validation successful")
            }
        }
    }

    // Check and request necessary permissions
    private fun checkAndRequestPermissions() {
        // Import PermissionsUtils
        val permissionsUtils = dam.tfg.blinky.utils.PermissionsUtils

        // Check and request microphone permission
        if (!permissionsUtils.hasMicrophonePermission(this)) {
            Log.d("MainActivity", "Requesting microphone permission")
            permissionsUtils.requestMicrophonePermission(this)
        } else {
            Log.d("MainActivity", "Microphone permission already granted")
        }

        // Check and request notification permission
        if (!permissionsUtils.hasNotificationPermission(this)) {
            Log.d("MainActivity", "Requesting notification permission")
            permissionsUtils.requestNotificationPermission(this)
        } else {
            Log.d("MainActivity", "Notification permission already granted")
        }
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            101 -> { // MICROPHONE_PERMISSION_REQUEST_CODE
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Microphone permission granted")
                } else {
                    Log.d("MainActivity", "Microphone permission denied")
                    // Show a toast explaining why the permission is needed
                    Toast.makeText(
                        this,
                        "El permiso de micrófono es necesario para usar el reconocimiento de voz",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            102 -> { // NOTIFICATION_PERMISSION_REQUEST_CODE
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Notification permission granted")
                } else {
                    Log.d("MainActivity", "Notification permission denied")
                    // Show a toast explaining why the permission is needed
                    Toast.makeText(
                        this,
                        "El permiso de notificaciones es necesario para recibir alertas de eventos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Schedule notifications for all future events
     */
    private fun scheduleNotificationsForFutureEvents() {
        // Create a NotificationHelper
        val notificationHelper = NotificationHelper(applicationContext)

        // Load events
        calendarViewModel.loadUserEvents()

        // Wait for events to load and then schedule notifications
        CoroutineScope(Dispatchers.Main).launch {
            // Give time for events to load
            kotlinx.coroutines.delay(1000)

            // Get current date
            val today = org.threeten.bp.LocalDate.now()

            // Get notification settings from AppConfig
            val notificationEnabled = AppConfig.getNotificationEnabled()
            val notificationHours = AppConfig.getNotificationHours()
            val notificationMinutes = AppConfig.getNotificationMinutes()

            // Only proceed if notifications are enabled
            if (notificationEnabled) {
                // Create notification time
                val notificationTime = org.threeten.bp.LocalTime.of(notificationHours, notificationMinutes)

                // Filter events that are today or in the future
                val futureEvents = calendarViewModel.events.filter { event ->
                    event.date.isEqual(today) || event.date.isAfter(today)
                }

                // Schedule notifications for each future event
                futureEvents.forEach { event ->
                    // Create a copy of the event with the notification time
                    val eventWithNotification = event.copy(notificationTime = notificationTime)

                    // Schedule notification
                    notificationHelper.scheduleNotification(eventWithNotification)

                    Log.d("MainActivity", "Scheduled notification for event: ${event.title} on ${event.date} at ${event.time}")
                }

                // Show toast message
                val message = if (futureEvents.isEmpty()) {
                    "No hay eventos futuros para programar notificaciones"
                } else {
                    "Se han programado notificaciones para ${futureEvents.size} eventos futuros"
                }
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
