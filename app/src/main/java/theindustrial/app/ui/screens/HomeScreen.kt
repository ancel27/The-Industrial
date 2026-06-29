package theindustrial.app.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import theindustrial.app.R
import theindustrial.app.ui.theme.DynamicLogo

sealed class Screen(val route: String, val title: String, val icon: Any) {
    object Menu : Screen("menu", "Menu", R.drawable.menu)
    object News : Screen("news", "News", R.drawable.news)
    object Magazine : Screen("magazine", "Magazine", R.drawable.mag2)
    object Video : Screen("video", "Video", R.drawable.video)
    object ForYou : Screen("foryou", "For You", Icons.Default.Home)
    object AccountSettings : Screen("settings", "Account Settings", R.drawable.user)
    
    // Virtual screens for personalized content
    object Bookmarks : Screen("bookmarks", "Bookmarks", Icons.Default.Bookmark)
    object Liked : Screen("liked", "Liked Content", Icons.Default.ThumbUp)
    object History : Screen("history", "Reading History", Icons.Default.History)
    object MyComments : Screen("comments", "My Comments", Icons.Default.ChatBubble)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.News) }
    var selectedNewsId by remember { mutableStateOf<Int?>(null) }
    var showMenuSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background // Dynamic Background
        ) {
            MenuScreen(
                onMenuItemClick = { title ->
                    showMenuSheet = false
                    when (title) {
                        "Account Settings" -> currentScreen = Screen.AccountSettings
                        "Bookmarks" -> currentScreen = Screen.Bookmarks
                        "Liked" -> currentScreen = Screen.Liked
                        "History" -> currentScreen = Screen.History
                        "My Comments" -> currentScreen = Screen.MyComments
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
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        DynamicLogo(modifier = Modifier.height(80.dp).width(100.dp))
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Search */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* TODO: QR Code */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.qrcode),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* TODO: Notifications */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.bell),
                                contentDescription = "Notifications",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background
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
                            label = { Text(screen.title) },
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
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.News -> NewsScreen(onNewsClick = { selectedNewsId = it })
                    Screen.Magazine -> MagazineScreen()
                    Screen.Video -> VideoScreen()
                    Screen.ForYou -> ForYouScreen()
                    Screen.AccountSettings -> AccountScreen(onLogout = onLogout)
                    Screen.Bookmarks -> BookmarkScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.News })
                    Screen.Liked -> LikedScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.News })
                    Screen.History -> HistoryScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.News })
                    Screen.MyComments -> UserCommentsScreen(onNewsClick = { selectedNewsId = it }, onBack = { currentScreen = Screen.News })
                    else -> NewsScreen(onNewsClick = { selectedNewsId = it })
                }
            }
        }
    }
}
