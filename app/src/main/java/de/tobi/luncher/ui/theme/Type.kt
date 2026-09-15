package de.tobi.luncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Roboto (Systemschrift), nur Light und Regular (Spec §3).
private val Roboto = FontFamily.Default

/** Uhrzeit: 64sp Light. */
val ClockStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Light,
    fontSize = 64.sp,
)

/** Datum: 14sp, Text sekundaer. */
val DateStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
)

/** Eingabefeld: 20sp Regular. */
val InputStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
)

/** Hinweistexte: 12sp. */
val HintStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
)

/** Einstellungszeilen. */
val ListStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
)

val LuncherTypography = Typography(
    bodyLarge = ListStyle,
    bodyMedium = DateStyle,
    bodySmall = HintStyle,
)
