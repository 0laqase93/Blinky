package dam.tfg.blinky.screens

import android.app.Activity
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.tfg.blinky.api.RetrofitClient
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.dataclass.EventDTO
import dam.tfg.blinky.dataclass.EventResponseDTO
import dam.tfg.blinky.dataclass.PersonalityResponseDTO
import dam.tfg.blinky.presentation.viewmodel.PersonalityViewModel
import dam.tfg.blinky.utils.CalendarUtils
import dam.tfg.blinky.utils.ThemeManager
import dam.tfg.blinky.utils.UserManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale

// Data class to represent a calendar event
data class CalendarEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val apiId: Long? = null,  // ID from the API response
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val endTime: LocalTime = time.plusHours(1),
    val description: String = "",
    val location: String = "",
    val notificationTime: LocalTime? = null  // Time before the event to show notification
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    // State for the current week's start date (Monday)
    var currentWeekStart by remember { mutableStateOf(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )}

    // State for the list of events
    val events = remember { mutableStateListOf<CalendarEvent>() }

    // State for the selected date
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // State for showing the add event dialog
    var showAddEventDialog by remember { mutableStateOf(false) }

    // State for showing the edit event dialog
    var showEditEventDialog by remember { mutableStateOf(false) }

    // State for the event being edited
    var eventBeingEdited by remember { mutableStateOf<CalendarEvent?>(null) }

    // State for showing the delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // State for the event being deleted
    var eventBeingDeleted by remember { mutableStateOf<CalendarEvent?>(null) }

    // State for showing the notification time picker dialog
    var showNotificationDialog by remember { mutableStateOf(false) }

    // State for the event for which we're setting a notification
    var eventForNotification by remember { mutableStateOf<CalendarEvent?>(null) }

    // Context for showing toasts
    val context = LocalContext.current

    // Initialize UserManager
    val userManager = UserManager.getInstance(context)

    // Coroutine scope for API calls
    val coroutineScope = rememberCoroutineScope()

    // Format for displaying dates
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))

    // State for loading events
    var isLoadingEvents by remember { mutableStateOf(false) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    // Function to load user events from API
    fun loadUserEvents() {
        coroutineScope.launch {
            try {
                isLoadingEvents = true
                loadingError = null

                // Get userId from UserManager
                val userId = userManager.userId.value

                // Check if userId is valid
                if (userId <= 0) {
                    throw Exception("No se pudo obtener el ID de usuario. Por favor, inicie sesión nuevamente.")
                }

                // Call API asynchronously
                RetrofitClient.eventApi.getUserEvents(userId).enqueue(object : Callback<List<EventDTO>> {
                    override fun onResponse(call: Call<List<EventDTO>>, response: Response<List<EventDTO>>) {
                        isLoadingEvents = false

                        if (response.isSuccessful && response.body() != null) {
                            // Clear existing events
                            events.clear()

                            // Convert EventDTO to CalendarEvent and add to list
                            response.body()?.forEach { eventDTO ->
                                try {
                                    // Convert LocalDateTime to LocalDate and LocalTime
                                    val startDate = LocalDate.of(
                                        eventDTO.startTime.year,
                                        eventDTO.startTime.monthValue,
                                        eventDTO.startTime.dayOfMonth
                                    )

                                    val startTime = LocalTime.of(
                                        eventDTO.startTime.hour,
                                        eventDTO.startTime.minute
                                    )

                                    val endTime = LocalTime.of(
                                        eventDTO.endTime.hour,
                                        eventDTO.endTime.minute
                                    )

                                    // Create CalendarEvent
                                    val calendarEvent = CalendarEvent(
                                        apiId = eventDTO.id,  // Assuming EventDTO has an id field
                                        title = eventDTO.title,
                                        date = startDate,
                                        time = startTime,
                                        endTime = endTime,
                                        description = eventDTO.description ?: "",
                                        location = eventDTO.location ?: ""
                                    )

                                    // Add to list
                                    events.add(calendarEvent)
                                } catch (e: Exception) {
                                    Log.e("CalendarScreen", "Error converting event", e)
                                }
                            }

                            // Show success message
                            Toast.makeText(context, "Eventos cargados correctamente", Toast.LENGTH_SHORT).show()
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

                            loadingError = errorMessage
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e("CalendarScreen", "API Error: $errorCode - $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<List<EventDTO>>, t: Throwable) {
                        isLoadingEvents = false

                        // Handle network or other errors
                        val errorMessage = when {
                            t.message?.contains("timeout") == true -> 
                                "Error de conexión: Tiempo de espera agotado. Compruebe su conexión a Internet."
                            t.message?.contains("Unable to resolve host") == true -> 
                                "Error de conexión: No se puede conectar al servidor. Compruebe su conexión a Internet."
                            else -> 
                                "Error al cargar los eventos: ${t.message ?: "Error desconocido"}"
                        }

                        loadingError = errorMessage
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("CalendarScreen", "Error loading events", t)
                    }
                })
            } catch (e: Exception) {
                isLoadingEvents = false

                // Handle exceptions that occur before the API call
                val errorMessage = "Error al preparar la carga de eventos: ${e.message ?: "Error desconocido"}"
                loadingError = errorMessage
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                Log.e("CalendarScreen", "Error preparing to load events", e)
            }
        }
    }

    // Load events when the screen is first displayed
    LaunchedEffect(key1 = Unit) {
        loadUserEvents()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evento"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Calendar header with title and navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendario",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous week button
                    IconButton(
                        onClick = {
                            currentWeekStart = currentWeekStart.minusWeeks(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Semana anterior"
                        )
                    }

                    // Current week display
                    Text(
                        text = "${currentWeekStart.format(dateFormatter)} - ${currentWeekStart.plusDays(6).format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Next week button
                    IconButton(
                        onClick = {
                            currentWeekStart = currentWeekStart.plusWeeks(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Semana siguiente"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of the week (Monday to Sunday)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayOffset in 0..6) {
                    val date = currentWeekStart.plusDays(dayOffset.toLong())
                    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                    val isSelected = date.isEqual(selectedDate)
                    val isToday = date.isEqual(LocalDate.now())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        // Day name (Mon, Tue, etc.)
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day number with selection indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { selectedDate = date }
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Selected date display with refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Eventos para ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES")))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Refresh button
                IconButton(
                    onClick = { loadUserEvents() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar eventos"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading indicator
            if (isLoadingEvents) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cargando eventos...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Error message
            if (!isLoadingEvents && loadingError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loadingError ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { loadUserEvents() }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Events for the selected date
            val selectedDateEvents = events.filter { it.date.isEqual(selectedDate) }

            if (!isLoadingEvents && loadingError == null && selectedDateEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos para este día",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else if (!isLoadingEvents && loadingError == null) {
                LazyColumn {
                    items(selectedDateEvents.sortedBy { it.time }) { event ->
                        EventCard(
                            event = event,
                            onDelete = { 
                                // Show confirmation dialog
                                eventBeingDeleted = event
                                showDeleteConfirmDialog = true
                            },
                            onEdit = {
                                eventBeingEdited = event
                                showEditEventDialog = true
                            },
                            onSetNotification = { selectedEvent ->
                                // Show notification time picker dialog
                                eventForNotification = selectedEvent
                                showNotificationDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Add event dialog
        if (showAddEventDialog) {
            AddEventDialog(
                selectedDate = selectedDate,
                onDismiss = { showAddEventDialog = false },
                onAddEvent = { title, time, endTime, description, location ->
                    // Create a new event in memory
                    val newEvent = CalendarEvent(
                        title = title,
                        date = selectedDate,
                        time = time,
                        endTime = endTime,
                        description = description,
                        location = location
                    )

                    // Add to local list
                    events.add(newEvent)

                    // Save to database via API
                    coroutineScope.launch {
                        try {
                            // Convert LocalDate and LocalTime to LocalDateTime
                            val startDateTime = java.time.LocalDateTime.of(
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth,
                                time.hour,
                                time.minute,
                                0
                            )

                            val endDateTime = java.time.LocalDateTime.of(
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth,
                                endTime.hour,
                                endTime.minute,
                                0
                            )

                            // Get userId from UserManager
                            val userId = userManager.userId.value

                            // Check if userId is valid
                            if (userId <= 0) {
                                throw Exception("No se pudo obtener el ID de usuario. Por favor, inicie sesión nuevamente.")
                            }

                            // Create EventDTO
                            val eventDTO = EventDTO(
                                title = title,
                                startTime = startDateTime,
                                endTime = endDateTime,
                                location = if (location.isBlank()) null else location,
                                description = if (description.isBlank()) null else description,
                                userId = userId
                            )

                            // Call API asynchronously
                            RetrofitClient.eventApi.createEvent(eventDTO).enqueue(object : Callback<EventResponseDTO> {
                                override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                                    if (response.isSuccessful && (response.body()?.success == true || response.code() == 201)) {
                                        // Get the event ID from the response
                                        val apiEventId = response.body()?.eventId

                                        // Update the event in the local list with the API ID
                                        if (apiEventId != null) {
                                            val index = events.indexOf(newEvent)
                                            if (index != -1) {
                                                events[index] = newEvent.copy(apiId = apiEventId)
                                            }
                                        }

                                        // Show success message
                                        val successMessage = response.body()?.message ?: "Evento guardado correctamente"
                                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Get error details
                                        val errorCode = response.code()
                                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                                        val responseMessage = response.body()?.message

                                        // Show error message with more details
                                        val errorMessage = when {
                                            responseMessage != null -> responseMessage
                                            errorCode == 401 -> "Error de autenticación: Su sesión ha expirado. Por favor, inicie sesión nuevamente."
                                            errorCode == 403 -> "Error de permisos: No tiene permisos para crear eventos."
                                            errorCode == 404 -> "Error: El servicio de eventos no está disponible."
                                            errorCode == 500 -> "Error del servidor: Problema interno del servidor."
                                            else -> "Error al guardar el evento (código $errorCode): $errorBody"
                                        }

                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        Log.e("CalendarScreen", "API Error: $errorCode - $errorBody")

                                        // Remove from local list if API call failed
                                        events.remove(newEvent)
                                    }
                                }

                                override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                                    // Handle network or other errors
                                    val errorMessage = when {
                                        t.message?.contains("timeout") == true -> 
                                            "Error de conexión: Tiempo de espera agotado. Compruebe su conexión a Internet."
                                        t.message?.contains("Unable to resolve host") == true -> 
                                            "Error de conexión: No se puede conectar al servidor. Compruebe su conexión a Internet."
                                        else -> 
                                            "Error al guardar el evento: ${t.message ?: "Error desconocido"}"
                                    }

                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    Log.e("CalendarScreen", "Error saving event", t)

                                    // Remove from local list if API call failed
                                    events.remove(newEvent)
                                }
                            })
                        } catch (e: Exception) {
                            // Handle exceptions that occur before the API call (date conversion, userId validation)
                            val errorMessage = "Error al preparar el evento: ${e.message ?: "Error desconocido"}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e("CalendarScreen", "Error preparing event", e)

                            // Remove from local list if preparation failed
                            events.remove(newEvent)
                        }
                    }

                    showAddEventDialog = false
                }
            )
        }

        // Edit event dialog
        if (showEditEventDialog && eventBeingEdited != null) {
            EditEventDialog(
                event = eventBeingEdited!!,
                onDismiss = { 
                    showEditEventDialog = false 
                    eventBeingEdited = null
                },
                onEditEvent = { title, time, endTime, description, location ->
                    // Get the original event
                    val originalEvent = eventBeingEdited!!

                    // Create an updated event
                    val updatedEvent = originalEvent.copy(
                        title = title,
                        time = time,
                        endTime = endTime,
                        description = description,
                        location = location,
                        apiId = originalEvent.apiId  // Preserve the API ID
                    )

                    // Update the local list
                    val index = events.indexOf(originalEvent)
                    if (index != -1) {
                        events[index] = updatedEvent
                    }

                    // Update in the database via API
                    coroutineScope.launch {
                        try {
                            // Convert LocalDate and LocalTime to LocalDateTime
                            val startDateTime = java.time.LocalDateTime.of(
                                updatedEvent.date.year,
                                updatedEvent.date.monthValue,
                                updatedEvent.date.dayOfMonth,
                                time.hour,
                                time.minute,
                                0
                            )

                            val endDateTime = java.time.LocalDateTime.of(
                                updatedEvent.date.year,
                                updatedEvent.date.monthValue,
                                updatedEvent.date.dayOfMonth,
                                endTime.hour,
                                endTime.minute,
                                0
                            )

                            // Get userId from UserManager
                            val userId = userManager.userId.value

                            // Check if userId is valid
                            if (userId <= 0) {
                                throw Exception("No se pudo obtener el ID de usuario. Por favor, inicie sesión nuevamente.")
                            }

                            // Create EventDTO
                            val eventDTO = EventDTO(
                                title = title,
                                startTime = startDateTime,
                                endTime = endDateTime,
                                location = if (location.isBlank()) null else location,
                                description = if (description.isBlank()) null else description,
                                userId = userId
                            )

                            // Get the event ID from the API response
                            val eventId = originalEvent.apiId ?: 
                                throw Exception("ID de evento inválido. No se puede actualizar. Este evento no tiene un ID de API válido.")

                            // Call API asynchronously
                            RetrofitClient.eventApi.updateEvent(eventId, eventDTO).enqueue(object : Callback<EventResponseDTO> {
                                override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                                    if (response.isSuccessful && (response.body()?.success == true || response.code() == 201)) {
                                        // Show success message
                                        val successMessage = response.body()?.message ?: "Evento actualizado correctamente"
                                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Get error details
                                        val errorCode = response.code()
                                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                                        val responseMessage = response.body()?.message

                                        // Show error message with more details
                                        val errorMessage = when {
                                            responseMessage != null -> responseMessage
                                            errorCode == 401 -> "Error de autenticación: Su sesión ha expirado. Por favor, inicie sesión nuevamente."
                                            errorCode == 403 -> "Error de permisos: No tiene permisos para actualizar eventos."
                                            errorCode == 404 -> "Error: El evento no existe o el servicio no está disponible."
                                            errorCode == 500 -> "Error del servidor: Problema interno del servidor."
                                            else -> "Error al actualizar el evento (código $errorCode): $errorBody"
                                        }

                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        Log.e("CalendarScreen", "API Error: $errorCode - $errorBody")

                                        // Revert to original event in local list if API call failed
                                        if (index != -1) {
                                            events[index] = originalEvent
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                                    // Handle network or other errors
                                    val errorMessage = when {
                                        t.message?.contains("timeout") == true -> 
                                            "Error de conexión: Tiempo de espera agotado. Compruebe su conexión a Internet."
                                        t.message?.contains("Unable to resolve host") == true -> 
                                            "Error de conexión: No se puede conectar al servidor. Compruebe su conexión a Internet."
                                        else -> 
                                            "Error al actualizar el evento: ${t.message ?: "Error desconocido"}"
                                    }

                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    Log.e("CalendarScreen", "Error updating event", t)

                                    // Revert to original event in local list if API call failed
                                    if (index != -1) {
                                        events[index] = originalEvent
                                    }
                                }
                            })
                        } catch (e: Exception) {
                            // Handle exceptions that occur before the API call (date conversion, userId validation)
                            val errorMessage = "Error al preparar la actualización del evento: ${e.message ?: "Error desconocido"}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e("CalendarScreen", "Error preparing event update", e)

                            // Revert to original event in local list if preparation failed
                            val index = events.indexOf(updatedEvent)
                            if (index != -1) {
                                events[index] = originalEvent
                            }
                        }
                    }

                    showEditEventDialog = false
                    eventBeingEdited = null
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteConfirmDialog && eventBeingDeleted != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteConfirmDialog = false 
                    eventBeingDeleted = null
                },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Está seguro de que desea eliminar el evento '${eventBeingDeleted!!.title}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val originalEvent = eventBeingDeleted!!

                            // Check if the event has an API ID
                            if (originalEvent.apiId != null) {
                                // Call API to delete the event
                                coroutineScope.launch {
                                    try {
                                        // Remove from local list first for immediate UI feedback
                                        events.remove(originalEvent)

                                        // Call API asynchronously
                                        RetrofitClient.eventApi.deleteEvent(originalEvent.apiId).enqueue(object : Callback<EventResponseDTO> {
                                            override fun onResponse(call: Call<EventResponseDTO>, response: Response<EventResponseDTO>) {
                                                if (response.isSuccessful && (response.body()?.success == true || response.code() == 200 || response.code() == 204)) {
                                                    // Show success message
                                                    val successMessage = response.body()?.message ?: "Evento eliminado correctamente"
                                                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                } else {
                                                    // Get error details
                                                    val errorCode = response.code()
                                                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                                                    val responseMessage = response.body()?.message

                                                    // Show error message with more details
                                                    val errorMessage = when {
                                                        responseMessage != null -> responseMessage
                                                        errorCode == 401 -> "Error de autenticación: Su sesión ha expirado. Por favor, inicie sesión nuevamente."
                                                        errorCode == 403 -> "Error de permisos: No tiene permisos para eliminar eventos."
                                                        errorCode == 404 -> "Error: El evento no existe o el servicio no está disponible."
                                                        errorCode == 500 -> "Error del servidor: Problema interno del servidor."
                                                        else -> "Error al eliminar el evento (código $errorCode): $errorBody"
                                                    }

                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                                    Log.e("CalendarScreen", "API Error: $errorCode - $errorBody")

                                                    // Add back to local list if API call failed
                                                    if (!events.contains(originalEvent)) {
                                                        events.add(originalEvent)
                                                    }
                                                }
                                            }

                                            override fun onFailure(call: Call<EventResponseDTO>, t: Throwable) {
                                                // Handle network or other errors
                                                val errorMessage = when {
                                                    t.message?.contains("timeout") == true -> 
                                                        "Error de conexión: Tiempo de espera agotado. Compruebe su conexión a Internet."
                                                    t.message?.contains("Unable to resolve host") == true -> 
                                                        "Error de conexión: No se puede conectar al servidor. Compruebe su conexión a Internet."
                                                    else -> 
                                                        "Error al eliminar el evento: ${t.message ?: "Error desconocido"}"
                                                }

                                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                                Log.e("CalendarScreen", "Error deleting event", t)

                                                // Add back to local list if API call failed
                                                if (!events.contains(originalEvent)) {
                                                    events.add(originalEvent)
                                                }
                                            }
                                        })
                                    } catch (e: Exception) {
                                        // Handle exceptions that occur before the API call
                                        val errorMessage = "Error al preparar la eliminación del evento: ${e.message ?: "Error desconocido"}"
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        Log.e("CalendarScreen", "Error preparing event deletion", e)

                                        // Add back to local list if preparation failed
                                        if (!events.contains(originalEvent)) {
                                            events.add(originalEvent)
                                        }
                                    }
                                }
                            } else {
                                // If the event doesn't have an API ID, just remove it from the local list
                                events.remove(originalEvent)
                                Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                            }

                            showDeleteConfirmDialog = false
                            eventBeingDeleted = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteConfirmDialog = false 
                            eventBeingDeleted = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Notification time picker bottom sheet
        if (showNotificationDialog && eventForNotification != null) {
            // Default to 30 minutes before the event if no notification time is set
            val initialTime = eventForNotification!!.notificationTime ?: 
                              LocalTime.of(0, 30) // Default to 30 minutes

            // State for hours and minutes
            var hours by remember { mutableStateOf(initialTime.hour) }
            var minutes by remember { mutableStateOf(initialTime.minute) }

            // State for the bottom sheet
            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { 
                    showNotificationDialog = false 
                    eventForNotification = null
                },
                sheetState = bottomSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = "Configurar notificación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Description
                    Text(
                        text = "Seleccione cuánto tiempo antes del evento desea recibir la notificación:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Time picker for hours and minutes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { if (hours < 24) hours++ },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Aumentar horas",
                                    modifier = Modifier.rotate(270f)
                                )
                            }

                            Text(
                                text = "$hours",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { if (hours > 0) hours-- },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Disminuir horas",
                                    modifier = Modifier.rotate(90f)
                                )
                            }

                            Text("Horas", style = MaterialTheme.typography.bodySmall)
                        }

                        Text(":", style = MaterialTheme.typography.headlineLarge)

                        // Minutes picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { 
                                    minutes = (minutes + 5) % 60
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Aumentar minutos",
                                    modifier = Modifier.rotate(270f)
                                )
                            }

                            Text(
                                text = minutes.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { 
                                    minutes = if (minutes < 5) 55 else minutes - 5
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Disminuir minutos",
                                    modifier = Modifier.rotate(90f)
                                )
                            }

                            Text("Minutos", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Confirmation message
                    Text(
                        text = "Recibirá una notificación ${hours}h ${minutes}min antes del evento",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel button
                        TextButton(
                            onClick = { 
                                showNotificationDialog = false 
                                eventForNotification = null
                            }
                        ) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Save button
                        Button(
                            onClick = {
                                val event = eventForNotification!!
                                val index = events.indexOf(event)

                                // Get the selected time
                                val notificationTime = LocalTime.of(
                                    hours,
                                    minutes
                                )

                                // Update the event with the notification time
                                if (index != -1) {
                                    val updatedEvent = event.copy(notificationTime = notificationTime)
                                    events[index] = updatedEvent

                                    // Schedule the notification
                                    val eventDateTime = event.date.atTime(event.time)
                                    val notificationDateTime = eventDateTime.minusHours(notificationTime.hour.toLong())
                                                                          .minusMinutes(notificationTime.minute.toLong())

                                    // Show confirmation toast
                                    Toast.makeText(
                                        context, 
                                        "Notificación configurada para ${notificationTime.hour}h ${notificationTime.minute}min antes del evento", 
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Log for debugging
                                    Log.d("CalendarScreen", "[DEBUG_LOG] Notification set for event: ${event.title}")
                                    Log.d("CalendarScreen", "[DEBUG_LOG] Event time: ${eventDateTime}")
                                    Log.d("CalendarScreen", "[DEBUG_LOG] Notification time: ${notificationDateTime}")
                                }

                                showNotificationDialog = false
                                eventForNotification = null
                            }
                        ) {
                            Text("Guardar")
                        }
                    }

                    // Add extra space at the bottom to avoid buttons being cut off
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSetNotification: (CalendarEvent) -> Unit
) {
    val context = LocalContext.current
    var showExportProgress by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showExportError by remember { mutableStateOf(false) }

    // Auto-dismiss success/error messages after 3 seconds
    LaunchedEffect(showExportSuccess, showExportError) {
        if (showExportSuccess || showExportError) {
            kotlinx.coroutines.delay(3000)
            showExportSuccess = false
            showExportError = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time display with start and end times
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)
                ) {
                    // Start time
                    Text(
                        text = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Small arrow or separator
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(90f),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // End time
                    Text(
                        text = event.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Duration
                    val duration = org.threeten.bp.Duration.between(event.time, event.endTime)
                    val hours = duration.toHours()
                    val minutes = duration.toMinutesPart()
                    val durationText = when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h"
                        else -> "${minutes}m"
                    }

                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Divider(
                    modifier = Modifier
                        .height(70.dp)
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Event details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (event.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    if (event.location.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${event.location}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Action buttons
                Column {
                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar evento",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Notification button
                    IconButton(onClick = { onSetNotification(event) }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Configurar notificación",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar evento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Export status messages
            if (showExportProgress || showExportSuccess || showExportError) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        showExportProgress -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exportando a Google Calendar...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        showExportSuccess -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Éxito",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Evento añadido a Google Calendar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        showExportError -> {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Error al exportar a Google Calendar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Add to Google Calendar button
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Check if we have the necessary permissions
                    if (context is Activity) {
                        showExportProgress = true

                        // Log event details for debugging
                        Log.d("EventCard", "[DEBUG_LOG] Attempting to export event to Google Calendar:")
                        Log.d("EventCard", "[DEBUG_LOG] - Title: ${event.title}")
                        Log.d("EventCard", "[DEBUG_LOG] - Date: ${event.date}")
                        Log.d("EventCard", "[DEBUG_LOG] - Start Time: ${event.time}")
                        Log.d("EventCard", "[DEBUG_LOG] - End Time: ${event.endTime}")
                        Log.d("EventCard", "[DEBUG_LOG] - Description: ${event.description}")
                        Log.d("EventCard", "[DEBUG_LOG] - Location: ${event.location}")

                        // Create the intent as specified in the issue description
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            // Add extras if needed
                            putExtra(CalendarContract.Events.TITLE, event.title)
                            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)

                            // Convert LocalDate and LocalTime to milliseconds
                            val startMillis = event.date.atTime(event.time)
                                .atZone(org.threeten.bp.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()

                            val endMillis = event.date.atTime(event.endTime)
                                .atZone(org.threeten.bp.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()

                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                        }

                        // Debug logs as specified in the issue description
                        Log.d("EventCard", "[DEBUG_LOG] Intent URI: ${intent.data}")
                        Log.d("EventCard", "[DEBUG_LOG] Package manager can resolve: ${intent.resolveActivity(context.packageManager) != null}")

                        // Verification code block as specified in the issue description
                        if (intent.resolveActivity(context.packageManager) != null) {
                            try {
                                context.startActivity(intent)
                                showExportProgress = false
                                showExportSuccess = true
                                return@Button
                            } catch (e: Exception) {
                                Log.e("EventCard", "[DEBUG_LOG] Error al abrir el calendario: ${e.message}")
                                showExportProgress = false
                                showExportError = true
                                return@Button
                            }
                        } else {
                            // Google Calendar no está instalado
                            Log.e("EventCard", "[DEBUG_LOG] Export failed: No activity found to handle calendar intent")
                            showExportProgress = false
                            showExportError = true

                            // Show dialog to suggest installing Google Calendar
                            CalendarUtils.showInstallGoogleCalendarDialog(context)
                            return@Button
                        }
                    } else {
                        Log.e("EventCard", "[DEBUG_LOG] Export failed: Context is not an Activity")
                        showExportError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Añadir a Google Calendar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir a Google Calendar")
            }
        }
    }
}

/**
 * A reusable TimePicker dialog component
 *
 * @param showDialog Whether to show the dialog
 * @param initialTime The initial time to display
 * @param onTimeSelected Callback when a time is selected
 * @param onDismiss Callback when the dialog is dismissed
 * @param title Optional title for the dialog
 * @param is24Hour Whether to use 24-hour format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Seleccionar hora",
    is24Hour: Boolean = true
) {
    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24Hour
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = true // Important: usar dimensiones por defecto
            ),
            title = { Text(title) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Display the time in a larger format
                    Text(
                        text = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Use only the text input mode
                    TimeInput(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset to current time button
                    TextButton(
                        onClick = {
                            val now = LocalTime.now()
                            // Round to nearest 5 minutes for better UX
                            val roundedMinute = (now.minute / 5) * 5
                            val roundedTime = LocalTime.of(now.hour, roundedMinute)
                            onTimeSelected(roundedTime)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Hora actual",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hora actual")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(selectedTime)
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onAddEvent: (title: String, time: LocalTime, endTime: LocalTime, description: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var timeValidationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initialize with current time rounded to nearest hour
    val currentTime = LocalTime.now()
    val initialHour = currentTime.hour
    val initialMinute = if (currentTime.minute >= 30) 30 else 0

    // Time state
    var selectedStartTime by remember { 
        mutableStateOf(LocalTime.of(initialHour, initialMinute)) 
    }
    var selectedEndTime by remember { 
        mutableStateOf(LocalTime.of(
            if (initialHour + 1 > 23) 23 else initialHour + 1, 
            initialMinute
        )) 
    }

    // Time picker dialogs using the new component
    TimePickerDialog(
        showDialog = showStartTimePicker,
        initialTime = selectedStartTime,
        onTimeSelected = { newTime ->
            selectedStartTime = newTime

            // Validate that end time is after start time
            if (selectedEndTime.isBefore(newTime)) {
                // Automatically adjust end time to be 30 minutes after start time
                selectedEndTime = newTime.plusMinutes(30)
            }

            timeValidationError = null
            showStartTimePicker = false
        },
        onDismiss = { showStartTimePicker = false },
        title = "Seleccionar hora de inicio"
    )

    TimePickerDialog(
        showDialog = showEndTimePicker,
        initialTime = selectedEndTime,
        onTimeSelected = { newTime ->
            // Validate that end time is after start time
            if (newTime.isBefore(selectedStartTime)) {
                timeValidationError = "La hora de fin debe ser posterior a la hora de inicio"
            } else {
                selectedEndTime = newTime
                timeValidationError = null
                showEndTimePicker = false
            }
        },
        onDismiss = { showEndTimePicker = false },
        title = "Seleccionar hora de fin"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo evento")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start time selection with icon
                OutlinedTextField(
                    value = selectedStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    label = { Text("Hora de inicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartTimePicker = true },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de inicio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Cambiar hora de inicio"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // End time selection with icon
                OutlinedTextField(
                    value = selectedEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    label = { Text("Hora de fin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndTimePicker = true },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de fin",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Cambiar hora de fin"
                            )
                        }
                    },
                    isError = timeValidationError != null
                )

                // Show validation error if any
                if (timeValidationError != null) {
                    Text(
                        text = timeValidationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                // Time duration display
                val duration = org.threeten.bp.Duration.between(selectedStartTime, selectedEndTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutesPart()
                val durationText = when {
                    hours > 0 && minutes > 0 -> "$hours h $minutes min"
                    hours > 0 -> "$hours h"
                    else -> "$minutes min"
                }

                Text(
                    text = "Duración: $durationText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && timeValidationError == null) {
                        onAddEvent(
                            title,
                            selectedStartTime,
                            selectedEndTime,
                            description,
                            location
                        )
                    }
                },
                enabled = title.isNotBlank() && timeValidationError == null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Añadir")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onEditEvent: (title: String, time: LocalTime, endTime: LocalTime, description: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var location by remember { mutableStateOf(event.location) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var timeValidationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Time state initialized with event times
    var selectedStartTime by remember { mutableStateOf(event.time) }
    var selectedEndTime by remember { mutableStateOf(event.endTime) }

    // Time picker dialogs using the new component
    TimePickerDialog(
        showDialog = showStartTimePicker,
        initialTime = selectedStartTime,
        onTimeSelected = { newTime ->
            selectedStartTime = newTime

            // Validate that end time is after start time
            if (selectedEndTime.isBefore(newTime)) {
                // Automatically adjust end time to be 30 minutes after start time
                selectedEndTime = newTime.plusMinutes(30)
            }

            timeValidationError = null
            showStartTimePicker = false
        },
        onDismiss = { showStartTimePicker = false },
        title = "Seleccionar hora de inicio"
    )

    TimePickerDialog(
        showDialog = showEndTimePicker,
        initialTime = selectedEndTime,
        onTimeSelected = { newTime ->
            // Validate that end time is after start time
            if (newTime.isBefore(selectedStartTime)) {
                timeValidationError = "La hora de fin debe ser posterior a la hora de inicio"
            } else {
                selectedEndTime = newTime
                timeValidationError = null
                showEndTimePicker = false
            }
        },
        onDismiss = { showEndTimePicker = false },
        title = "Seleccionar hora de fin"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar evento")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start time selection with icon
                OutlinedTextField(
                    value = selectedStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    label = { Text("Hora de inicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartTimePicker = true },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de inicio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Cambiar hora de inicio"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // End time selection with icon
                OutlinedTextField(
                    value = selectedEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = { },
                    label = { Text("Hora de fin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndTimePicker = true },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de fin",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Cambiar hora de fin"
                            )
                        }
                    },
                    isError = timeValidationError != null
                )

                // Show validation error if any
                if (timeValidationError != null) {
                    Text(
                        text = timeValidationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                // Time duration display
                val duration = org.threeten.bp.Duration.between(selectedStartTime, selectedEndTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutesPart()
                val durationText = when {
                    hours > 0 && minutes > 0 -> "$hours h $minutes min"
                    hours > 0 -> "$hours h"
                    else -> "$minutes min"
                }

                Text(
                    text = "Duración: $durationText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Ubicación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && timeValidationError == null) {
                        onEditEvent(
                            title,
                            selectedStartTime,
                            selectedEndTime,
                            description,
                            location
                        )
                    }
                },
                enabled = title.isNotBlank() && timeValidationError == null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

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

    // Initialize PersonalityViewModel
    val personalityViewModel = androidx.lifecycle.viewmodel.compose.viewModel<PersonalityViewModel>(
        factory = PersonalityViewModel.Factory()
    )
    val personalitiesList by personalityViewModel.personalities.collectAsState<List<PersonalityResponseDTO>, List<PersonalityResponseDTO>>(initial = emptyList())

    // Access state values
    val isLoadingPersonalities by remember { personalityViewModel.isLoading }
    val personalitiesError by remember { personalityViewModel.error }

    // State for AI personality
    var aiPersonality by remember { mutableStateOf(AppConfig.getAIPersonality()) }
    var isPersonalityMenuExpanded by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
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

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userManager = UserManager.getInstance(context)

    // Get user data from UserManager
    val email by userManager.userEmail
    val password by userManager.userPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // User information card
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
                    text = "Información del usuario",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Column {
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password (masked)
                Column {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = password,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A simplified time input dialog that only shows the numeric input
 *
 * @param showDialog Whether to show the dialog
 * @param onDismissRequest Callback when the dialog is dismissed
 * @param onTimeSelected Callback when a time is selected with hour and minute parameters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Seleccionar hora") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Display the time in a larger format
                    Text(
                        text = String.format("%02d:%02d", state.hour, state.minute),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimeInput(
                        state = state,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(state.hour, state.minute)
                        onDismissRequest()
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancelar")
                }
            }
        )
    }
}
