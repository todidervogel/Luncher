package de.tobi.luncher.launcher

/**
 * Eine startbare App. Bewusst frei von Android-Typen, damit [AppMatcher]
 * ohne Instrumentierung testbar bleibt.
 */
data class InstalledApp(
    val label: String,
    val packageName: String,
    val activityName: String,
)
