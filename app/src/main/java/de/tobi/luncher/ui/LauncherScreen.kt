package de.tobi.luncher.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tobi.luncher.ui.theme.Accent
import de.tobi.luncher.ui.theme.Background
import de.tobi.luncher.ui.theme.EDGE_PADDING_DP
import de.tobi.luncher.ui.theme.ErrorRed
import de.tobi.luncher.ui.theme.HintStyle
import de.tobi.luncher.ui.theme.InputStyle
import de.tobi.luncher.ui.theme.TextPrimary
import de.tobi.luncher.ui.theme.TextTertiary

/** Schwelle, ab der ein Zug nach oben den App-Start wieder schliesst. */
private val SWIPE_DISMISS_THRESHOLD_DP = 72.dp

/**
 * Der App-Start (Spec §4.2): ein Eingabefeld, sonst nichts.
 *
 * Keine Vorschlaege, kein Autocomplete, keine App-Liste, keine Historie --
 * und die Tastatur geht nicht von selbst auf.
 */
@Composable
fun LauncherScreen(
    query: String,
    notFound: Boolean,
    lineError: Boolean,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    // Das Feld startet ohne Fokus; die Tastatur kommt erst beim Antippen.
    LaunchedEffect(Unit) { focusManager.clearFocus(force = true) }

    val lineColor by animateColorAsState(
        targetValue = if (lineError) ErrorRed else TextTertiary,
        animationSpec = tween(durationMillis = 150),
        label = "lineColor",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                val threshold = SWIPE_DISMISS_THRESHOLD_DP.toPx()
                var travelled = 0f
                detectVerticalDragGestures(
                    onDragStart = { travelled = 0f },
                    onDragCancel = { travelled = 0f },
                    onDragEnd = { if (travelled < -threshold) onDismiss() },
                    onVerticalDrag = { change, delta ->
                        travelled += delta
                        change.consume()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .padding(horizontal = EDGE_PADDING_DP.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = InputStyle.copy(color = TextPrimary),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
                // autoCorrect = false setzt TYPE_TEXT_FLAG_NO_SUGGESTIONS und laesst
                // TYPE_TEXT_FLAG_AUTO_COMPLETE weg: keine Vorschlaege, keine Historie.
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                // Bestaetigung ausschliesslich ueber die Done-Taste.
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(text = "App-Name", style = InputStyle, color = TextTertiary)
                    }
                    innerTextField()
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor),
            )
            // Feste Hoehe, damit das Feld beim Fehler nicht springt.
            Box(modifier = Modifier.height(24.dp)) {
                if (notFound) {
                    Text(
                        text = "nicht gefunden",
                        style = HintStyle,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
