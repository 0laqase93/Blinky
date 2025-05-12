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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.dataclass.ChatDTO
import dam.tfg.blinky.dataclass.ChatResponse
import dam.tfg.blinky.ui.theme.BlinkyTheme
import dam.tfg.blinky.utils.TokenManager
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

    // StateFlow para manejar el estado del texto reconocido
    private val promptStateFlow = MutableStateFlow("")

    // StateFlow para manejar el estado de la respuesta de la API
    private val responseStateFlow = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure RetrofitClient is initialized
        RetrofitClient.initialize(applicationContext)

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
                ChatScreen(
                    tts = tts,
                    onMicClick = { startSpeechRecognition() }
                )
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

    // Función para enviar el texto reconocido a la API
    private fun sendPromptToApi(prompt: String) {
        val chatDTO = ChatDTO(1, prompt) // Usando un ID de conversación fijo para simplificar

        RetrofitClient.api.sendPrompt(chatDTO).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        // Actualizar el estado de la respuesta
                        responseStateFlow.value = chatResponse.response

                        // Leer la respuesta en voz alta
                        tts.speak(chatResponse.response, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else {
                    val errorMsg = "Error en la respuesta: ${response.code()}"
                    Log.e("Blinky", errorMsg)
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("Blinky", errorMsg, t)
                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
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
    // Observar el estado del texto reconocido y la respuesta de la API
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val promptFlow = (context as MainActivity).getPromptStateFlow()
    val promptState = promptFlow.collectAsState(initial = "")
    val responseFlow = context.getResponseStateFlow()
    val responseState = responseFlow.collectAsState(initial = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Spacer(modifier = Modifier.height(24.dp))
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
