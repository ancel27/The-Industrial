package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onNewsClick: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId by ThemeManager.userId
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var likedHashes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var bookmarkedHashes by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    // Initial fetch for likes/bookmarks to sync icon states
    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val cleanKey = appKey!!.trim()
                val likesRes = RetrofitInstance.api.viewLikes(cleanKey, userId!!)
                if (likesRes.isSuccessful) {
                    likedHashes = likesRes.body()?.responseDetails?.mapNotNull { it.hash }?.toSet() ?: emptySet()
                }
                val bookmarksRes = RetrofitInstance.api.viewBookmarks(cleanKey, userId!!)
                if (bookmarksRes.isSuccessful) {
                    bookmarkedHashes = bookmarksRes.body()?.responseDetails?.mapNotNull { it.hash }?.toSet() ?: emptySet()
                }
            } catch (e: Exception) { /* Silently ignore status fetch errors */ }
        }
    }

    val performSearch = {
        if (searchQuery.isNotBlank() && !appKey.isNullOrBlank()) {
            scope.launch {
                isLoading = true
                errorMessage = null
                hasSearched = true
                focusManager.clearFocus()
                try {
                    val cleanKey = appKey!!.trim()
                    val response = RetrofitInstance.api.searchContent(
                        appKey = cleanKey,
                        query = searchQuery.trim(),
                        appKeyQ = cleanKey,
                        queryQ = searchQuery.trim()
                    )
                    if (response.isSuccessful) {
                        searchResults = response.body()?.responseDetails ?: emptyList()
                    } else {
                        errorMessage = "Search failed. Please try again."
                    }
                } catch (e: Exception) {
                    errorMessage = "Network error. Please check your connection."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search articles, news...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    
                    IconButton(onClick = { performSearch() }, enabled = searchQuery.isNotBlank()) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (hasSearched && searchResults.isEmpty()) {
                Text(text = "No results found for \"$searchQuery\"", modifier = Modifier.align(Alignment.Center))
            } else if (!hasSearched) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Search for industrial news and insights", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults) { newsItem ->
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
                                        val res = if (isLiked) {
                                            RetrofitInstance.api.unlike(key, uId, "content", hash, key, uId, "content", hash)
                                        } else {
                                            RetrofitInstance.api.like(key, uId, "content", hash, key, uId, "content", hash)
                                        }
                                        if (res.isSuccessful) likedHashes = if (isLiked) likedHashes - hash else likedHashes + hash
                                    }
                                }
                            },
                            onBookmarkClick = {
                                if (userId != null && appKey != null && newsItem.hash != null) {
                                    scope.launch {
                                        val hash = newsItem.hash!!
                                        val key = appKey!!.trim()
                                        val uId = userId!!
                                        val res = if (isBookmarked) {
                                            RetrofitInstance.api.unbookmark(key, uId, "content", hash, key, uId, "content", hash)
                                        } else {
                                            RetrofitInstance.api.bookmark(key, uId, "content", hash, key, uId, "content", hash)
                                        }
                                        if (res.isSuccessful) bookmarkedHashes = if (isBookmarked) bookmarkedHashes - hash else bookmarkedHashes + hash
                                    }
                                }
                            },
                            onShareClick = {
                                theindustrial.app.utils.ShareUtils.shareLink(context, newsItem.title, newsItem.link)
                            }
                        )
                    }
                }
            }
        }
    }
}
