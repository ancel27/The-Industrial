package theindustrial.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import theindustrial.app.R
import theindustrial.app.ui.theme.DynamicLogo

sealed class Screen(val route: String, val title: String, val iconResId: Int) {
    object Menu : Screen("menu", "Menu", R.drawable.menu)
    object News : Screen("news", "News", R.drawable.news)
    object Magazine : Screen("magazine", "Magazine", R.drawable.mag2)
    object Video : Screen("video", "Video", R.drawable.video)
    object Account : Screen("account", "My Account", R.drawable.user)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.News) }
    var selectedNewsId by remember { mutableStateOf<Int?>(null) }
    var showMenuSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = sheetState
        ) {
            MenuScreen(onMenuItemClick = { title ->
                showMenuSheet = false
                // Handle navigation for items inside the menu if needed
            })
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
                        DynamicLogo(modifier = Modifier.height(85.dp).width(100.dp))
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
                NavigationBar {
                    val items = listOf(Screen.Menu, Screen.News, Screen.Magazine, Screen.Video, Screen.Account)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    painter = painterResource(id = screen.iconResId), 
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                ) 
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
                    Screen.Account -> AccountScreen()
                    else -> NewsScreen(onNewsClick = { selectedNewsId = it })
                }
            }
        }
    }
}
