package de.tobi.luncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Nur ein dunkles Theme. Ein Light-Theme gibt es bewusst nicht (Spec §3).
private val LuncherColors = darkColorScheme(
    primary = Accent,
    onPrimary = Background,
    secondary = Accent,
    onSecondary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary,
    outline = TextTertiary,
)

/** Abstand zum Bildschirmrand (Spec §3). */
const val EDGE_PADDING_DP = 24

@Composable
fun LuncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LuncherColors,
        typography = LuncherTypography,
        content = content,
    )
}
