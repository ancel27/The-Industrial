package theindustrial.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.CommentDetail
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
    var publicComments by remember { mutableStateOf<List<CommentDetail>>(emptyList()) }
    
    // Report State
    var showReportSheet by remember { mutableStateOf(false) }
    val reportSheetState = rememberModalBottomSheetState()
    
    val scope = rememberCoroutineScope()

    val fetchPublicComments = {
        scope.launch {
            detailItem?.hash?.let { hash ->
                val cleanKey = appKey?.trim() ?: ""
                if (cleanKey.isNotBlank()) {
                    val commentsRes = RetrofitInstance.api.getPublicComments(
                        cleanKey, "content", hash, cleanKey, "content", hash
                    )
                    if (commentsRes.isSuccessful) {
                        publicComments = commentsRes.body()?.responseDetails ?: emptyList()
                    }
                }
            }
        }
    }

    BackHandler { onBack() }

    // Record History after 2 seconds
    LaunchedEffect(newsId, userId, appKey, detailItem) {
        if (!appKey.isNullOrBlank() && userId != null && detailItem?.hash != null) {
            delay(2000)
            try {
                val cleanKey = appKey!!.trim()
                val hash = detailItem!!.hash!!
                RetrofitInstance.api.addHistory(
                    appKey = cleanKey, userId = userId, entityType = "content", entityId = hash,
                    appKeyQ = cleanKey, userIdQ = userId, entityTypeQ = "content", entityIdQ = hash
                )
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank()) {
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getNewsDetail(newsId, cleanKey)
                if (response.isSuccessful) {
                    detailItem = response.body()?.details?.firstOrNull()
                    
                    // Fetch Public Comments using the internal reusable function logic
                    detailItem?.hash?.let { hash ->
                        val commentsRes = RetrofitInstance.api.getPublicComments(
                            cleanKey, "content", hash, cleanKey, "content", hash
                        )
                        if (commentsRes.isSuccessful) {
                            publicComments = commentsRes.body()?.responseDetails ?: emptyList()
                        }
                    }
                }

                if (userId != null) {
                    val likesRes = RetrofitInstance.api.viewLikes(cleanKey, userId!!)
                    if (likesRes.isSuccessful) {
                        isLiked = likesRes.body()?.responseDetails?.any { it.id == newsId || it.hash == detailItem?.hash } ?: false
                    }
                    val bookmarksRes = RetrofitInstance.api.viewBookmarks(cleanKey, userId!!)
                    if (bookmarksRes.isSuccessful) {
                        isBookmarked = bookmarksRes.body()?.responseDetails?.any { it.id == newsId || it.hash == detailItem?.hash } ?: false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Network error"
            } finally {
                isLoading = false
            }
        }
    }

    if (showReportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReportSheet = false },
            sheetState = reportSheetState,
            containerColor = Color.White
        ) {
            ReportContentForm(
                contentTitle = detailItem?.title ?: "",
                onSubmit = { details ->
                    scope.launch {
                        if (userId != null && !appKey.isNullOrBlank()) {
                            try {
                                val cleanKey = appKey!!.trim()
                                val safeMessage = "Reported Item: ${detailItem?.title}. Details: $details"
                                    .replace("\n", " ")
                                
                                val response = RetrofitInstance.api.createTicket(
                                    cleanKey, userId, "content", "Reported Content",
                                    safeMessage,
                                    cleanKey, userId, "content", "Reported Content",
                                    safeMessage
                                )
                                if (response.isSuccessful) {
                                    showReportSheet = false
                                    android.widget.Toast.makeText(context, "Report submitted successfully", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Submission failed", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error submitting report", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    DynamicLogo(modifier = Modifier.height(52.dp).width(80.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else {
                detailItem?.let { item ->
                    val config = ThemeManager.currentConfig.value
                    val placeholderUrl = remember(config) {
                        val cdn = config?.cdnUrl ?: ""
                        if (cdn.endsWith("/")) "${cdn}content/placeholder.jpg" else "${cdn}/content/placeholder.jpg"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = item.image,
                            error = rememberAsyncImagePainter(model = placeholderUrl),
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
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ActionIcon(
                                    icon = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, 
                                    description = "Like",
                                    active = isLiked,
                                    onClick = {
                                        if (userId != null && appKey != null && item.hash != null) {
                                            scope.launch {
                                                val hash = item.hash!!
                                                val key = appKey!!.trim()
                                                val res = if (isLiked) {
                                                    RetrofitInstance.api.unlike(key, userId!!, "content", hash, key, userId!!, "content", hash)
                                                } else {
                                                    RetrofitInstance.api.like(key, userId!!, "content", hash, key, userId!!, "content", hash)
                                                }
                                                if (res.isSuccessful) isLiked = !isLiked
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
                                        if (userId != null && appKey != null && item.hash != null) {
                                            scope.launch {
                                                val hash = item.hash!!
                                                val key = appKey!!.trim()
                                                val res = if (isBookmarked) {
                                                    RetrofitInstance.api.unbookmark(key, userId!!, "content", hash, key, userId!!, "content", hash)
                                                } else {
                                                    RetrofitInstance.api.bookmark(key, userId!!, "content", hash, key, userId!!, "content", hash)
                                                }
                                                if (res.isSuccessful) isBookmarked = !isBookmarked
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                ActionIcon(
                                    icon = Icons.Default.Share, 
                                    description = "Share",
                                    active = false,
                                    onClick = { ShareUtils.shareLink(context, item.title, item.link) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                // Report Action
                                ActionIcon(
                                    icon = Icons.Outlined.Flag,
                                    description = "Report",
                                    active = false,
                                    onClick = { showReportSheet = true }
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
                                        line-height: 1.6; margin: 0; padding: 0; width: 100vw; word-wrap: break-word;
                                    }
                                    p, span, div { font-size: 18px !important; line-height: 1.6 !important; width: auto !important; max-width: 100% !important; display: block; }
                                    img, video, iframe, embed, object { max-width: 100% !important; height: auto !important; border-radius: 8px; margin: 16px 0; display: block; }
                                    a { color: ${String.format("#%06X", 0xFFFFFF and primaryColor)}; text-decoration: none; }
                                    b, strong { font-weight: bold; }
                                    * { box-sizing: border-box; }
                                </style>
                                </head>
                                <body>${item.fullDescription ?: ""}</body>
                                </html>
                            """.trimIndent()

                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = WebViewClient()
                                        setBackgroundColor(0)
                                        isVerticalScrollBarEnabled = false
                                        isHorizontalScrollBarEnabled = false
                                        settings.apply {
                                            javaScriptEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                                            textZoom = 100 
                                        }
                                    }
                                },
                                update = { webView -> webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null) },
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(24.dp))

                            // --- Public Comments Feed ---
                            if (publicComments.isNotEmpty()) {
                                Text(
                                    text = "Reader Feedback", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                publicComments.forEach { comment ->
                                    PublicCommentItem(comment)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // --- Post a Comment Section ---
                            Text(text = "Post a Comment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Share your thoughts...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                enabled = !isPostingComment
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (commentText.isNotBlank() && userId != null && appKey != null && item.hash != null) {
                                        scope.launch {
                                            isPostingComment = true
                                            try {
                                                val cleanAppKey = appKey!!.trim()
                                                val response = RetrofitInstance.api.addComment(
                                                    cleanAppKey, userId!!, "news", item.hash!!, commentText.trim(),
                                                    cleanAppKey, userId!!, "news", item.hash!!, commentText.trim()
                                                )
                                                if (response.isSuccessful && response.body()?.responseDetails?.firstOrNull()?.success == true) {
                                                    commentText = ""
                                                    android.widget.Toast.makeText(context, "Comment posted successfully", android.widget.Toast.LENGTH_SHORT).show()
                                                    // Refresh comments list instantly
                                                    fetchPublicComments()
                                                }
                                            } catch (e: Exception) { } finally { isPostingComment = false }
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                enabled = commentText.isNotBlank() && !isPostingComment
                            ) {
                                if (isPostingComment) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text("Post Comment")
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
fun PublicCommentItem(comment: CommentDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.name?.take(1)?.uppercase() ?: "R",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = comment.name ?: "Reader", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(text = comment.readAt?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment.comment ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ReportContentForm(contentTitle: String, onSubmit: (String) -> Unit) {
    var details by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
        Text("Report Content", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Subject: Reported Content", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text("Content: $contentTitle", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Why are you reporting this?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSubmit(details) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = details.isNotBlank()
        ) {
            Text("Submit Report", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, active: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick, 
        shape = CircleShape,
        color = Color.Transparent, // Removed background color
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon, 
                contentDescription = description, 
                modifier = Modifier.size(24.dp), // Slightly larger icon to maintain balance
                tint = if (active) MaterialTheme.colorScheme.primary else Color.Gray // Dynamic active color
            )
        }
    }
}
