package theindustrial.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.NewsDetailItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.DynamicLogo
import theindustrial.app.ui.theme.ThemeManager
import theindustrial.app.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(newsId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value 
    
    var detailItem by remember { mutableStateOf<NewsDetailItem?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Comment State
    var commentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Intercept system back button
    BackHandler {
        onBack()
    }

    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank()) {
            try {
                val cleanKey = appKey!!.trim()
                
                // 1. Fetch Details
                val response = RetrofitInstance.api.getNewsDetail(newsId, cleanKey)
                if (response.isSuccessful) {
                    detailItem = response.body()?.details?.firstOrNull()
                }

                // 2. Check Like/Bookmark status
                if (userId != null) {
                    val likesRes = RetrofitInstance.api.viewLikes(cleanKey, userId!!)
                    if (likesRes.isSuccessful) {
                        isLiked = likesRes.body()?.responseDetails?.any { it.id == newsId } ?: false
                    }

                    val bookmarksRes = RetrofitInstance.api.viewBookmarks(cleanKey, userId!!)
                    if (bookmarksRes.isSuccessful) {
                        isBookmarked = bookmarksRes.body()?.responseDetails?.any { it.id == newsId } ?: false
                    }
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
                title = { DynamicLogo(modifier = Modifier.height(80.dp).width(100.dp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else {
                detailItem?.let { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = item.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = item.startDate ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ActionIcon(
                                    icon = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, 
                                    description = "Like",
                                    active = isLiked,
                                    onClick = {
                                        if (userId != null && appKey != null) {
                                            scope.launch {
                                                val idStr = newsId.toString()
                                                val key = appKey!!.trim()
                                                val uId = userId!!
                                                val res = if (isLiked) {
                                                    RetrofitInstance.api.unlike(key, uId, "news", idStr, key, uId, "news", idStr)
                                                } else {
                                                    RetrofitInstance.api.like(key, uId, "news", idStr, key, uId, "news", idStr)
                                                }
                                                if (res.isSuccessful && res.body()?.responseHeader == 200) {
                                                    isLiked = !isLiked
                                                }
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                ActionIcon(
                                    icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, 
                                    description = "Bookmark",
                                    active = isBookmarked,
                                    onClick = {
                                        if (userId != null && appKey != null) {
                                            scope.launch {
                                                val idStr = newsId.toString()
                                                val key = appKey!!.trim()
                                                val uId = userId!!
                                                val res = if (isBookmarked) {
                                                    RetrofitInstance.api.unbookmark(key, uId, "news", idStr, key, uId, "news", idStr)
                                                } else {
                                                    RetrofitInstance.api.bookmark(key, uId, "news", idStr, key, uId, "news", idStr)
                                                }
                                                if (res.isSuccessful && res.body()?.responseHeader == 200) {
                                                    isBookmarked = !isBookmarked
                                                }
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                ActionIcon(
                                    icon = Icons.Default.Share, 
                                    description = "Share",
                                    active = false,
                                    onClick = { 
                                        ShareUtils.shareLink(context, item.title, item.link) 
                                    }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Render HTML Content using WebView
                            val bgColor = MaterialTheme.colorScheme.background.toArgb()
                            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
                            
                            val htmlContent = """
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                <style>
                                    body {
                                        background-color: ${String.format("#%06X", 0xFFFFFF and bgColor)};
                                        color: ${String.format("#%06X", 0xFFFFFF and textColor)};
                                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                        line-height: 1.6;
                                        margin: 0;
                                        padding: 0;
                                        width: 100vw;
                                        word-wrap: break-word;
                                    }
                                    p, span, div { 
                                        font-size: 18px !important; 
                                        line-height: 1.6 !important;
                                        width: auto !important;
                                        max-width: 100% !important;
                                        display: block;
                                    }
                                    img, video, iframe, embed, object { 
                                        max-width: 100% !important; 
                                        height: auto !important; 
                                        border-radius: 8px; 
                                        margin: 16px 0; 
                                        display: block;
                                    }
                                    a { color: ${String.format("#%06X", 0xFFFFFF and primaryColor)}; text-decoration: none; }
                                    b, strong { font-weight: bold; }
                                    * { box-sizing: border-box; }
                                </style>
                                </head>
                                <body>
                                    ${item.fullDescription ?: ""}
                                </body>
                                </html>
                            """.trimIndent()

                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = WebViewClient()
                                        setBackgroundColor(0) // Transparent
                                        settings.apply {
                                            javaScriptEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                                            textZoom = 100 
                                        }
                                    }
                                },
                                update = { webView ->
                                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                                },
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(24.dp))

                            // --- Comments Section ---
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Write a comment...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                enabled = !isPostingComment
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (commentText.isNotBlank() && userId != null && appKey != null && item.id != null) {
                                        scope.launch {
                                            isPostingComment = true
                                            try {
                                                val cleanAppKey = appKey!!.trim()
                                                val cleanComment = commentText.trim()
                                                val entityId = item.id.toString()
                                                val entityType = "news" 

                                                // LOG TO CONSOLE (for direct monitoring)
                                                println("DEBUG: Button Clicked - User: ${userId}, AppKey: true, ID: ${item.id}")

                                                val response = RetrofitInstance.api.addComment(
                                                    appKey = cleanAppKey,
                                                    userId = userId!!,
                                                    entityType = entityType,
                                                    entityId = entityId,
                                                    comment = cleanComment,
                                                    appKeyQ = cleanAppKey,
                                                    userIdQ = userId!!,
                                                    entityTypeQ = entityType,
                                                    entityIdQ = entityId,
                                                    commentQ = cleanComment
                                                )
                                                if (response.isSuccessful && response.body()?.responseDetails?.firstOrNull()?.success == true) {
                                                    commentText = ""
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isPostingComment = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                enabled = commentText.isNotBlank() && !isPostingComment,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isPostingComment) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Post Comment")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    description: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(20.dp),
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
        }
    }
}
