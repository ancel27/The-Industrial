package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedScreen(onNewsClick: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var allLikes by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var currentPage by remember { mutableIntStateOf(1) }
    var isEndReached by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isFetchingNextPage by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val ptrState = rememberPullToRefreshState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= allLikes.size - 5 && !isEndReached && !isFetchingNextPage && !isLoading && !isRefreshing && allLikes.isNotEmpty()
        }
    }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank() && userId != null) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                val response = RetrofitInstance.api.viewLikes(appKey!!.trim(), userId, page = pageToFetch, limit = 30)
                if (response.isSuccessful) {
                    val freshLikes = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        allLikes = freshLikes
                        currentPage = 1
                        isEndReached = freshLikes.size < 30
                    } else {
                        if (freshLikes.isNotEmpty()) {
                            allLikes = allLikes + freshLikes
                            currentPage = pageToFetch
                        }
                        isEndReached = freshLikes.size < 30
                    }
                }
            } catch (e: Exception) { }
        }
    }

    BackHandler { onBack() }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            isFetchingNextPage = true
            fetchData(false)
            isFetchingNextPage = false
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchData(true)
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            state = ptrState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    fetchData(true)
                    isRefreshing = false
                }
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = ptrState,
                    isRefreshing = isRefreshing,
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && !isRefreshing && allLikes.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (allLikes.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No articles liked yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Liked Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        itemsIndexed(allLikes) { _, item ->
                            NewsCard(
                                item = item,
                                onClick = { item.id?.let { onNewsClick(it) } }
                            )
                        }
                        
                        if (isFetchingNextPage) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
