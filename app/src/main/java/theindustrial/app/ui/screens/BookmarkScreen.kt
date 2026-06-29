package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(onNewsClick: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value

    var bookmarkList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Intercept system back button
    BackHandler {
        onBack()
    }

    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val response = RetrofitInstance.api.viewBookmarks(appKey!!.trim(), userId)
                if (response.isSuccessful) {
                    bookmarkList = response.body()?.responseDetails ?: emptyList()
                } else {
                    errorMessage = "Failed to load bookmarks"
                }
            } catch (e: Exception) {
                errorMessage = "Network error"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (bookmarkList.isEmpty()) {
                Text(text = "No bookmarked articles yet.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookmarkList) { item ->
                        NewsCard(
                            item = item,
                            isLiked = false,
                            isBookmarked = true,
                            onClick = { item.id?.let { onNewsClick(it) } },
                            onLikeClick = {},
                            onBookmarkClick = {},
                            onShareClick = {}
                        )
                    }
                }
            }
        }
    }
}
