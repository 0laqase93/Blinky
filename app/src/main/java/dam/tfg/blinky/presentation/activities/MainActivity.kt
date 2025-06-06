package dam.tfg.blinky

import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import dam.tfg.blinky.dataclass.WrenchEmotion
import dam.tfg.blinky.navigation.BottomNavBar
import dam.tfg.blinky.navigation.Screen
import dam.tfg.blinky.presentation.components.AnimatedEyes
import dam.tfg.blinky.presentation.components.AnimatedEyes
import dam.tfg.blinky.presentation.components.AnimatedEyes
import dam.tfg.blinky.screens.CalendarScreen
import dam.tfg.blinky.presentation.screens.EnhancedProfileScreen
import dam.tfg.blinky.screens.SettingsScreen
import dam.tfg.blinky.ui.theme.BlinkyTheme
import dam.tfg.blinky.utils.ThemeManager
import dam.tfg.blinky.utils.TokenManager
import dam.tfg.blinky.utils.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    // StateFlow para manejar el estado del texto reconocido
    private val promptStateFlow = MutableStateFlow("")

    // StateFlow para manejar el estado de la respuesta de la API
    private val responseStateFlow = MutableStateFlow("")

    // StateFlow para manejar la emoción actual
    private val emotionStateFlow = MutableStateFlow(WrenchEmotion.DEFAULT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure RetrofitClient is initialized
        RetrofitClient.initialize(applicationContext)

        // Initialize ThemeManager
        ThemeManager.getInstance(applicationContext)

        // Initialize UserManager
        userManager = UserManager.getInstance(applicationContext)

        // Inicializar TextToSpeech
        tts = TextToSpeech(this, this)

        // Configurar reconocimiento de voz
        speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                spokenText?.let {
                    updatePrompt(it)
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
                            ChatScreen(
                                tts = tts,
                                onMicClick = { startSpeechRecognition() }
                            )
                        }
                        composable(Screen.Calendar.route) {
                            CalendarScreen()
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
    private fun updatePrompt(newPrompt: String) {
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
        val chatDTO = ChatDTO(prompt, finalUserId)

        RetrofitClient.api.sendPrompt(chatDTO).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        // Actualizar el estado de la respuesta
                        responseStateFlow.value = chatResponse.response

                        // Detectar etiqueta de emoción en la respuesta y establecer la emoción correspondiente
                        val detectedEmotion = detectEmotionTag(chatResponse.response)
                        emotionStateFlow.value = detectedEmotion

                        // Registrar la emoción detectada
                        Log.d("Blinky", "Emoción detectada: ${detectedEmotion.description}")

                        // Leer la respuesta en voz alta
                        tts.speak(chatResponse.response, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else {
                    val errorMsg = "Error en la respuesta: ${response.code()}"
                    Log.e("Blinky", errorMsg)
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()

                    // Establecer emoción a ERROR para respuesta fallida
                    emotionStateFlow.value = WrenchEmotion.ERROR
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("Blinky", errorMsg, t)
                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()

                // Establecer emoción a ERROR para conexión fallida
                emotionStateFlow.value = WrenchEmotion.ERROR
            }
        })
    }

    // Función para iniciar el reconocimiento de voz
    private fun startSpeechRecognition() {
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

    // Liberar recursos en onDestroy
    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    tts: TextToSpeech,
    onMicClick: () -> Unit
) {
    // Observar el estado del texto reconocido, la respuesta de la API y la emoción
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val promptFlow = (context as MainActivity).getPromptStateFlow()
    val promptState = promptFlow.collectAsState(initial = "")
    val responseFlow = context.getResponseStateFlow()
    val responseState = responseFlow.collectAsState(initial = "")
    val emotionFlow = context.getEmotionStateFlow()
    val emotionState = emotionFlow.collectAsState(initial = WrenchEmotion.DEFAULT)

    // Estados para las posiciones de los ojos
    val leftEyeOffset = remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val rightEyeOffset = remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Efecto para animar los ojos con movimiento aleatorio
    LaunchedEffect(key1 = Unit) {
        while(true) {
            // Determinar si volvemos al centro o movemos aleatoriamente
            val returnToCenter = (0..10).random() < 2 // 20% de probabilidad de volver al centro

            if (returnToCenter) {
                // Volver al centro gradualmente
                leftEyeOffset.value = androidx.compose.ui.geometry.Offset.Zero
                rightEyeOffset.value = androidx.compose.ui.geometry.Offset.Zero
            } else {
                // Generar nuevas posiciones aleatorias dentro de un rango limitado
                val maxOffset = 30f // Máximo desplazamiento en dp
                leftEyeOffset.value = androidx.compose.ui.geometry.Offset(
                    x = (-maxOffset..maxOffset).random(),
                    y = (-maxOffset..maxOffset).random()
                )
                rightEyeOffset.value = androidx.compose.ui.geometry.Offset(
                    x = (-maxOffset..maxOffset).random(),
                    y = (-maxOffset..maxOffset).random()
                )
            }

            // Esperar antes del siguiente movimiento (entre 0.5 y 2 segundos)
            kotlinx.coroutines.delay((500..2000).random().toLong())
        }
    }

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
                // Ojo izquierdo con offset aleatorio
                Box(
                    modifier = Modifier
                        .offset(x = leftEyeOffset.value.x.dp, y = leftEyeOffset.value.y.dp)
                ) {
                    Text(
                        text = emotionState.value.leftEye,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 150.sp)
                    )
                }
                // Espaciado entre los ojos
                Spacer(modifier = Modifier.width(48.dp))
                // Ojo derecho con offset aleatorio
                Box(
                    modifier = Modifier
                        .offset(x = rightEyeOffset.value.x.dp, y = rightEyeOffset.value.y.dp)
                ) {
                    Text(
                        text = emotionState.value.rightEye,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 150.sp)
                    )
                }
            }
        }

        // Botones para probar las emociones
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Probar emociones:",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WrenchEmotion.values().forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    onClick = { context.setEmotion(emotion) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        // Botón para hablar
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onMicClick) {
            Icon(Icons.Default.Phone, contentDescription = "Micrófono")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hablar")
        }
    }
}

@Composable
fun EmotionButton(
    emotion: WrenchEmotion,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emotion.leftEye,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = emotion.rightEye,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
