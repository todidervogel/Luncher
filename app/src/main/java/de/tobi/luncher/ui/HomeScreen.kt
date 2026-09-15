package de.tobi.luncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import de.tobi.luncher.ui.theme.Background
import de.tobi.luncher.ui.theme.ClockStyle
import de.tobi.luncher.ui.theme.DateStyle
import de.tobi.luncher.ui.theme.EDGE_PADDING_DP
import de.tobi.luncher.ui.theme.TextPrimary
import de.tobi.luncher.ui.theme.TextSecondary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Schwelle, ab der ein vertikaler Zug als Swipe zaehlt. */
private val SWIPE_THRESHOLD_DP = 72.dp

/**
 * Der leere Startbildschirm (Spec §4.1): Uhrzeit, Datum -- sonst nichts.
 *
 * Swipe nach unten oeffnet den App-Start, langer Druck die Einstellungen.
 * Swipe nach oben ist erkannt, aber in V1 ohne Funktion.
 */
@Composable
fun HomeScreen(
    time: String,
    date: String,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit,
    onLongPress: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                val threshold = SWIPE_THRESHOLD_DP.toPx()
                coroutineScope {
                    launch {
                        detectTapGestures(onLongPress = { onLongPress() })
                    }
                    launch {
                        var travelled = 0f
                        detectVerticalDragGestures(
                            onDragStart = { travelled = 0f },
                            onDragCancel = { travelled = 0f },
                            onDragEnd = {
                                when {
                                    travelled > threshold -> onSwipeDown()
                                    travelled < -threshold -> onSwipeUp()
                                }
                            },
                            onVerticalDrag = { change, delta ->
                                travelled += delta
                                change.consume()
                            },
                        )
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = EDGE_PADDING_DP.dp, vertical = EDGE_PADDING_DP.dp),
        ) {
            // Uhrzeit sitzt im oberen Drittel.
            Spacer(modifier = Modifier.fillMaxHeight(0.22f))
            Text(
                text = time,
                style = ClockStyle,
                color = TextPrimary,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                style = DateStyle,
                color = TextSecondary,
                textAlign = TextAlign.Start,
            )
        }
    }
}
