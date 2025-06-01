package dam.tfg.blinky.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

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