package dam.tfg.blinky.presentation.screens

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import dam.tfg.blinky.domain.model.CalendarEvent
import dam.tfg.blinky.presentation.viewmodel.CalendarViewModel
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Calendar screen that displays events and allows adding, editing, and deleting events
 */
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val context = LocalContext.current

    // State from ViewModel
    val events = viewModel.events
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Local UI state
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showEditEventDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var newlyCreatedEventId by remember { mutableStateOf<Long?>(null) }

    // Tutorial state
    val tutorialShown by viewModel.tutorialShown
    var showTutorial by remember { mutableStateOf(!tutorialShown) }
    var fabBounds by remember { mutableStateOf<Rect?>(null) }

    // Pull to refresh state
    var refreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)

    // Format for displaying dates
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))

    // Load events when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadUserEvents()
    }

    // Reset refreshing state when loading completes
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            refreshing = false
        }
    }

    // Handle newly created event - show edit dialog when event is loaded
    LaunchedEffect(newlyCreatedEventId, events) {
        if (newlyCreatedEventId != null && !isLoading) {
            // Find the newly created event by ID
            events.find { it.apiId == newlyCreatedEventId }?.let { newEvent ->
                selectedEvent = newEvent
                showEditEventDialog = true
                // Reset the ID after finding the event
                newlyCreatedEventId = null
            }
        }
    }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
            // Calendar header with month and year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.setSelectedDate(selectedDate.minusMonths(1))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior"
                    )
                }

                Text(
                    text = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                        .replaceFirstChar { it.uppercase() } + " " + selectedDate.year,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    viewModel.setSelectedDate(selectedDate.plusMonths(1))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.setSelectedDate(selectedDate.minusWeeks(1))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Semana anterior"
                    )
                }

                // Format for displaying short dates (day and month)
                val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
                val firstDayOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val lastDayOfWeek = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                Text(
                    text = "${firstDayOfWeek.format(shortDateFormatter)} - ${lastDayOfWeek.format(shortDateFormatter)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    viewModel.setSelectedDate(selectedDate.plusWeeks(1))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Semana siguiente"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in DayOfWeek.values()) {
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                            .replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid - show only current week
            val firstDayOfGrid = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val lastDayOfGrid = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            var currentDate = firstDayOfGrid
            while (currentDate.isBefore(lastDayOfGrid) || currentDate.isEqual(lastDayOfGrid)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0 until 7) {
                        if (currentDate.isBefore(lastDayOfGrid) || currentDate.isEqual(lastDayOfGrid)) {
                            val date = currentDate
                            val isCurrentMonth = date.month == selectedDate.month
                            val isToday = date.isEqual(LocalDate.now())
                            val isSelected = date.isEqual(selectedDate)

                            // Events for this day
                            val eventsForDay = events.filter { it.date.isEqual(date) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> dam.tfg.blinky.ui.theme.GoogleBlueLight
                                            isToday && !selectedDate.isEqual(LocalDate.now()) -> dam.tfg.blinky.ui.theme.GoogleBlueSoft
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        viewModel.setSelectedDate(date)
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.background
                                            isToday -> Color.White
                                            isCurrentMonth -> MaterialTheme.colorScheme.onBackground
                                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        },
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                    )

                                    if (eventsForDay.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else dam.tfg.blinky.ui.theme.GoogleBlue,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }

                            currentDate = currentDate.plusDays(1)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Events for selected date
            Text(
                text = "Eventos para ${selectedDate.format(dateFormatter)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SwipeRefresh for pull-to-refresh functionality
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    refreshing = true
                    viewModel.loadUserEvents()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Content inside SwipeRefresh
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Loading indicator
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    // Error message
                    else if (error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = error ?: "Error desconocido",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.loadUserEvents() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reintentar"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Reintentar")
                                }
                            }
                        }
                    }
                    // Event list
                    else {
                        val eventsForSelectedDate = events.filter { it.date.isEqual(selectedDate) }

                        if (eventsForSelectedDate.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay eventos para este día",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(eventsForSelectedDate.sortedBy { it.time }) { event ->
                                    EventCard(
                                        event = event,
                                        onDelete = {
                                            selectedEvent = event
                                            showDeleteConfirmation = true
                                        },
                                        onEdit = {
                                            selectedEvent = event
                                            showEditEventDialog = true
                                        },
                                        onAddToCalendar = { event ->
                                            // Add to device calendar
                                            val intent = Intent(Intent.ACTION_INSERT)
                                                .setData(CalendarContract.Events.CONTENT_URI)
                                                .putExtra(CalendarContract.Events.TITLE, event.title)
                                                .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                                                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                                                .putExtra(
                                                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                                    event.date.atTime(event.time).toEpochSecond(org.threeten.bp.ZoneOffset.UTC) * 1000
                                                )
                                                .putExtra(
                                                    CalendarContract.EXTRA_EVENT_END_TIME,
                                                    event.date.atTime(event.endTime).toEpochSecond(org.threeten.bp.ZoneOffset.UTC) * 1000
                                                )

                                            try {
                                                (context as? Activity)?.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "No se pudo abrir el calendario: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e("CalendarScreen", "Error opening calendar", e)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

            // FAB positioned lower and more to the left
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 32.dp)
                    .size(70.dp) // Fixed size
                    .onGloballyPositioned { coordinates ->
                        // Store the FAB bounds for the tutorial spotlight
                        fabBounds = coordinates.boundsInWindow()
                    },
                shape = RoundedCornerShape(16.dp), // Consistent shape with other FABs
                containerColor = dam.tfg.blinky.ui.theme.GoogleBlueLight
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evento",
                    modifier = Modifier.size(32.dp), // Fixed icon size
                    tint = Color.White
                )
            }

            // Tutorial overlay
            TutorialOverlay(
                visible = showTutorial,
                targetBounds = fabBounds,
                message = "Añadir nuevo evento al calendario",
                onDismiss = {
                    showTutorial = false
                    viewModel.markTutorialAsShown()
                }
            )
        }
    }

    // Add event dialog
    if (showAddEventDialog) {
        AddEventDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { title, time, endTime, description, location, notificationTime, date ->
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val startTimeStr = time.format(DateTimeFormatter.ISO_TIME)
                val endTimeStr = endTime.format(DateTimeFormatter.ISO_TIME)

                viewModel.createEvent(
                    title = title,
                    date = dateStr,
                    startTime = startTimeStr,
                    endTime = endTimeStr,
                    description = description,
                    location = location,
                    onSuccess = { eventId ->
                        showAddEventDialog = false
                        // Store the newly created event ID to find it after events are loaded
                        newlyCreatedEventId = eventId
                    },
                    onError = { errorMessage ->
                        showAddEventDialog = false
                    }
                )
            }
        )
    }

    // Edit event dialog
    if (showEditEventDialog && selectedEvent != null) {
        EditEventDialog(
            event = selectedEvent!!,
            onDismiss = { showEditEventDialog = false },
            onEditEvent = { title, time, endTime, description, location, notificationTime ->
                val eventId = selectedEvent?.apiId ?: return@EditEventDialog
                val dateStr = selectedEvent?.date?.format(DateTimeFormatter.ISO_DATE) ?: return@EditEventDialog
                val startTimeStr = time.format(DateTimeFormatter.ISO_TIME)
                val endTimeStr = endTime.format(DateTimeFormatter.ISO_TIME)

                viewModel.updateEvent(
                    eventId = eventId,
                    title = title,
                    date = dateStr,
                    startTime = startTimeStr,
                    endTime = endTimeStr,
                    description = description,
                    location = location,
                    onSuccess = {
                        showEditEventDialog = false
                    },
                    onError = { errorMessage ->
                        showEditEventDialog = false
                    }
                )
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation && selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Está seguro de que desea eliminar el evento '${selectedEvent?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val eventId = selectedEvent?.apiId ?: return@TextButton

                        viewModel.deleteEvent(
                            eventId = eventId,
                            onSuccess = {
                                showDeleteConfirmation = false
                            },
                            onError = { errorMessage ->
                                showDeleteConfirmation = false
                            }
                        )
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card that displays an event
 */
@Composable
fun EventCard(
    event: CalendarEvent,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddToCalendar: (CalendarEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar evento",
                            tint = dam.tfg.blinky.ui.theme.GoogleBlue
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar evento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Hora",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${event.time.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onAddToCalendar(event) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Añadir al calendario"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir al calendario")
            }
        }
    }
}

/**
 * Dialog for adding a new event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onAddEvent: (title: String, time: LocalTime, endTime: LocalTime, description: String, location: String, notificationTime: LocalTime?, date: LocalDate) -> Unit,
    initialTitle: String = "",
    initialTime: LocalTime = LocalTime.now(),
    initialEndTime: LocalTime = LocalTime.now().plusHours(1),
    initialDescription: String = "",
    initialLocation: String = ""
) {
    var title by remember { mutableStateOf(initialTitle) }
    var time by remember { mutableStateOf(initialTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }
    var description by remember { mutableStateOf(initialDescription) }
    var location by remember { mutableStateOf(initialLocation) }
    var date by remember { mutableStateOf(selectedDate) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var notificationTime by remember { mutableStateOf(LocalTime.of(0, 15)) } // Default 15 minutes before
    var showDatePicker by remember { mutableStateOf(false) }

    // Predefined notification time options
    val notificationOptions = listOf(
        "5 minutos antes" to LocalTime.of(0, 5),
        "10 minutos antes" to LocalTime.of(0, 10),
        "15 minutos antes" to LocalTime.of(0, 15),
        "30 minutos antes" to LocalTime.of(0, 30),
        "1 hora antes" to LocalTime.of(1, 0),
        "2 horas antes" to LocalTime.of(2, 0),
        "1 día antes" to LocalTime.of(23, 59),
        "Personalizado" to null
    )

    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showTimePickerNotification by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir evento") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Día: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")

                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Seleccionar día"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora inicio: ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}")

                    IconButton(onClick = { showTimePickerStart = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de inicio"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora fin: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")

                    IconButton(onClick = { showTimePickerEnd = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de fin"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notification row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificación: ")
                        NotificationTimeSelector(
                            notificationEnabled = notificationEnabled,
                            notificationTime = notificationTime,
                            notificationOptions = notificationOptions,
                            onNotificationTimeSelected = { newTime ->
                                notificationTime = newTime
                            },
                            onShowTimePicker = {
                                showTimePickerNotification = true
                            }
                        )
                    }

                    IconButton(onClick = { notificationEnabled = !notificationEnabled }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = if (notificationEnabled) "Desactivar notificación" else "Activar notificación",
                            tint = if (notificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddEvent(title, time, endTime, description, location, if (notificationEnabled) notificationTime else null, date)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = dam.tfg.blinky.ui.theme.GoogleBlue)
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showTimePickerStart) {
        TimePickerDialog(
            showDialog = true,
            initialTime = time,
            onTimeSelected = { newTime ->
                time = newTime
                // If end time is before start time, set end time to start time + 1 hour
                if (endTime.isBefore(newTime)) {
                    endTime = newTime.plusHours(1)
                }
                showTimePickerStart = false
            },
            onDismiss = { showTimePickerStart = false }
        )
    }

    if (showTimePickerEnd) {
        TimePickerDialog(
            showDialog = true,
            initialTime = endTime,
            onTimeSelected = { newTime ->
                // Ensure end time is after start time
                if (newTime.isAfter(time)) {
                    endTime = newTime
                } else {
                    // Toast removed as per requirement
                }
                showTimePickerEnd = false
            },
            onDismiss = { showTimePickerEnd = false }
        )
    }

    if (showTimePickerNotification) {
        TimePickerDialog(
            showDialog = true,
            initialTime = notificationTime,
            onTimeSelected = { newTime ->
                notificationTime = newTime
                showTimePickerNotification = false
            },
            onDismiss = { showTimePickerNotification = false },
            title = "Tiempo de notificación antes del evento"
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            showDialog = true,
            initialDate = date,
            onDateSelected = { newDate ->
                date = newDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Dialog for editing an existing event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onEditEvent: (title: String, time: LocalTime, endTime: LocalTime, description: String, location: String, notificationTime: LocalTime?) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var time by remember { mutableStateOf(event.time) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var description by remember { mutableStateOf(event.description) }
    var location by remember { mutableStateOf(event.location) }
    var notificationEnabled by remember { mutableStateOf(event.notificationTime != null) }
    var notificationTime by remember { mutableStateOf(event.notificationTime ?: LocalTime.of(0, 15)) } // Default 15 minutes before

    // Predefined notification time options
    val notificationOptions = listOf(
        "5 minutos antes" to LocalTime.of(0, 5),
        "10 minutos antes" to LocalTime.of(0, 10),
        "15 minutos antes" to LocalTime.of(0, 15),
        "30 minutos antes" to LocalTime.of(0, 30),
        "1 hora antes" to LocalTime.of(1, 0),
        "2 horas antes" to LocalTime.of(2, 0),
        "1 día antes" to LocalTime.of(23, 59),
        "Personalizado" to null
    )

    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showTimePickerNotification by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar evento") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora inicio: ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}")

                    IconButton(onClick = { showTimePickerStart = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de inicio"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora fin: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")

                    IconButton(onClick = { showTimePickerEnd = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora de fin"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notification row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificación: ")
                        NotificationTimeSelector(
                            notificationEnabled = notificationEnabled,
                            notificationTime = notificationTime,
                            notificationOptions = notificationOptions,
                            onNotificationTimeSelected = { newTime ->
                                notificationTime = newTime
                            },
                            onShowTimePicker = {
                                showTimePickerNotification = true
                            }
                        )
                    }

                    IconButton(onClick = { notificationEnabled = !notificationEnabled }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = if (notificationEnabled) "Desactivar notificación" else "Activar notificación",
                            tint = if (notificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        focusedLabelColor = dam.tfg.blinky.ui.theme.GoogleBlue,
                        cursorColor = dam.tfg.blinky.ui.theme.GoogleBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onEditEvent(title, time, endTime, description, location, if (notificationEnabled) notificationTime else null)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = dam.tfg.blinky.ui.theme.GoogleBlue)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showTimePickerStart) {
        TimePickerDialog(
            showDialog = true,
            initialTime = time,
            onTimeSelected = { newTime ->
                time = newTime
                // If end time is before start time, set end time to start time + 1 hour
                if (endTime.isBefore(newTime)) {
                    endTime = newTime.plusHours(1)
                }
                showTimePickerStart = false
            },
            onDismiss = { showTimePickerStart = false }
        )
    }

    if (showTimePickerEnd) {
        TimePickerDialog(
            showDialog = true,
            initialTime = endTime,
            onTimeSelected = { newTime ->
                // Ensure end time is after start time
                if (newTime.isAfter(time)) {
                    endTime = newTime
                } else {
                    // Toast removed as per requirement
                }
                showTimePickerEnd = false
            },
            onDismiss = { showTimePickerEnd = false }
        )
    }

    if (showTimePickerNotification) {
        TimePickerDialog(
            showDialog = true,
            initialTime = notificationTime,
            onTimeSelected = { newTime ->
                notificationTime = newTime
                showTimePickerNotification = false
            },
            onDismiss = { showTimePickerNotification = false },
            title = "Tiempo de notificación antes del evento"
        )
    }
}

/**
 * Composable for notification time selection
 */
@Composable
fun NotificationTimeSelector(
    notificationEnabled: Boolean,
    notificationTime: LocalTime,
    notificationOptions: List<Pair<String, LocalTime?>>,
    onNotificationTimeSelected: (LocalTime) -> Unit,
    onShowTimePicker: () -> Unit
) {
    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }

    // Find the current selected option label
    val selectedOptionLabel = remember(notificationTime) {
        notificationOptions.find { it.second == notificationTime }?.first ?: "Personalizado"
    }

    if (notificationEnabled) {
        // Dropdown menu for notification time selection
        Box {
            OutlinedTextField(
                value = selectedOptionLabel,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.width(180.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar tiempo de notificación"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary
                )
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                notificationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.first) },
                        onClick = {
                            if (option.second == null) {
                                // Custom time option selected, show time picker
                                onShowTimePicker()
                            } else {
                                onNotificationTimeSelected(option.second!!)
                            }
                            expanded = false
                        }
                    )
                }
            }
        }
    } else {
        Text(
            text = "Desactivada",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Tutorial overlay with spotlight effect - minimalist design with icons
 */
@Composable
fun TutorialOverlay(
    visible: Boolean,
    targetBounds: Rect?,
    message: String,
    onDismiss: () -> Unit
) {
    if (visible && targetBounds != null) {
        // Remember theme colors outside of the drawWithContent block
        val primaryColor = MaterialTheme.colorScheme.primary
        val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)

        // Get screen size to make calculations responsive
        val density = LocalDensity.current
        val screenWidth = with(density) { 
            androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx() 
        }
        val screenHeight = with(density) { 
            androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() 
        }

        // Calculate a responsive circle size based on screen dimensions
        val screenSizeFactor = (screenWidth.coerceAtMost(screenHeight) / 1080f).coerceIn(0.5f, 1.5f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.99f // Needed for the BlendMode to work properly
                }
                .drawWithContent {
                    // Draw the content (this will be the darkened background)
                    drawContent()

                    // Calculate responsive circle size based on target and screen size
                    val baseSize = targetBounds.width.coerceAtLeast(targetBounds.height) * 1.2f
                    val adjustedSize = baseSize * screenSizeFactor

                    // Draw the spotlight circle with theme-colored border
                    // First draw the border (slightly larger circle)
                    drawCircle(
                        color = primaryColor,
                        radius = adjustedSize * 0.75f,
                        center = Offset(
                            targetBounds.left + targetBounds.width / 2,
                            targetBounds.top + targetBounds.height / 2
                        )
                    )

                    // Then draw the transparent circle to create the "hole"
                    drawCircle(
                        color = Color.Transparent,
                        radius = adjustedSize * 0.7f,
                        center = Offset(
                            targetBounds.left + targetBounds.width / 2,
                            targetBounds.top + targetBounds.height / 2
                        ),
                        blendMode = BlendMode.Clear // This creates the "hole" in the overlay
                    )
                }
                .background(backgroundColor)
                .clickable(onClick = onDismiss)
        ) {
            // Tutorial message - minimalist design
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp * screenSizeFactor.coerceIn(0.8f, 1.2f),
                        end = 16.dp * screenSizeFactor.coerceIn(0.8f, 1.2f),
                        top = 32.dp * screenSizeFactor.coerceIn(0.8f, 1.2f),
                        bottom = 32.dp * screenSizeFactor.coerceIn(0.8f, 1.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f * screenSizeFactor.coerceIn(0.7f, 1.0f)), // Responsive width
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp), // Slightly rounded corners
                    border = BorderStroke(2.dp, primaryColor) // Add border to the card
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp * screenSizeFactor.coerceIn(0.8f, 1.2f)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon representing the action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 8.dp * screenSizeFactor.coerceIn(0.8f, 1.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(32.dp * screenSizeFactor.coerceIn(0.8f, 1.2f))
                            )
                            Spacer(modifier = Modifier.width(8.dp * screenSizeFactor.coerceIn(0.8f, 1.2f)))
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(32.dp * screenSizeFactor.coerceIn(0.8f, 1.2f))
                            )
                        }

                        // Shorter, more concise message
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                bottom = 16.dp * screenSizeFactor.coerceIn(0.8f, 1.2f),
                                start = 8.dp,
                                end = 8.dp
                            ),
                            fontWeight = FontWeight.Medium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * screenSizeFactor.coerceIn(0.9f, 1.1f)
                        )

                        // More minimalist button with icon and animation
                        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(if (isPressed) 0.9f else 1f)

                        IconButton(
                            onClick = onDismiss,
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(CircleShape)
                                .background(primaryColor)
                                .size(48.dp * screenSizeFactor.coerceIn(0.8f, 1.2f))
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar tutorial",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp * screenSizeFactor.coerceIn(0.8f, 1.2f))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for selecting a time
 */
@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Seleccionar hora"
) {
    if (showDialog) {
        var hour by remember { mutableStateOf(initialTime.hour) }
        var minute by remember { mutableStateOf(initialTime.minute) }
        var hourText by remember { mutableStateOf(initialTime.hour.toString().padStart(2, '0')) }
        var minuteText by remember { mutableStateOf(initialTime.minute.toString().padStart(2, '0')) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour picker
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { value ->
                                // Allow empty input or valid numbers
                                if (value.isEmpty()) {
                                    hourText = value
                                } else {
                                    val newHour = value.toIntOrNull()
                                    if (newHour != null && newHour in 0..23) {
                                        hourText = value
                                        hour = newHour
                                    } else if (value.length <= 2) {
                                        // Allow typing partial numbers (e.g. single digits)
                                        hourText = value
                                    }
                                }
                            },
                            modifier = Modifier.width(60.dp),
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Minute picker
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { value ->
                                // Allow empty input or valid numbers
                                if (value.isEmpty()) {
                                    minuteText = value
                                } else {
                                    val newMinute = value.toIntOrNull()
                                    if (newMinute != null && newMinute in 0..59) {
                                        minuteText = value
                                        minute = newMinute
                                    } else if (value.length <= 2) {
                                        // Allow typing partial numbers (e.g. single digits)
                                        minuteText = value
                                    }
                                }
                            },
                            modifier = Modifier.width(60.dp),
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Ensure we have valid values before confirming
                        val finalHour = hourText.toIntOrNull() ?: hour
                        val finalMinute = minuteText.toIntOrNull() ?: minute
                        onTimeSelected(LocalTime.of(finalHour, finalMinute))
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

/**
 * Dialog for selecting a date
 */
@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Seleccionar día"
) {
    if (showDialog) {
        var day by remember { mutableStateOf(initialDate.dayOfMonth) }
        var month by remember { mutableStateOf(initialDate.monthValue) }
        var year by remember { mutableStateOf(initialDate.year) }

        var dayText by remember { mutableStateOf(initialDate.dayOfMonth.toString().padStart(2, '0')) }
        var monthText by remember { mutableStateOf(initialDate.monthValue.toString().padStart(2, '0')) }
        var yearText by remember { mutableStateOf(initialDate.year.toString()) }

        // Month names for dropdown
        val monthNames = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        var showMonthDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day picker
                        OutlinedTextField(
                            value = dayText,
                            onValueChange = { value ->
                                // Allow empty input or valid numbers
                                if (value.isEmpty()) {
                                    dayText = value
                                } else {
                                    val newDay = value.toIntOrNull()
                                    if (newDay != null && newDay in 1..31) {
                                        dayText = value
                                        day = newDay
                                    } else if (value.length <= 2) {
                                        // Allow typing partial numbers
                                        dayText = value
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                            label = { Text("Día") }
                        )

                        // Month picker (dropdown)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = monthNames[month - 1],
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Mes") },
                                trailingIcon = {
                                    IconButton(onClick = { showMonthDropdown = !showMonthDropdown }) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Seleccionar mes",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                monthNames.forEachIndexed { index, name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            month = index + 1
                                            monthText = (index + 1).toString().padStart(2, '0')
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year picker
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = { value ->
                                // Allow empty input or valid numbers
                                if (value.isEmpty()) {
                                    yearText = value
                                } else {
                                    val newYear = value.toIntOrNull()
                                    if (newYear != null && newYear in 2000..2100) {
                                        yearText = value
                                        year = newYear
                                    } else if (value.length <= 4) {
                                        // Allow typing partial numbers
                                        yearText = value
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                            label = { Text("Año") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            // Ensure we have valid values before confirming
                            val finalDay = dayText.toIntOrNull() ?: day
                            val finalMonth = monthText.toIntOrNull() ?: month
                            val finalYear = yearText.toIntOrNull() ?: year

                            // Validate the date
                            val date = LocalDate.of(finalYear, finalMonth, finalDay)
                            onDateSelected(date)
                        } catch (e: Exception) {
                            // If the date is invalid, use the initial date
                            onDateSelected(initialDate)
                        }
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
