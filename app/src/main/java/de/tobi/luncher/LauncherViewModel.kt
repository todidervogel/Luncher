package de.tobi.luncher

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.tobi.luncher.launcher.AppLauncher
import de.tobi.luncher.launcher.AppMatcher
import de.tobi.luncher.launcher.AppRepository
import de.tobi.luncher.settings.AliasStore
import de.tobi.luncher.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { HOME, LAUNCH, SETTINGS }

data class LauncherState(
    val screen: Screen = Screen.HOME,
    val query: String = "",
    /** Hinweis "nicht gefunden"; verschwindet bei der naechsten Zeichenaenderung. */
    val notFound: Boolean = false,
    /** Linie unter dem Eingabefeld faerbt sich fuer 600ms rot. */
    val lineError: Boolean = false,
    val time: String = Clock.time(),
    val date: String = Clock.date(),
)

/** Ein Alias, wie ihn die Einstellungen anzeigen. */
data class AliasEntry(
    val alias: String,
    val packageName: String,
    val label: String?,
) {
    /** Aliase auf deinstallierte Pakete werden ausgegraut (Spec §7). */
    val installed: Boolean get() = label != null
}

class LauncherViewModel(
    private val appRepository: AppRepository,
    private val appLauncher: AppLauncher,
    private val aliasStore: AliasStore,
    private val vibrate: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(LauncherState())
    val state: StateFlow<LauncherState> = _state.asStateFlow()

    private val aliases: StateFlow<Map<String, String>> = aliasStore.aliases
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Erzwingt ein Neuberechnen der Alias-Liste, etwa beim Oeffnen der Einstellungen. */
    private val aliasRefresh = MutableStateFlow(0)

    val aliasEntries: StateFlow<List<AliasEntry>> = combine(aliases, aliasRefresh) { current, _ ->
        val apps = appRepository.apps()
        current.entries
            .sortedBy { it.key }
            .map { (alias, packageName) ->
                AliasEntry(
                    alias = alias,
                    packageName = packageName,
                    label = apps.firstOrNull { it.packageName == packageName }?.label,
                )
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var errorLineJob: Job? = null

    init {
        // App-Liste einmal im Hintergrund waermen, damit der erste Treffer
        // nicht auf den PackageManager warten muss.
        viewModelScope.launch(Dispatchers.Default) { appRepository.apps() }
    }

    // --- Uhr -------------------------------------------------------------

    /** Wird vom ClockReceiver zur vollen Minute gerufen. */
    fun onTick() = _state.update { it.copy(time = Clock.time(), date = Clock.date()) }

    // --- Navigation ------------------------------------------------------

    /** Swipe nach unten auf dem Home-Screen. */
    fun openLaunch() = _state.update { it.copy(screen = Screen.LAUNCH) }

    /** Langer Druck auf dem Home-Screen. */
    fun openSettings() {
        aliasRefresh.update { it + 1 }
        _state.update { it.copy(screen = Screen.SETTINGS) }
    }

    /**
     * Swipe nach oben auf dem Home-Screen. In V1 ohne Funktion, die Geste ist
     * fuer den Second Brain reserviert (Spec §10).
     */
    fun onSwipeUp() = Unit

    /** Zurueck zum leeren Home-Screen; Eingabe und Fehlerzustand fallen weg. */
    fun goHome() {
        errorLineJob?.cancel()
        _state.update {
            LauncherState(screen = Screen.HOME, time = it.time, date = it.date)
        }
    }

    // --- Eingabe ---------------------------------------------------------

    fun onQueryChange(value: String) =
        _state.update { it.copy(query = value, notFound = false) }

    /** Bestaetigung per Done-Taste (Spec §4.2). */
    fun submit() {
        val current = _state.value
        val app = AppMatcher.match(current.query, appRepository.apps(), aliases.value)
        if (app == null) {
            miss()
            return
        }
        if (!appLauncher.launch(app)) {
            // Vermutlich gerade deinstalliert: Cache verwerfen, Fehlschlag zeigen.
            appRepository.invalidate()
            miss()
            return
        }
        goHome()
    }

    private fun miss() {
        vibrate()
        _state.update { it.copy(notFound = true, lineError = true) }
        errorLineJob?.cancel()
        errorLineJob = viewModelScope.launch {
            delay(ERROR_LINE_MILLIS)
            _state.update { it.copy(lineError = false) }
        }
    }

    // --- Aliase ----------------------------------------------------------

    /**
     * Legt einen Alias an. Die App wird ueber ihren exakten Namen aufgeloest --
     * es gibt auch hier keine Liste und keine Vorschlaege.
     *
     * @return false, wenn kein App-Name passt.
     */
    fun addAlias(alias: String, appName: String): Boolean {
        if (AppMatcher.normalize(alias).isEmpty()) return false
        val app = AppMatcher.match(appName, appRepository.apps(), emptyMap()) ?: return false
        viewModelScope.launch { aliasStore.put(alias, app.packageName) }
        return true
    }

    fun removeAlias(alias: String) {
        viewModelScope.launch { aliasStore.remove(alias) }
    }

    companion object {
        private const val ERROR_LINE_MILLIS = 600L

        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = context.applicationContext
                LauncherViewModel(
                    appRepository = AppRepository.get(app),
                    appLauncher = AppLauncher(app),
                    aliasStore = AliasStore(app),
                    vibrate = { vibrateOnce(app) },
                )
            }
        }

        private fun vibrateOnce(context: Context) {
            val vibrator = context.getSystemService(Vibrator::class.java) ?: return
            if (!vibrator.hasVibrator()) return
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
