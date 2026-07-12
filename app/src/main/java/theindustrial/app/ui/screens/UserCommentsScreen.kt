package theindustrial.app.ui.screens

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
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.CommentDetail
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCommentsScreen(onNewsClick: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var allComments by remember { mutableStateOf<List<CommentDetail>>(emptyList()) }
    var currentPage by remember { mutableStateOf(1) }
    var isEndReached by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isFetchingNextPage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    val fetchData = suspend { isNewFetch: Boolean ->
        if (!appKey.isNullOrBlank() && userId != null) {
            val pageToFetch = if (isNewFetch) 1 else currentPage + 1
            try {
                val response = RetrofitInstance.api.viewUserComments(appKey!!.trim(), userId, page = pageToFetch, limit = 30)
                if (response.isSuccessful) {
                    val freshComments = response.body()?.responseDetails ?: emptyList()
                    
                    if (isNewFetch) {
                        allComments = freshComments
                        currentPage = 1
                        isEndReached = freshComments.size < 30
                        errorMessage = null
                    } else {
                        if (freshComments.isNotEmpty()) {
                            allComments = allComments + freshComments
                            currentPage = pageToFetch
                        }
                        isEndReached = freshComments.size < 30
                    }
                }
            } catch (e: Exception) {
                if (allComments.isEmpty()) errorMessage = "Network error"
            }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchData(true)
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null && allComments.isEmpty()) {
            Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
        } else if (allComments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No comments made yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "My Comments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                itemsIndexed(allComments) { index, commentItem ->
                    if (index >= allComments.size - 1 && !isEndReached && !isFetchingNextPage) {
                        LaunchedEffect(Unit) {
                            isFetchingNextPage = true
                            fetchData(false)
                            isFetchingNextPage = false
                        }
                    }

                    UserCommentCard(commentItem, onNewsClick)
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

@Composable
fun UserCommentCard(item: CommentDetail, onNewsClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content?.title ?: "Original Article",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { item.content?.id?.let { onNewsClick(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.comment ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.readAt ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
