package kivaa.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.CommentDetail
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(onNewsClick: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var allReviews by remember { mutableStateOf<List<CommentDetail>>(emptyList()) }
    var currentPage by remember { mutableIntStateOf(1) }
    var isEndReached by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isFetchingNextPage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank() && userId != null) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                // Following the pattern used for comments/history
                val response = RetrofitInstance.api.viewUserReviews(appKey!!.trim(), userId, page = pageToFetch, limit = 30)
                if (response.isSuccessful) {
                    val freshReviews = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        allReviews = freshReviews
                        currentPage = 1
                        isEndReached = freshReviews.size < 30
                        errorMessage = null
                    } else {
                        if (freshReviews.isNotEmpty()) {
                            allReviews = allReviews + freshReviews
                            currentPage = pageToFetch
                        }
                        isEndReached = freshReviews.size < 30
                    }
                }
            } catch (e: Exception) {
                if (allReviews.isEmpty()) errorMessage = "Network error"
            }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchData(true)
        isLoading = false
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (isLoading && allReviews.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && allReviews.isEmpty()) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (allReviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reviews made yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "My Reviews",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 0.dp)
                        )
                    }
                    itemsIndexed(allReviews) { index, reviewItem ->
                        if (index >= allReviews.size - 1 && !isEndReached && !isFetchingNextPage) {
                            LaunchedEffect(Unit) {
                                isFetchingNextPage = true
                                fetchData(false)
                                isFetchingNextPage = false
                            }
                        }

                        UserCommentCard(reviewItem, onNewsClick)
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
