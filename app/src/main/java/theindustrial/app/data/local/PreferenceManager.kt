package theindustrial.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import theindustrial.app.data.model.PlatformConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val APP_KEY = stringPreferencesKey("app_key")
        val CACHED_CONFIG = stringPreferencesKey("cached_config")
        val USER_ID = intPreferencesKey("user_id")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val appKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_KEY]
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val cachedConfig: Flow<PlatformConfig?> = context.dataStore.data.map { preferences ->
        preferences[CACHED_CONFIG]?.let {
            try {
                gson.fromJson(it, PlatformConfig::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean, id: Int? = null) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
            if (id != null) preferences[USER_ID] = id
        }
    }

    suspend fun setAppKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_KEY] = key
        }
    }

    suspend fun setCachedConfig(config: PlatformConfig) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_CONFIG] = gson.toJson(config)
        }
    }
}
