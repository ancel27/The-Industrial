package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    var commentsList by remember { mutableStateOf<List<CommentDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Intercept system back button
    BackHandler {
        onBack()
    }

    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val response = RetrofitInstance.api.viewUserComments(appKey!!.trim(), userId)
                if (response.isSuccessful) {
                    commentsList = response.body()?.responseDetails ?: emptyList()
                } else {
                    errorMessage = "Failed to load comments"
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
                title = { Text("My Comments", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else if (commentsList.isEmpty()) {
                Text(text = "No comments yet.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(commentsList) { commentItem ->
                        UserCommentCard(commentItem, onNewsClick)
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Linked Content Title
            Text(
                text = item.content?.title ?: "Original Article",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { item.content?.id?.let { onNewsClick(it) } }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // The Comment
            Text(
                text = item.comment ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
            Text(
                text = item.readAt ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
