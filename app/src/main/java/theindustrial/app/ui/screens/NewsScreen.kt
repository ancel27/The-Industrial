package theindustrial.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager
import theindustrial.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(onNewsClick: (Int) -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val cachedNews by preferenceManager.cachedNews.collectAsState(initial = null)
    val userId by ThemeManager.userId 
    val scope = rememberCoroutineScope()
    
    var newsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
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
            lastVisibleItemIndex >= newsList.size - 5 && !isEndReached && !isFetchingNextPage && !isLoading && !isRefreshing && newsList.isNotEmpty()
        }
    }

    LaunchedEffect(cachedNews) {
        if (!cachedNews.isNullOrEmpty() && newsList.isEmpty()) {
            newsList = cachedNews!!
        }
    }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank()) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getNews(cleanKey, page = pageToFetch, limit = 30)
                
                if (response.isSuccessful) {
                    val freshNews = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        newsList = freshNews
                        preferenceManager.saveCachedNews(freshNews)
                        currentPage = 1
                        isEndReached = freshNews.size < 30
                    } else {
                        if (freshNews.isNotEmpty()) {
                            newsList = newsList + freshNews
                            currentPage = pageToFetch
                        }
                        isEndReached = freshNews.size < 30
                    }
                    errorMessage = null
                }
            } catch (e: Exception) {
                if (newsList.isEmpty()) errorMessage = "Network error"
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
        if (newsList.isEmpty()) isLoading = true
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
            if (isLoading && !isRefreshing && newsList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && newsList.isEmpty()) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (newsList.isEmpty() && !isLoading) {
                ComingSoonView(categoryName = "News")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(newsList) { _, newsItem ->
                        NewsCard(
                            item = newsItem, 
                            onClick = { newsItem.id?.let { onNewsClick(it) } }
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

@Composable
fun PaginationFooter(currentPage: Int, totalItems: Int, onPageClick: (Int) -> Unit) {
    val totalPages = (totalItems + 9) / 10
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalPages) {
            Surface(
                onClick = { onPageClick(i) },
                shape = CircleShape,
                color = if (currentPage == i) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = if (currentPage == i) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                modifier = Modifier.size(40.dp).padding(horizontal = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = i.toString(),
                        color = if (currentPage == i) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    item: NewsItem, 
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        val cdn = config?.cdnUrl ?: ""
        if (cdn.endsWith("/")) "${cdn}content/placeholder.jpg" else "${cdn}/content/placeholder.jpg"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            AsyncImage(
                model = item.image,
                error = rememberAsyncImagePainter(model = placeholderUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.briefIntro ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!item.startDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = DateUtils.formatDate(item.startDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
