package theindustrial.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExclusiveScreen(onNewsClick: (Int) -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId by ThemeManager.userId
    val scope = rememberCoroutineScope()
    
    var exclusiveList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var currentPage by remember { mutableIntStateOf(1) }
    var isEndReached by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isFetchingNextPage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val ptrState = rememberPullToRefreshState()

    // --- Load More Detection ---
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= exclusiveList.size - 5 && !isEndReached && !isFetchingNextPage && !isLoading && !isRefreshing && exclusiveList.isNotEmpty()
        }
    }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank()) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getExclusives(cleanKey, page = pageToFetch, limit = 30)
                
                if (response.isSuccessful) {
                    val freshData = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        exclusiveList = freshData
                        currentPage = 1
                        isEndReached = freshData.size < 30
                    } else {
                        if (freshData.isNotEmpty()) {
                            exclusiveList = exclusiveList + freshData
                            currentPage = pageToFetch
                        }
                        isEndReached = freshData.size < 30
                    }
                    errorMessage = null
                }
            } catch (e: Exception) {
                if (exclusiveList.isEmpty()) errorMessage = "Network error"
            }
        }
    }

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
            if (isLoading && !isRefreshing && exclusiveList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && exclusiveList.isEmpty()) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (exclusiveList.isEmpty() && !isLoading) {
                ComingSoonView(categoryName = "Exclusive")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Prime Exclusives",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    itemsIndexed(exclusiveList) { _, item ->
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
