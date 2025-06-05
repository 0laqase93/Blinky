package dam.tfg.blinky.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.tfg.blinky.dataclass.WrenchEmotion
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * A composable that displays animated eyes that move randomly around the screen
 * and occasionally return to the center.
 *
 * @param emotion The current emotion to display
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun AnimatedEyes(
    emotion: WrenchEmotion,
    modifier: Modifier = Modifier
) {
    // Estados para las posiciones de los ojos
    val leftEyeOffset = remember { mutableStateOf(Offset.Zero) }
    val rightEyeOffset = remember { mutableStateOf(Offset.Zero) }
    
    // Efecto para animar los ojos con movimiento aleatorio
    LaunchedEffect(key1 = Unit) {
        while(true) {
            // Determinar si volvemos al centro o movemos aleatoriamente
            val returnToCenter = Random.nextInt(0, 10) < 2 // 20% de probabilidad de volver al centro
            
            if (returnToCenter) {
                // Volver al centro gradualmente
                leftEyeOffset.value = Offset.Zero
                rightEyeOffset.value = Offset.Zero
            } else {
                // Generar nuevas posiciones aleatorias dentro de un rango limitado
                val maxOffset = 30f // Máximo desplazamiento en dp
                leftEyeOffset.value = Offset(
                    x = Random.nextFloat() * maxOffset * 2 - maxOffset,
                    y = Random.nextFloat() * maxOffset * 2 - maxOffset
                )
                rightEyeOffset.value = Offset(
                    x = Random.nextFloat() * maxOffset * 2 - maxOffset,
                    y = Random.nextFloat() * maxOffset * 2 - maxOffset
                )
            }
            
            // Esperar antes del siguiente movimiento (entre 0.5 y 2 segundos)
            delay(Random.nextLong(500, 2000))
        }
    }

    // Mostrar los ojos con las posiciones calculadas
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ojo izquierdo con offset aleatorio
        Box(
            modifier = Modifier
                .offset(x = leftEyeOffset.value.x.dp, y = leftEyeOffset.value.y.dp)
        ) {
            Text(
                text = emotion.leftEye,
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
                text = emotion.rightEye,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 150.sp)
            )
        }
    }
}