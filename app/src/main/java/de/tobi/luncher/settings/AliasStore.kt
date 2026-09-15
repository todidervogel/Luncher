package de.tobi.luncher.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tobi.luncher.launcher.AppMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "luncher")

/**
 * Aliase (Spec §7), abgelegt als Preferences-Eintraege `alias.<name>` -> packageName.
 *
 * Aliase werden nur manuell in den Einstellungen angelegt; es gibt bewusst
 * keine automatische Generierung.
 */
class AliasStore(context: Context) {

    private val store = context.applicationContext.dataStore

    /** Abbildung normalisierter Alias -> packageName. */
    val aliases: Flow<Map<String, String>> = store.data.map { preferences ->
        preferences.asMap()
            .mapNotNull { (key, value) ->
                val name = key.name
                if (name.startsWith(PREFIX) && value is String) {
                    name.removePrefix(PREFIX) to value
                } else {
                    null
                }
            }
            .toMap()
    }

    suspend fun put(alias: String, packageName: String) {
        val key = AppMatcher.normalize(alias)
        if (key.isEmpty()) return
        store.edit { it[keyFor(key)] = packageName }
    }

    suspend fun remove(alias: String) {
        store.edit { it.remove(keyFor(AppMatcher.normalize(alias))) }
    }

    private fun keyFor(normalizedAlias: String) = stringPreferencesKey("$PREFIX$normalizedAlias")

    private companion object {
        const val PREFIX = "alias."
    }
}
