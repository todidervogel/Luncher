package de.tobi.luncher.time

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatierung der Uhrzeit und des Datums (Spec §4.1). */
object Clock {

    private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN)
    private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN)

    fun time(now: LocalDateTime = LocalDateTime.now()): String = TIME.format(now)

    fun date(now: LocalDateTime = LocalDateTime.now()): String = DATE.format(now)
}
