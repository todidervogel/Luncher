package de.tobi.luncher.launcher

import java.util.Locale

/**
 * Namensabgleich (Spec §6). Reine Funktionen, keine Android-Abhaengigkeit.
 *
 * Bewusst **kein** Fuzzy-, Praefix- oder Teilstring-Matching und keine
 * Levenshtein-Distanz. Das ist der Kern des Konzepts und keine Vereinfachung:
 * wer eine App oeffnen will, muss ihren Namen kennen. Bitte auch bei spaeterem
 * Refactoring nicht "verbessern".
 */
object AppMatcher {

    private val WHITESPACE = Regex("\\s+")

    /**
     * Trimmen, kleinschreiben mit [Locale.GERMAN], Mehrfach-Leerzeichen
     * zusammenziehen. Umlaute und ss bleiben stehen, wie sie sind.
     */
    fun normalize(raw: String): String =
        raw.trim().lowercase(Locale.GERMAN).replace(WHITESPACE, " ")

    /**
     * Sucht die zur [input] passende App.
     *
     * Reihenfolge: 1. Aliase (ueberschreiben gleichnamige Labels),
     * 2. exakter Treffer auf ein normalisiertes App-Label, 3. sonst null.
     *
     * Zeigt ein Alias auf ein nicht (mehr) installiertes Paket, wird er
     * ignoriert und die Suche laeuft normal weiter.
     *
     * @param aliases Abbildung Alias -> packageName.
     */
    fun match(
        input: String,
        apps: List<InstalledApp>,
        aliases: Map<String, String>,
    ): InstalledApp? {
        val query = normalize(input)
        if (query.isEmpty()) return null

        for ((alias, packageName) in aliases) {
            if (normalize(alias) != query) continue
            val target = apps.firstOrNull { it.packageName == packageName }
            if (target != null) return target
        }

        // Bei mehrdeutigen Labels gewinnt der zuerst gefundene Eintrag.
        return apps.firstOrNull { normalize(it.label) == query }
    }
}
