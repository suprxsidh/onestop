package com.suprxsidh.onestop.gestures.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suprxsidh.onestop.gestures.model.GestureSettings
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.gestureSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "gesture_settings")

private val KEY_ENABLED = booleanPreferencesKey("enabled")
private val KEY_SHAKE_ACTION = stringPreferencesKey("shake_action")
private val KEY_ROTATE_ACTION = stringPreferencesKey("rotate_action")
private val KEY_BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")

class GestureSettingsRepository(private val context: Context) {

    val settings: Flow<GestureSettings> =
        context.gestureSettingsDataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { prefs ->
                GestureSettings(
                    enabled = prefs[KEY_ENABLED] ?: true,
                    mappings = mapOf(
                        GestureType.SHAKE to parseAction(prefs[KEY_SHAKE_ACTION]),
                        GestureType.ROTATE to parseAction(prefs[KEY_ROTATE_ACTION])
                    ),
                    blockedPackages = prefs[KEY_BLOCKED_PACKAGES] ?: emptySet()
                )
            }

    suspend fun setEnabled(enabled: Boolean) {
        context.gestureSettingsDataStore.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun setMapping(gestureType: GestureType, action: GlobalActionType) {
        val key = when (gestureType) {
            GestureType.SHAKE -> KEY_SHAKE_ACTION
            GestureType.ROTATE -> KEY_ROTATE_ACTION
        }
        context.gestureSettingsDataStore.edit { it[key] = action.name }
    }

    suspend fun setBlockedPackages(packages: Set<String>) {
        context.gestureSettingsDataStore.edit { it[KEY_BLOCKED_PACKAGES] = packages }
    }

    private fun parseAction(name: String?): GlobalActionType =
        name?.let { runCatching { GlobalActionType.valueOf(it) }.getOrNull() } ?: GlobalActionType.NONE
}
