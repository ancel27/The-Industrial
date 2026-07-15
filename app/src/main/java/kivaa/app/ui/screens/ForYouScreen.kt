package kivaa.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.NewsItem
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager

enum class ContentCategory(val title: String) {
    FOR_YOU("For You"),
    INTERVIEWS("Interviews"),
    ARTICLES("Articles"),
    CASE_STUDY("Case Study")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouScreen(onNewsClick: (Int) -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId by ThemeManager.userId
    val scope = rememberCoroutineScope()

    // Cache observers
    val cachedForYou by preferenceManager.cachedForYou.collectAsState(initial = null)
    val cachedArticles by preferenceManager.cachedArticles.collectAsState(initial = null)
    val cachedInterviews by preferenceManager.cachedInterviews.collectAsState(initial = null)
    val cachedCaseStudies by preferenceManager.cachedCaseStudies.collectAsState(initial = null)

    var selectedCategory by remember { mutableStateOf(ContentCategory.FOR_YOU) }
    var contentList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    
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
            lastVisibleItemIndex >= contentList.size - 5 && !isEndReached && !isFetchingNextPage && !isLoading && !isRefreshing && contentList.isNotEmpty()
        }
    }

    // Instant Cache Loading
    LaunchedEffect(selectedCategory, cachedForYou, cachedArticles, cachedInterviews, cachedCaseStudies) {
        val cache = when (selectedCategory) {
            ContentCategory.FOR_YOU -> cachedForYou
            ContentCategory.ARTICLES -> cachedArticles
            ContentCategory.INTERVIEWS -> cachedInterviews
            ContentCategory.CASE_STUDY -> cachedCaseStudies
        }
        if (!cache.isNullOrEmpty() && contentList.isEmpty()) {
            contentList = cache!!
            currentPage = 1
        }
    }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank()) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                val cleanKey = appKey!!.trim()
                val response = when (selectedCategory) {
                    ContentCategory.FOR_YOU -> if (userId != null) RetrofitInstance.api.getForYouContent(cleanKey, userId!!, page = pageToFetch) else RetrofitInstance.api.getNews(cleanKey, page = pageToFetch)
                    ContentCategory.ARTICLES -> RetrofitInstance.api.getArticles(cleanKey, page = pageToFetch)
                    ContentCategory.INTERVIEWS -> RetrofitInstance.api.getInterviews(cleanKey, page = pageToFetch)
                    ContentCategory.CASE_STUDY -> RetrofitInstance.api.getCaseStudies(cleanKey, page = pageToFetch)
                }

                if (response.isSuccessful) {
                    val freshContent = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        contentList = freshContent
                        currentPage = 1
                        isEndReached = freshContent.size < 30
                        errorMessage = null
                        
                        when (selectedCategory) {
                            ContentCategory.FOR_YOU -> preferenceManager.saveCachedForYou(freshContent)
                            ContentCategory.ARTICLES -> preferenceManager.saveCachedArticles(freshContent)
                            ContentCategory.INTERVIEWS -> preferenceManager.saveCachedInterviews(freshContent)
                            ContentCategory.CASE_STUDY -> preferenceManager.saveCachedCaseStudies(freshContent)
                        }
                    } else {
                        if (freshContent.isNotEmpty()) {
                            contentList = contentList + freshContent
                            currentPage = pageToFetch
                        }
                        isEndReached = freshContent.size < 30
                    }
                }
            } catch (e: Exception) {
                if (contentList.isEmpty()) errorMessage = "Network error"
            }
        }
    }

    // Handle Infinite Scroll trigger
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            isFetchingNextPage = true
            fetchData(false)
            isFetchingNextPage = false
        }
    }

    LaunchedEffect(appKey, selectedCategory, userId) {
        if (contentList.isEmpty()) isLoading = true
        fetchData(true)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Category Selection Bar (Minimal Space) ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ContentCategory.entries.toTypedArray()) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { 
                        selectedCategory = category
                        contentList = emptyList() // Trigger fresh fetch
                    }
                )
            }
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
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && !isRefreshing && contentList.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (contentList.isEmpty() && !isLoading) {
                    ComingSoonView(categoryName = selectedCategory.title)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = if (selectedCategory == ContentCategory.FOR_YOU) "Personalized feed" else selectedCategory.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(contentList) { item ->
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

@Composable
fun CategoryChip(category: ContentCategory, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = category.title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ComingSoonView(categoryName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (categoryName == "For You") "Personalized for You" else "$categoryName Content",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Exciting new insights are being prepared. Soon, you'll see curated stories tailored to your interests in $categoryName.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "COMING SOON",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
