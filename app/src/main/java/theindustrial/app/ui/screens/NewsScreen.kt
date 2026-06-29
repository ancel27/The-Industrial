package theindustrial.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.*
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
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager
import theindustrial.app.utils.ShareUtils

@Composable
fun NewsScreen(onNewsClick: (Int) -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId by ThemeManager.userId 
    val scope = rememberCoroutineScope()
    
    var newsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var likedHashes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var bookmarkedHashes by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank()) {
            isLoading = true
            try {
                val cleanKey = appKey!!.trim()
                
                // 1. Fetch News
                val newsResponse = RetrofitInstance.api.getNews(cleanKey)
                if (newsResponse.isSuccessful) {
                    newsList = newsResponse.body()?.responseDetails ?: emptyList()
                }

                // 2. Fetch Likes & Bookmarks if logged in
                if (userId != null) {
                    val likesRes = RetrofitInstance.api.viewLikes(cleanKey, userId!!)
                    if (likesRes.isSuccessful) {
                        likedHashes = likesRes.body()?.responseDetails?.mapNotNull { it.hash }?.toSet() ?: emptySet()
                    }

                    val bookmarksRes = RetrofitInstance.api.viewBookmarks(cleanKey, userId!!)
                    if (bookmarksRes.isSuccessful) {
                        bookmarkedHashes = bookmarksRes.body()?.responseDetails?.mapNotNull { it.hash }?.toSet() ?: emptySet()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Network error"
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(newsList) { newsItem ->
                    val isLiked = likedHashes.contains(newsItem.hash)
                    val isBookmarked = bookmarkedHashes.contains(newsItem.hash)

                    NewsCard(
                        item = newsItem, 
                        isLiked = isLiked,
                        isBookmarked = isBookmarked,
                        onClick = { newsItem.id?.let { onNewsClick(it) } },
                        onLikeClick = {
                            if (userId != null && appKey != null && newsItem.hash != null) {
                                scope.launch {
                                    val hash = newsItem.hash!!
                                    val key = appKey!!.trim()
                                    val uId = userId!!
                                    
                                    val response = if (isLiked) {
                                        RetrofitInstance.api.unlike(key, uId, "content", hash, key, uId, "content", hash)
                                    } else {
                                        RetrofitInstance.api.like(key, uId, "content", hash, key, uId, "content", hash)
                                    }
                                    
                                    if (response.isSuccessful && response.body()?.responseHeader == 200) {
                                        likedHashes = if (isLiked) likedHashes - hash else likedHashes + hash
                                    }
                                }
                            }
                        },
                        onBookmarkClick = {
                            if (userId != null && appKey != null && newsItem.hash != null) {
                                scope.launch {
                                    val hash = newsItem.hash!!
                                    val key = appKey!!.trim()
                                    val uId = userId!!
                                    
                                    val response = if (isBookmarked) {
                                        RetrofitInstance.api.unbookmark(key, uId, "content", hash, key, uId, "content", hash)
                                    } else {
                                        RetrofitInstance.api.bookmark(key, uId, "content", hash, key, uId, "content", hash)
                                    }
                                    
                                    if (response.isSuccessful && response.body()?.responseHeader == 200) {
                                        bookmarkedHashes = if (isBookmarked) bookmarkedHashes - hash else bookmarkedHashes + hash
                                    }
                                }
                            }
                        },
                        onShareClick = {
                            ShareUtils.shareLink(context, newsItem.title, newsItem.link)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    item: NewsItem, 
    isLiked: Boolean,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Like",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onBookmarkClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
