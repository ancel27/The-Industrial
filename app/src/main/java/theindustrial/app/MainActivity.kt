package theindustrial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.screens.AuthContainer
import theindustrial.app.ui.screens.HomeScreen
import theindustrial.app.ui.theme.TheIndustrialTheme
import theindustrial.app.ui.theme.ThemeManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val preferenceManager = remember { PreferenceManager(context) }
            val isLoggedIn by preferenceManager.isLoggedIn.collectAsState(initial = false)
            val savedAppKey by preferenceManager.appKey.collectAsState(initial = null)
            val savedUserId by preferenceManager.userId.collectAsState(initial = null)
            val cachedConfig by preferenceManager.cachedConfig.collectAsState(initial = null)
            val scope = rememberCoroutineScope()
            
            // Sync saved UserID to Global State immediately
            LaunchedEffect(savedUserId) {
                ThemeManager.setUserId(savedUserId)
            }

            // Immediate apply from cache if available
            LaunchedEffect(cachedConfig) {
                if (cachedConfig != null && ThemeManager.currentConfig.value == null) {
                    ThemeManager.updateConfig(cachedConfig!!)
                }
            }

            LaunchedEffect(savedAppKey) {
                if (!savedAppKey.isNullOrBlank()) {
                    try {
                        val trimmedKey = savedAppKey!!.trim()
                        val response = RetrofitInstance.api.getConfig(trimmedKey, trimmedKey)
                        if (response.isSuccessful) {
                            response.body()?.responseDetails?.firstOrNull()?.let { newConfig ->
                                // Update if different or no current config
                                if (newConfig != ThemeManager.currentConfig.value) {
                                    ThemeManager.updateConfig(newConfig)
                                    preferenceManager.setCachedConfig(newConfig)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            TheIndustrialTheme {
                if (!isLoggedIn) {
                    AuthContainer(onAuthSuccess = { userId ->
                        // Persist login state with User ID
                        scope.launch {
                            ThemeManager.setUserId(userId)
                            preferenceManager.setLoggedIn(true, userId)
                        }
                    })
                } else {
                    HomeScreen()
                }
            }
        }
    }
}
