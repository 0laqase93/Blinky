package dam.tfg.blinky.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import dam.tfg.blinky.domain.model.CalendarEvent
import dam.tfg.blinky.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel for calendar events
 */
class CalendarViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    // State for events
    private val _events = mutableStateListOf<CalendarEvent>()
    val events: List<CalendarEvent> = _events

    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State for selected date
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /**
     * Set the selected date
     */
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * Load user events from the repository
     */
    fun loadUserEvents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                eventRepository.getUserEvents().enqueue(object : Callback<List<EventDTO>> {
                    override fun onResponse(call: Call<List<EventDTO>>, response: Response<List<EventDTO>>) {
                        _isLoading.value = false

                        if (response.isSuccessful && response.body() != null) {
                            // Clear existing events
                            _events.clear()

                            // Convert EventDTO to CalendarEvent and add to list
                            response.body()?.forEach { eventDTO ->
                                try {
                                    // Convert LocalDateTime to LocalDate and LocalTime
                                    val startDate = LocalDate.of(
                                        eventDTO.startTime!!.year,
                                        eventDTO.startTime!!.monthValue,
                                        eventDTO.startTime!!.dayOfMonth
                                    )

                                    val startTime = LocalTime.of(
                                        eventDTO.startTime!!.hour,
                                        eventDTO.startTime!!.minute
                                    )

                                    val endTime = LocalTime.of(
                                        eventDTO.endTime!!.hour,
                                        eventDTO.endTime!!.minute
                                    )

                                    // Create CalendarEvent
                                    val calendarEvent = CalendarEvent(
                                        apiId = eventDTO.id,
                                        title = eventDTO.title,
                                        date = startDate,
                                        time = startTime,
                                        endTime = endTime,
                                        description = eventDTO.description ?: "",
                                        location = eventDTO.location ?: ""
                                    )

                                    // Add to list
                                    _events.add(calendarEvent)
                                } catch (e: Exception) {
                                    Log.e("CalendarViewModel", "Error converting event", e)
                                }
                            }
                        } else {
                            // Get error details
                            val errorCode = response.code()
                            val errorBody = response.errorBody()?.string() ?: "Sin detalles"

                            // Show error message with more details
                            val errorMessage = when {
                                errorCode == 401 -> "Error de autenticación: Su sesión ha expirado. Por favor, inicie sesión nuevamente."
                                errorCode == 403 -> "Error de permisos: No tiene permisos para ver eventos."
                                errorCode == 404 -> "Error: El servicio de eventos no está disponible."
                                errorCode == 500 -> "Error del servidor: Problema interno del servidor."
                                else -> "Error al cargar los eventos (código $errorCode): $errorBody"
                            }

                            _error.value = errorMessage
                            Log.e("CalendarViewModel", "API Error: $errorCode - $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<List<EventDTO>>, t: Throwable) {
                        _isLoading.value = false

                        // Handle network or other errors
                        val errorMessage = when {
                            t.message?.contains("timeout") == true -> 
                                "Error de conexión: Tiempo de espera agotado. Compruebe su conexión a Internet."
                            t.message?.contains("Unable to resolve host") == true -> 
                                "Error de conexión: No se puede conectar al servidor. Compruebe su conexión a Internet."
                            else -> 
                                "Error al cargar los eventos: ${t.message ?: "Error desconocido"}"
                        }

                        _error.value = errorMessage
                        Log.e("CalendarViewModel", "Error loading events", t)
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false

                // Handle exceptions that occur before the API call
                val errorMessage = "Error al preparar la carga de eventos: ${e.message ?: "Error desconocido"}"
                _error.value = errorMessage
                Log.e("CalendarViewModel", "Error preparing to load events", e)
            }
        }
    }

    /**
     * Create a new event
     */
    fun createEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?,
        onSuccess: (Long?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                eventRepository.createEvent(
                    title = title,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    location = location
                ).enqueue(object : Callback<EventResponseDTO> {
                    override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                        _isLoading.value = false

                        if (response.isSuccessful && response.body() != null) {
                            val eventResponse = response.body()!!
                            if (eventResponse.success) {
                                onSuccess(eventResponse.eventId)
                                loadUserEvents() // Reload events
                            } else {
                                onError("")
                                loadUserEvents() // Reload events even on error
                            }
                        } else {
                            val errorCode = response.code()
                            val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                            val errorMessage = "Error al crear el evento (código $errorCode): $errorBody"
                            onError("")
                            loadUserEvents() // Reload events even on error
                            Log.e("CalendarViewModel", errorMessage)
                        }
                    }

                    override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                        _isLoading.value = false
                        val errorMessage = "Error de conexión: ${t.message ?: "Error desconocido"}"
                        onError("")
                        loadUserEvents() // Reload events even on error
                        Log.e("CalendarViewModel", "Error creating event", t)
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error al preparar la creación del evento: ${e.message ?: "Error desconocido"}"
                onError("")
                loadUserEvents() // Reload events even on error
                Log.e("CalendarViewModel", "Error preparing to create event", e)
            }
        }
    }

    /**
     * Update an existing event
     */
    fun updateEvent(
        eventId: Long,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String?,
        location: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                eventRepository.updateEvent(
                    eventId = eventId,
                    title = title,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    location = location
                ).enqueue(object : Callback<EventResponseDTO> {
                    override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                        _isLoading.value = false

                        if (response.isSuccessful && response.body() != null) {
                            val eventResponse = response.body()!!
                            if (eventResponse.success) {
                                onSuccess()
                                loadUserEvents() // Reload events
                            } else {
                                onError("")
                                loadUserEvents() // Reload events even on error
                            }
                        } else {
                            val errorCode = response.code()
                            val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                            val errorMessage = "Error al actualizar el evento (código $errorCode): $errorBody"
                            onError("")
                            loadUserEvents() // Reload events even on error
                            Log.e("CalendarViewModel", errorMessage)
                        }
                    }

                    override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                        _isLoading.value = false
                        val errorMessage = "Error de conexión: ${t.message ?: "Error desconocido"}"
                        onError("")
                        loadUserEvents() // Reload events even on error
                        Log.e("CalendarViewModel", "Error updating event", t)
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error al preparar la actualización del evento: ${e.message ?: "Error desconocido"}"
                onError("")
                loadUserEvents() // Reload events even on error
                Log.e("CalendarViewModel", "Error preparing to update event", e)
            }
        }
    }

    /**
     * Delete an event
     */
    fun deleteEvent(
        eventId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                eventRepository.deleteEvent(eventId).enqueue(object : Callback<EventResponseDTO> {
                    override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                        _isLoading.value = false

                        if (response.isSuccessful && response.body() != null) {
                            val eventResponse = response.body()!!
                            if (eventResponse.success) {
                                onSuccess()
                                loadUserEvents() // Reload events
                            } else {
                                onError("")
                                loadUserEvents() // Reload events even on error
                            }
                        } else {
                            val errorCode = response.code()
                            val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                            val errorMessage = "Error al eliminar el evento (código $errorCode): $errorBody"
                            onError("")
                            loadUserEvents() // Reload events even on error
                            Log.e("CalendarViewModel", errorMessage)
                        }
                    }

                    override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                        _isLoading.value = false
                        val errorMessage = "Error de conexión: ${t.message ?: "Error desconocido"}"
                        onError("")
                        loadUserEvents() // Reload events even on error
                        Log.e("CalendarViewModel", "Error deleting event", t)
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error al preparar la eliminación del evento: ${e.message ?: "Error desconocido"}"
                onError("")
                loadUserEvents() // Reload events even on error
                Log.e("CalendarViewModel", "Error preparing to delete event", e)
            }
        }
    }
}
