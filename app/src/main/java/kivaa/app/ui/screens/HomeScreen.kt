package kivaa.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kivaa.app.R
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.NewsItem
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.DynamicLogo
import kivaa.app.ui.theme.ThemeManager
import kivaa.app.utils.NetworkUtils
import kivaa.app.utils.NavigationManager

sealed class Screen(val route: String, val title: String, val icon: Any) {
    object Menu : Screen("menu", "Menu", Icons.Default.Menu)
    object News : Screen("news", "News", R.drawable.news)
    object ForYou : Screen("for_you", "For You", Icons.Default.Home)
    object Magazine : Screen("magazine", "Magazine", R.drawable.mag2)
    object Video : Screen("video", "AudiVue", R.drawable.video)
    
    // Virtual screens for personalized content
    object AccountSettings : Screen("settings", "My Account", R.drawable.user)
    object Addresses : Screen("addresses", "My Addresses", Icons.Default.LocationOn)
    object Bookmarks : Screen("bookmarks", "Bookmarks", Icons.Default.Bookmark)
    object Liked : Screen("liked", "Liked Content", Icons.Default.ThumbUp)
    object History : Screen("history", "Reading History", Icons.Default.History)
    object MyComments : Screen("comments", "My Comments", Icons.Default.ChatBubble)
    object MyReviews : Screen("reviews", "My Reviews", Icons.Default.Star)
    object Orders : Screen("orders", "My Orders", Icons.Default.Receipt)
    object Subscription : Screen("subscription", "Subscription", R.drawable.cart)
    object Preferences : Screen("preferences", "Preferences", Icons.Default.Notifications)
    object Exclusive : Screen("exclusive", "Exclusive", R.drawable.exclusive)
    object Support : Screen("support", "Support", Icons.Default.SupportAgent)
    object AskKivaa : Screen("ask_kivaa", "Ask Kivaa", Icons.Default.Message)
    object QrScanner : Screen("qr_scanner", "QR Scanner", R.drawable.qrcode)
    object SearchResults : Screen("search_results", "Search", Icons.Default.Search)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()
    val isOnline by NetworkUtils.observeConnectivity(context).collectAsState(initial = true)
    
