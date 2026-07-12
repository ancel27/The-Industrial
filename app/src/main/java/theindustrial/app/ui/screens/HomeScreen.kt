package theindustrial.app.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import theindustrial.app.R
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.DynamicLogo
import theindustrial.app.utils.NetworkUtils

sealed class Screen(val route: String, val title: String, val icon: Any) {
    object Menu : Screen("menu", "Menu", R.drawable.menu)
    object News : Screen("news", "News", R.drawable.news)
    object Magazine : Screen("magazine", "Magazine", R.drawable.mag2)
    object Video : Screen("video", "Video", R.drawable.video)
    object ForYou : Screen("foryou", "For You", Icons.Default.Home)
    object AccountSettings : Screen("settings", "My Account", R.drawable.user)
    
    // Virtual screens for personalized content
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
    object AskKivaa : Screen("ask_kivaa", "Ask Kivaa", Icons.Default.ChatBubble)
    object QrScanner : Screen("qr_scanner", "QR Scanner", R.drawable.qrcode)
    object SearchResults : Screen("search_results", "Search Results", Icons.Default.Search)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val focusManager = LocalFocusManager.current
    val isOnline by NetworkUtils.observeConnectivity(context).collectAsState(initial = true)
    
    // --- Connectivity Transition State ---
    var previousOnlineState by remember { mutableStateOf(isOnline) }
    var showBackOnlineBanner by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!previousOnlineState && isOnline) {
            // Transition: Offline -> Online
            showBackOnlineBanner = true
            kotlinx.coroutines.delay(3000)
            showBackOnlineBanner = false
        }
        previousOnlineState = isOnline
    }
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ForYou) }
    var selectedNewsId by remember { mutableStateOf<Int?>(null) }
    var showMenuSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // --- Search State ---
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val preferenceManager = remember { theindustrial.app.data.local.PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)

    // --- Notification Permission Handling ---
    var showNotificationDialog by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> showNotificationDialog = false }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) showNotificationDialog = true
        }
    }

    val performSearch = {
        if (searchQuery.isNotBlank() && !appKey.isNullOrBlank()) {
            scope.launch {
                isSearching = true
                currentScreen = Screen.SearchResults
                focusManager.clearFocus()
                try {
                    val cleanKey = appKey!!.trim()
                    val response = RetrofitInstance.api.searchContent(
                        appKey = cleanKey, query = searchQuery.trim(),
                        appKeyQ = cleanKey, queryQ = searchQuery.trim()
                    )
                    if (response.isSuccessful) {
                        searchResults = response.body()?.responseDetails ?: emptyList()
                    }
                } catch (e: Exception) { } finally {
                    isSearching = false
                }
            }
        }
    }

    // --- App Quit Logic on For You Page ---
    BackHandler {
        if (selectedNewsId != null) {
            selectedNewsId = null
        } else if (currentScreen != Screen.ForYou) {
            currentScreen = Screen.ForYou
            searchQuery = ""
        } else {
            activity?.finish()
        }
    }

    if (showNotificationDialog) {
        NotificationPermissionDialog(
            onAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    showNotificationDialog = false
                }
            },
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = sheetState,
            containerColor = Color.White // Enforced White
        ) {
            MenuScreen(
                onMenuItemClick = { title ->
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
                }
            )
        }
    }

    if (selectedNewsId != null) {
        NewsDetailScreen(
            newsId = selectedNewsId!!,
            onBack = { selectedNewsId = null }
        )
    } else if (currentScreen == Screen.AskKivaa) {
        KivaaChatScreen(onBack = { currentScreen = Screen.ForYou })
    } else {
        Scaffold(
            topBar = {
                Column {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp), // Tighter bottom padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable { 
                                        currentScreen = Screen.ForYou 
                                        searchQuery = ""
                                    }
                            ) {
                                DynamicLogo(modifier = Modifier.height(52.dp).width(80.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp) // Height to prevent clipping
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(26.dp)
                                    ),
                                placeholder = { 
                                    Text(
                                        "Search",
                                        fontSize = 14.sp
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(26.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 14.sp, 
                                    lineHeight = 18.sp // Increased line height for clear visibility
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { performSearch() })
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = { /* TODO: Notifications */ }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                    
                    if (!isOnline) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "You are currently offline. Showing cached content.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    } else if (showBackOnlineBanner) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50) // Branded Green
                        ) {
                            Text(
                                text = "You are back online. Feed updated.",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
            NavigationBar(
                containerColor = Color.White // Enforced White
            ) {
                val items = listOf(Screen.Menu, Screen.News, Screen.ForYou, Screen.Magazine, Screen.Video)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                when (val icon = screen.icon) {
                                    is Int -> Icon(
                                        painter = painterResource(id = icon), 
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    is ImageVector -> Icon(
                                        imageVector = icon, 
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            selected = currentScreen == screen,
                            onClick = {
                                if (screen == Screen.Menu) {
                                    showMenuSheet = true
                                } else {
                                    currentScreen = screen
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())) {
                when (currentScreen) {
                    Screen.News -> NewsScreen(onNewsClick = { selectedNewsId = it })
                    Screen.Magazine -> MagazineScreen()
                    Screen.Video -> VideoScreen()
                    Screen.ForYou -> ForYouScreen(onNewsClick = { selectedNewsId = it })
                    Screen.AccountSettings -> AccountScreen(onLogout = onLogout)
                    Screen.Addresses -> AddressScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Bookmarks -> BookmarkScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.Liked -> LikedScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.History -> HistoryScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.MyComments -> UserCommentsScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.MyReviews -> UserReviewsScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.ForYou })
                    Screen.Orders -> OrderScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Subscription -> SubscriptionScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Preferences -> PreferencesScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.Exclusive -> ExclusiveScreen(onNewsClick = { selectedNewsId = it })
                    Screen.Support -> SupportScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.AskKivaa -> KivaaChatScreen(onBack = { currentScreen = Screen.ForYou })
                    Screen.QrScanner -> QrScannerScreen(
                        onQrScanned = { _ ->
                            currentScreen = Screen.ForYou 
                        },
                        onBack = { currentScreen = Screen.ForYou }
                    )
                    Screen.SearchResults -> {
                        SearchPageContent(
                            isLoading = isSearching,
                            results = searchResults,
                            query = searchQuery,
                            onNewsClick = { selectedNewsId = it }
                        )
                    }
                    else -> NewsScreen(onNewsClick = { selectedNewsId = it })
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionDialog(onAllow: () -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Stay one step ahead!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Get instant updates on breaking news, trending videos and exclusive stories.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                        ) {
                            Text("Don't Allow", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = onAllow,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
                        ) {
                            Text("Allow", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchPageContent(isLoading: Boolean, results: List<NewsItem>, query: String, onNewsClick: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (results.isEmpty()) {
            Text("No results for \"$query\"", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(results) { item ->
                    NewsCard(
                        item = item,
                        onClick = { item.id?.let { onNewsClick(it) } }
                    )
                }
            }
        }
    }
}
