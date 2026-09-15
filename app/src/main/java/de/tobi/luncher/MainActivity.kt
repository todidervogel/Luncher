package de.tobi.luncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.tobi.luncher.time.ClockReceiver
import de.tobi.luncher.ui.HomeScreen
import de.tobi.luncher.ui.LauncherScreen
import de.tobi.luncher.ui.SettingsScreen
import de.tobi.luncher.ui.theme.LuncherTheme

/**
 * Die einzige Activity. Registriert sich als HOME-Launcher und schaltet
 * zwischen den drei Screens um (Spec §4).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels { LauncherViewModel.factory(this) }

    private val clockReceiver = ClockReceiver { viewModel.onTick() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inhalt zeichnet hinter Status- und Navigationsleiste, helle Icons.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        onBackPressedDispatcher.addCallback(this) {
            // Auf Home tut die Zurueck-Taste nichts -- ein Launcher darf sich
            // nicht wegdruecken lassen (Spec §9).
            if (viewModel.state.value.screen != Screen.HOME) viewModel.goHome()
        }

        setContent {
            LuncherTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val aliases by viewModel.aliasEntries.collectAsStateWithLifecycle()

                Crossfade(
                    targetState = state.screen,
                    animationSpec = tween(durationMillis = 200),
                    label = "screen",
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            time = state.time,
                            date = state.date,
                            onSwipeDown = viewModel::openLaunch,
                            onSwipeUp = viewModel::onSwipeUp,
                            onLongPress = viewModel::openSettings,
                        )

                        Screen.LAUNCH -> LauncherScreen(
                            query = state.query,
                            notFound = state.notFound,
                            lineError = state.lineError,
                            onQueryChange = viewModel::onQueryChange,
                            onSubmit = viewModel::submit,
                            onDismiss = viewModel::goHome,
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            aliases = aliases,
                            onAddAlias = viewModel::addAlias,
                            onRemoveAlias = viewModel::removeAlias,
                        )
                    }
                }
            }
        }
    }

    /**
     * Home-Taste, waehrend der Launcher schon vorn steht: zurueck auf den
     * leeren Screen -- und ausdruecklich nicht ins Eingabefeld (Spec §4.1).
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.goHome()
    }

    override fun onStart() {
        super.onStart()
        // ACTION_TIME_TICK laesst sich nur zur Laufzeit registrieren.
        registerReceiver(clockReceiver, ClockReceiver.filter())
        viewModel.onTick()
    }

    override fun onPause() {
        super.onPause()
        WindowInsetsControllerCompat(window, window.decorView)
            .hide(WindowInsetsCompat.Type.ime())
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(clockReceiver)
        // Nach der Rueckkehr aus einer anderen App steht wieder der leere Screen.
        viewModel.goHome()
    }
}
