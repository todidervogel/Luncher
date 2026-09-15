package de.tobi.luncher.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.tobi.luncher.AliasEntry
import de.tobi.luncher.BuildConfig
import de.tobi.luncher.ui.theme.Accent
import de.tobi.luncher.ui.theme.EDGE_PADDING_DP
import de.tobi.luncher.ui.theme.ErrorRed
import de.tobi.luncher.ui.theme.HintStyle
import de.tobi.luncher.ui.theme.InputStyle
import de.tobi.luncher.ui.theme.ListStyle
import de.tobi.luncher.ui.theme.Surface
import de.tobi.luncher.ui.theme.SurfaceRaised
import de.tobi.luncher.ui.theme.TextPrimary
import de.tobi.luncher.ui.theme.TextSecondary
import de.tobi.luncher.ui.theme.TextTertiary

/**
 * Einstellungen (Spec §4.3): Aliase, Standard-Launcher, Version. Sonst nichts.
 */
@Composable
fun SettingsScreen(
    aliases: List<AliasEntry>,
    onAddAlias: (alias: String, appName: String) -> Boolean,
    onRemoveAlias: (String) -> Unit,
) {
    val context = LocalContext.current
    var showAliases by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(EDGE_PADDING_DP.dp),
        ) {
            Text(
                text = if (showAliases) "Aliase" else "Einstellungen",
                style = ListStyle,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (showAliases) {
                AliasList(
                    aliases = aliases,
                    onRemoveAlias = onRemoveAlias,
                    onAdd = { showAddDialog = true },
                    onBack = { showAliases = false },
                )
            } else {
                SettingsRow(text = "Aliase verwalten") { showAliases = true }
                SettingsRow(text = "Als Standard-Launcher setzen") {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_HOME_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    } catch (e: ActivityNotFoundException) {
                        // Auf Geraeten ohne diesen Einstellungs-Screen passiert nichts.
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Über", style = ListStyle, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = HintStyle,
                    color = TextTertiary,
                )
            }
        }
    }

    if (showAddDialog) {
        AddAliasDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = onAddAlias,
        )
    }
}

@Composable
private fun SettingsRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = ListStyle,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun AliasList(
    aliases: List<AliasEntry>,
    onRemoveAlias: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit,
) {
    if (aliases.isEmpty()) {
        Text(text = "Keine Aliase angelegt", style = HintStyle, color = TextTertiary)
        Spacer(modifier = Modifier.height(16.dp))
    }
    aliases.forEach { entry ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.alias,
                    style = ListStyle,
                    // Aliase auf deinstallierte Pakete werden ausgegraut.
                    color = if (entry.installed) TextPrimary else TextTertiary,
                )
                Text(
                    text = entry.label ?: "${entry.packageName} · nicht installiert",
                    style = HintStyle,
                    color = if (entry.installed) TextSecondary else TextTertiary,
                )
            }
            TextButton(onClick = { onRemoveAlias(entry.alias) }) {
                Text(text = "löschen", style = HintStyle, color = ErrorRed)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    SettingsRow(text = "Alias hinzufügen", onClick = onAdd)
    SettingsRow(text = "Zurück", onClick = onBack)
}

@Composable
private fun AddAliasDialog(
    onDismiss: () -> Unit,
    onConfirm: (alias: String, appName: String) -> Boolean,
) {
    var alias by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        title = { Text(text = "Alias", style = ListStyle, color = TextPrimary) },
        text = {
            Column {
                DialogField(
                    value = alias,
                    placeholder = "Alias",
                    onValueChange = { alias = it; failed = false },
                )
                Spacer(modifier = Modifier.height(20.dp))
                DialogField(
                    value = appName,
                    placeholder = "App-Name, exakt",
                    onValueChange = { appName = it; failed = false },
                )
                if (failed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "nicht gefunden", style = HintStyle, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (onConfirm(alias, appName)) onDismiss() else failed = true
            }) {
                Text(text = "anlegen", style = HintStyle, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "abbrechen", style = HintStyle, color = TextSecondary)
            }
        },
    )
}

@Composable
private fun DialogField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = InputStyle.copy(color = TextPrimary),
            singleLine = true,
            cursorBrush = SolidColor(Accent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = placeholder, style = InputStyle, color = TextTertiary)
                }
                innerTextField()
            },
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextTertiary),
        )
    }
}
