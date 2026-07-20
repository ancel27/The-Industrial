package kivaa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.screens.AuthContainer
import kivaa.app.ui.screens.HomeScreen
import kivaa.app.ui.theme.TheIndustrialTheme
import kivaa.app.ui.theme.ThemeManager
import kivaa.app.utils.NavigationManager
import kivaa.app.utils.DeepLinkAction

class MainActivity : ComponentActivity() {
    private val deepLinkData = mutableStateOf<android.net.Uri?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkData.value = intent.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkData.value = intent.data
        
        // Force light status bar icons (dark symbols) by default
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val context = LocalContext.current
            val preferenceManager = remember { PreferenceManager(context) }
            val isLoggedIn by preferenceManager.isLoggedIn.collectAsState(initial = false)
            val savedUserId by preferenceManager.userId.collectAsState(initial = null)
            val savedUserName by preferenceManager.userName.collectAsState(initial = null)
            val savedAppKey by preferenceManager.appKey.collectAsState(initial = null)
            val cachedConfig by preferenceManager.cachedConfig.collectAsState(initial = null)
            val scope = rememberCoroutineScope()

            // Sync User Data to ThemeManager globally
            LaunchedEffect(savedUserId, savedUserName) {
                ThemeManager.setUserId(savedUserId)
                ThemeManager.setUserName(savedUserName)
            }

            // Handle Deep Link (Expanded Support)
            LaunchedEffect(deepLinkData.value) {
                deepLinkData.value?.let { uri ->
                    val segments = uri.pathSegments
                    var platformName = ""
                    var screen: String? = null
                    var id: String? = null

                    if (uri.scheme == "kivaa") {
                        platformName = uri.host ?: ""
                        screen = segments.getOrNull(0)
                        id = segments.getOrNull(1)
                    } else if (uri.scheme == "http" || uri.scheme == "https") {
                        val host = uri.host ?: ""
                        if (host == "kivaa.io.in") {
                            if (segments.size >= 2 && segments[0] == "app") {
                                // Legacy: /app/factory
                                platformName = segments[1]
                            } else if (segments.isNotEmpty()) {
                                // New: /factoryfuture/watch/123
                                platformName = segments[0]
                                screen = segments.getOrNull(1)
                                id = segments.getOrNull(2)
                            }
                        } else {
                            // Branded Domain: https://www.factoryfuture.in/news/59ab98c72a/slug
                            // Host defines the platform
                            platformName = when (host) {
                                "www.factoryfuture.in", "factoryfuture.in" -> "factory"
                                "www.theindustrial.in", "theindustrial.in", "www.industrial.in", "industrial.in" -> "industrial"
                                "www.thingsofbusiness.com", "thingsofbusiness.com" -> "business"
                                "www.mobilityhyperdrive.in", "mobilityhyperdrive.in" -> "mobility"
                                "www.bankingontechnology.com", "bankingontechnology.com" -> "banking"
                                "www.technologue.in", "technologue.in" -> "technologue"
                                else -> host.substringBefore(".")
                            }
                            
                            // Check for /news/[hash] structure
                            if (segments.size >= 2 && segments[0].lowercase() == "news") {
                                screen = "news"
                                id = segments[1] // Extracts the hash (e.g. 59ab98c72a)
                            } else {
                                screen = segments.getOrNull(0)
                                id = segments.getOrNull(1)
                            }
                        }
                    }
                    
                    if (platformName.isNotBlank()) {
                        val keyMap = mapOf(
                            "factory" to "kivaa_factory_future_mobile_Kt9AKaR7Q3pJ_Y9WO1NxOogvE6nTnhbj",
                            "factoryfuture" to "kivaa_factory_future_mobile_Kt9AKaR7Q3pJ_Y9WO1NxOogvE6nTnhbj",
                            "industrial" to "kivaa_the_industrial_mobile_QF9PLdi9smCZbrLaDLTX-6t7t-EReE1S",
                            "theindustrial" to "kivaa_the_industrial_mobile_QF9PLdi9smCZbrLaDLTX-6t7t-EReE1S",
                            "business" to "kivaa_things_of_business_mobile_-IHdHKSI-2OHY7HdAQC8qJFlY8ryMmDA",
                            "thingsofbusiness" to "kivaa_things_of_business_mobile_-IHdHKSI-2OHY7HdAQC8qJFlY8ryMmDA",
                            "mobility" to "kivaa_mobility_hyperdrive_mobile_S0yAFbkK2KozdvJzBrbwXeSdU1Nr0OKs",
                            "hyperdrive" to "kivaa_mobility_hyperdrive_mobile_S0yAFbkK2KozdvJzBrbwXeSdU1Nr0OKs",
                            "banking" to "kivaa_banking_on_technology_mobile_oTZ8lQ3h_tmdaLO93IogxcBypyeylHBH",
                            "technologue" to "kivaa_technologue_mobile_BJyOcCk7zUpTbKVVCLvvZRJKL7U9paxv"
                        )

                        val finalKey = keyMap[platformName.lowercase()] ?: platformName
                        
                        // Set pending action for HomeScreen to consume
                        NavigationManager.pendingAction.value = DeepLinkAction(
                            platform = platformName,
                            targetScreen = screen,
                            itemId = id
                        )

                        try {
                            val response = RetrofitInstance.api.getConfig(finalKey, finalKey)
                            if (response.isSuccessful) {
                                response.body()?.responseDetails?.firstOrNull()?.let { newConfig ->
                                    ThemeManager.updateConfig(newConfig)
                                    preferenceManager.saveAppKey(finalKey)
                                    preferenceManager.saveConfig(newConfig)
                                }
                            }
                        } catch (e: Exception) { }
                    }
                    
                    // Clear state to allow re-triggering same link
                    deepLinkData.value = null
                }
            }

            // Fetch dynamic config on launch using current flavor's key
            LaunchedEffect(Unit) {
                val key = BuildConfig.PLATFORM_KEY
                try {
                    val response = RetrofitInstance.api.getConfig(key, key)
                    if (response.isSuccessful) {
                        response.body()?.responseDetails?.firstOrNull()?.let { newConfig ->
                            ThemeManager.updateConfig(newConfig)
                            preferenceManager.saveAppKey(key)
                            preferenceManager.saveConfig(newConfig)
                        }
                    }
                } catch (e: Exception) { }
            }

            // Apply from cache immediately if available
            LaunchedEffect(cachedConfig) {
                if (cachedConfig != null && ThemeManager.currentConfig.value == null) {
                    ThemeManager.updateConfig(cachedConfig!!)
                }
            }

            TheIndustrialTheme {
                if (!isLoggedIn) {
                    AuthContainer(onAuthSuccess = { userId, userName ->
                        scope.launch {
                            preferenceManager.setLoggedIn(true, userId, userName)
                        }
                    })
                } else {
                    HomeScreen(onLogout = {
                        scope.launch {
                            preferenceManager.clearLogin()
                        }
                    })
                }
            }
        }
    }
}