    var previousOnlineState by remember { mutableStateOf(isOnline) }
    var showBackOnlineBanner by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!previousOnlineState && isOnline) {
            showBackOnlineBanner = true
            kotlinx.coroutines.delay(3000)
            showBackOnlineBanner = false
        }
        previousOnlineState = isOnline
    }
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ForYou) }
    var selectedNewsHash by remember { mutableStateOf<String?>(null) }
    
    // Handle Deep Link Navigation Actions
    LaunchedEffect(NavigationManager.pendingAction.value) {
        NavigationManager.pendingAction.value?.let { action ->
            when (action.targetScreen?.lowercase()) {
                "watch" -> currentScreen = Screen.Video
                "news" -> {
                    selectedNewsHash = action.itemId
                }
                "magazine" -> currentScreen = Screen.Magazine
            }
            NavigationManager.clear()
        }
    }
    
    // Bottom Sheet Menu
    var showMenuSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Search State
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)

    val performSearch = {
        if (searchQuery.isNotBlank() && !appKey.isNullOrBlank()) {
            focusManager.clearFocus()
            currentScreen = Screen.SearchResults
            isSearching = true
            scope.launch {
                try {
                    val cleanKey = appKey!!.trim()
                    val response = RetrofitInstance.api.searchContent(
                        cleanKey, searchQuery.trim(), "all",
                        cleanKey, searchQuery.trim(), "all"
                    )
                    if (response.isSuccessful) {
                        searchResults = response.body()?.responseDetails ?: emptyList()
                    }
                } catch (e: Exception) {
                    searchResults = emptyList()
                } finally {
                    isSearching = false
                }
            }
        }
    }

    // Navigation Logic: Back from anything to For You. Back from For You exits.
    BackHandler(enabled = currentScreen != Screen.ForYou || selectedNewsHash != null) {
        if (selectedNewsHash != null) {
            selectedNewsHash = null
        } else {
            currentScreen = Screen.ForYou
            searchQuery = ""
        }
    }

    if (selectedNewsHash != null) {
        NewsDetailScreen(
            newsHash = selectedNewsHash!!,
            onBack = { selectedNewsHash = null }
        )
    } else if (currentScreen == Screen.AskKivaa) {
        KivaaChatScreen(onBack = { currentScreen = Screen.ForYou })
    } else if (currentScreen == Screen.Subscription) {
        SubscriptionScreen(onBack = { currentScreen = Screen.ForYou })
    } else {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        // Main Search Row
                        Row(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentScreen != Screen.ForYou && currentScreen != Screen.News && currentScreen != Screen.Magazine && currentScreen != Screen.Video) {
                                IconButton(onClick = { currentScreen = Screen.ForYou; searchQuery = "" }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                DynamicLogo(modifier = Modifier.height(36.dp).width(60.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Slim Search Bar
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (searchQuery.isEmpty()) {
                                                Text("Search", fontSize = 14.sp, color = Color.Gray)
                                            }
                                            innerTextField()
                                        }
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = { /* TODO: Notifications */ }) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", modifier = Modifier.size(24.dp), tint = Color.Gray)
                            }
                        }
                        
                        // Connectivity Banners
                        if (!isOnline) {
                            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer) {
                                Text("Offline. Showing cached content.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        } else if (showBackOnlineBanner) {
                            Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF4CAF50)) {
                                Text("Back online. Feed updated.", color = Color.White, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    val items = listOf(Screen.Menu, Screen.News, Screen.ForYou, Screen.Magazine, Screen.Video)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                when (val icon = screen.icon) {
                                    is Int -> Icon(painter = painterResource(id = icon), contentDescription = screen.title, modifier = Modifier.size(24.dp))
                                    is ImageVector -> Icon(imageVector = icon, contentDescription = screen.title, modifier = Modifier.size(24.dp))
                                }
                            },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            selected = currentScreen == screen,
                            onClick = {
                                if (screen == Screen.Menu) showMenuSheet = true
                                else { currentScreen = screen; selectedNewsHash = null }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            // --- MAIN CONTENT AREA ---
            // We use the top padding to ensure content starts AFTER the appbar
            Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())) {
                when (currentScreen) {
                    Screen.News -> NewsScreen(onNewsClick = { selectedNewsHash = it })
                    Screen.Magazine -> MagazineScreen()
                    Screen.Video -> VideoScreen()
                    Screen.ForYou -> ForYouScreen(onNewsClick = { selectedNewsHash = it })
                    Screen.AccountSettings -> AccountScreen(onLogout = onLogout)
                    Screen.Addresses -> AddressScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Bookmarks -> BookmarkScreen(onNewsClick = { selectedNewsHash = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.Liked -> LikedScreen(onNewsClick = { selectedNewsHash = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.History -> HistoryScreen(onNewsClick = { selectedNewsHash = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.MyComments -> UserCommentsScreen(onNewsClick = { selectedNewsHash = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.MyReviews -> UserReviewsScreen(onNewsClick = { selectedNewsHash = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.Orders -> OrderScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Subscription -> SubscriptionScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Preferences -> PreferencesScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Exclusive -> ExclusiveScreen(onNewsClick = { selectedNewsHash = it })
                    Screen.Support -> SupportScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.QrScanner -> QrScannerScreen(
                        onQrScanned = { hash ->
                            selectedNewsHash = hash
                            currentScreen = Screen.ForYou 
                        },
                        onBack = { currentScreen = Screen.ForYou }
                    )
                    Screen.SearchResults -> SearchPageContent(isLoading = isSearching, results = searchResults, query = searchQuery, onNewsClick = { selectedNewsHash = it })
                    else -> NewsScreen(onNewsClick = { selectedNewsHash = it })
                }
            }
        }
    }

    if (showMenuSheet) {
        ModalBottomSheet(onDismissRequest = { showMenuSheet = false }, sheetState = sheetState, containerColor = Color.White) {
            MenuScreen(onMenuItemClick = { title ->
                showMenuSheet = false
                when (title) {
                    "My Account" -> currentScreen = Screen.AccountSettings
                    "Addresses" -> currentScreen = Screen.Addresses
                    "Bookmarks" -> currentScreen = Screen.Bookmarks
                    "Liked" -> currentScreen = Screen.Liked
                    "History" -> currentScreen = Screen.History
                    "My Comments" -> currentScreen = Screen.MyComments
                    "My Reviews" -> currentScreen = Screen.MyReviews
                    "My Orders" -> currentScreen = Screen.Orders
                    "Subscription" -> currentScreen = Screen.Subscription
                    "Preferences" -> currentScreen = Screen.Preferences
                    "Exclusive" -> currentScreen = Screen.Exclusive
                    "Support" -> currentScreen = Screen.Support
                    "Ask Kivaa" -> currentScreen = Screen.AskKivaa
                    "QR Scanner" -> currentScreen = Screen.QrScanner
                }
            })
        }
    }
}

@Composable
fun SearchPageContent(isLoading: Boolean, results: List<NewsItem>, query: String, onNewsClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Search results for \"$query\"", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No results found.", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(results) { item -> NewsCard(item = item, onClick = { item.hash?.let { onNewsClick(it) } }) }
            }
        }
    }
}
