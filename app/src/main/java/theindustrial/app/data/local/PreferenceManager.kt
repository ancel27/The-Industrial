package theindustrial.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.model.PlatformConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {
    private val gson = Gson()

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val APP_KEY = stringPreferencesKey("app_key")
        val CACHED_CONFIG = stringPreferencesKey("cached_config")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val CACHED_NEWS = stringPreferencesKey("cached_news")
        val CACHED_FOR_YOU = stringPreferencesKey("cached_for_you")
        val CACHED_ARTICLES = stringPreferencesKey("cached_articles")
        val CACHED_INTERVIEWS = stringPreferencesKey("cached_interviews")
        val CACHED_CASE_STUDIES = stringPreferencesKey("cached_case_studies")
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

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    private fun deserializeNews(json: String?): List<NewsItem>? {
        return json?.let {
            try {
                val type = object : TypeToken<List<NewsItem>>() {}.type
                gson.fromJson<List<NewsItem>>(it, type)
            } catch (e: Exception) {
                null
            }
        }
    }

    val cachedNews: Flow<List<NewsItem>?> = context.dataStore.data.map { preferences ->
        deserializeNews(preferences[CACHED_NEWS])
    }

    val cachedForYou: Flow<List<NewsItem>?> = context.dataStore.data.map { preferences ->
        deserializeNews(preferences[CACHED_FOR_YOU])
    }

    val cachedArticles: Flow<List<NewsItem>?> = context.dataStore.data.map { preferences ->
        deserializeNews(preferences[CACHED_ARTICLES])
    }

    val cachedInterviews: Flow<List<NewsItem>?> = context.dataStore.data.map { preferences ->
        deserializeNews(preferences[CACHED_INTERVIEWS])
    }

    val cachedCaseStudies: Flow<List<NewsItem>?> = context.dataStore.data.map { preferences ->
        deserializeNews(preferences[CACHED_CASE_STUDIES])
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

    suspend fun saveCachedNews(news: List<NewsItem>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_NEWS] = gson.toJson(news)
        }
    }

    suspend fun saveCachedForYou(news: List<NewsItem>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_FOR_YOU] = gson.toJson(news)
        }
    }

    suspend fun saveCachedArticles(news: List<NewsItem>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_ARTICLES] = gson.toJson(news)
        }
    }

    suspend fun saveCachedInterviews(news: List<NewsItem>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_INTERVIEWS] = gson.toJson(news)
        }
    }

    suspend fun saveCachedCaseStudies(news: List<NewsItem>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_CASE_STUDIES] = gson.toJson(news)
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean, id: Int? = null, name: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
            if (id != null) preferences[USER_ID] = id
            if (name != null) preferences[USER_NAME] = name
            
            if (!loggedIn) {
                preferences.remove(USER_ID)
                preferences.remove(USER_NAME)
            }
        }
    }

    suspend fun saveAppKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_KEY] = key
        }
    }

    suspend fun saveConfig(config: PlatformConfig) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_CONFIG] = gson.toJson(config)
        }
    }

    suspend fun clearAllCache() {
        context.dataStore.edit { preferences ->
            preferences.remove(CACHED_NEWS)
            preferences.remove(CACHED_FOR_YOU)
            preferences.remove(CACHED_ARTICLES)
            preferences.remove(CACHED_INTERVIEWS)
            preferences.remove(CACHED_CASE_STUDIES)
            // We keep user ID/login but wipe all content data
        }
    }

    // --- Legacy compatibility aliases for other screens ---
    suspend fun setAppKey(key: String) = saveAppKey(key)
    suspend fun setCachedConfig(config: PlatformConfig) = saveConfig(config)
}
