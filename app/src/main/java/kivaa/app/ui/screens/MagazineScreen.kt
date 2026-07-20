package kivaa.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.MagazineItem
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager
import kivaa.app.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineScreen() {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var magazines by remember { mutableStateOf<List<MagazineItem>>(emptyList()) }
    var selectedMagazine by remember { mutableStateOf<MagazineItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ptrState = rememberPullToRefreshState()

    val fetchDetail = suspend { magHash: String ->
        if (!appKey.isNullOrBlank()) {
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getMagazineDetail(cleanKey, magHash, cleanKey, magHash)
                if (response.isSuccessful) {
                    val detail = response.body()?.magazines?.firstOrNull()
                    if (detail != null) {
                        selectedMagazine = detail
                    }
                }
            } catch (e: Exception) { }
        }
    }

    val fetchData = suspend {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getMagazines(cleanKey, userId, cleanKey, userId)
                if (response.isSuccessful) {
                    val list = response.body()?.magazines ?: emptyList()
                    magazines = list
                    if (selectedMagazine == null && list.isNotEmpty()) {
                        list.first().hash?.let { fetchDetail(it) }
                    }
                    errorMessage = null
                }
            } catch (e: Exception) {
                if (magazines.isEmpty()) errorMessage = "Network error"
            }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchData()
        isLoading = false
    }

    PullToRefreshBox(
        state = ptrState,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                fetchData()
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
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Fixed Header Area: Selected Magazine
            selectedMagazine?.let { mag ->
                Box(modifier = Modifier.padding(16.dp)) {
                    MagazineDetailCard(mag)
                }
            }

            // 2. Scrollable Body: All Editions Grid
            if (isLoading && !isRefreshing && magazines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null && magazines.isEmpty()) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp), color = Color.Red)
            } else if (magazines.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No magazines available.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            text = "All Editions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(magazines) { magazine ->
                        MagazineThumbnail(
                            magazine = magazine,
                            isSelected = selectedMagazine?.hash == magazine.hash,
                            onClick = { 
                                scope.launch {
                                    magazine.hash?.let { fetchDetail(it) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagazineDetailCard(magazine: MagazineItem) {
    val context = LocalContext.current
    val config = ThemeManager.currentConfig.value
    val preferenceManager = remember { PreferenceManager(context) }
    val savedAppKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()
    
    var isLiked by remember(magazine.hash) { mutableStateOf(false) }
    var isBookmarked by remember(magazine.hash) { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(magazine.hash, userId, savedAppKey) {
        if (userId != null && !savedAppKey.isNullOrBlank() && magazine.hash != null) {
            try {
                val cleanKey = savedAppKey!!.trim()
                val likesRes = RetrofitInstance.api.viewLikes(cleanKey, userId)
                if (likesRes.isSuccessful) {
                    isLiked = likesRes.body()?.responseDetails?.any { it.hash == magazine.hash } ?: false
                }
                val bookmarksRes = RetrofitInstance.api.viewBookmarks(cleanKey, userId)
                if (bookmarksRes.isSuccessful) {
                    isBookmarked = bookmarksRes.body()?.responseDetails?.any { it.hash == magazine.hash } ?: false
                }
            } catch (e: Exception) {}
        }
    }

    val placeholderUrl = remember(config) {
        config?.imageUrl1 ?: config?.imageUrl2 ?: ""
    }
    
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = magazine.image,
                    placeholder = rememberAsyncImagePainter(model = placeholderUrl),
                    error = rememberAsyncImagePainter(model = placeholderUrl),
                    fallback = rememberAsyncImagePainter(model = placeholderUrl),
                    contentDescription = magazine.title,
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = magazine.date ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        IconButton(
                            onClick = { ShareUtils.shareLink(context, magazine.title ?: "Magazine Edition", magazine.magazineUrl ?: "") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailLabelValue("Published by", magazine.publishedBy ?: "Kivaa Digital LLP")
                    DetailLabelValue("Language", magazine.language ?: "English")
                    DetailLabelValue("Frequency", magazine.frequency ?: "Monthly")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (userId != null && !savedAppKey.isNullOrBlank() && magazine.hash != null) {
                                    scope.launch {
                                        val key = savedAppKey!!.trim()
                                        val res = if (isLiked) {
                                            RetrofitInstance.api.unlike(key, userId, "magazine", magazine.hash, key, userId, "magazine", magazine.hash)
                                        } else {
                                            RetrofitInstance.api.like(key, userId, "magazine", magazine.hash, key, userId, "magazine", magazine.hash)
                                        }
                                        if (res.isSuccessful) isLiked = !isLiked
                                    }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUpOffAlt,
                                contentDescription = "Like",
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text("|", color = Color.LightGray)

                        IconButton(
                            onClick = {
                                if (userId != null && !savedAppKey.isNullOrBlank() && magazine.hash != null) {
                                    scope.launch {
                                        val key = savedAppKey!!.trim()
                                        val res = if (isBookmarked) {
                                            RetrofitInstance.api.unbookmark(key, userId, "magazine", magazine.hash, key, userId, "magazine", magazine.hash)
                                        } else {
                                            RetrofitInstance.api.bookmark(key, userId, "magazine", magazine.hash, key, userId, "magazine", magazine.hash)
                                        }
                                        if (res.isSuccessful) isBookmarked = !isBookmarked
                                    }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text("|", color = Color.LightGray)

                        IconButton(
                            onClick = { showReviewDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Review",
                                tint = Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            
            if (!magazine.intro.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = arrowRotation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = magazine.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (magazine.hasAccess == "1") {
                Button(
                    onClick = {
                        val apiUrl = magazine.magazineUrl ?: ""
                        if (apiUrl.isNotBlank()) {
                            // The API URL already contains the tag, so we just clean it up
                            var finalUrl = apiUrl
                            
                            // 1. Enforce trailing slash
                            if (!finalUrl.endsWith("/")) finalUrl += "/"
                            
                            // 2. Branded Domain Swap (Ensures it matches your verified Chrome link)
                            finalUrl = finalUrl.replace("cdntie9m3y9sg7b.cdn.e2enetworks.net", "cdnti.kivaa.io.in")
                            
                            // Debug Toast for confirmation
                            Toast.makeText(context, "Opening: $finalUrl", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, kivaa.app.MagazineReaderActivity::class.java).apply {
                                putExtra("MAGAZINE_URL", finalUrl)
                                putExtra("MAGAZINE_TITLE", magazine.date ?: "Magazine Reader")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Read Now", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val apiUrl = magazine.magazineUrl ?: ""
                        if (apiUrl.isNotBlank()) {
                            var finalUrl = apiUrl
                            if (!finalUrl.endsWith("/")) finalUrl += "/"
                            finalUrl = finalUrl.replace("cdntie9m3y9sg7b.cdn.e2enetworks.net", "cdnti.kivaa.io.in")
                            
                            Toast.makeText(context, "Opening: $finalUrl", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, kivaa.app.MagazineReaderActivity::class.java).apply {
                                putExtra("MAGAZINE_URL", finalUrl)
                                putExtra("MAGAZINE_TITLE", magazine.date ?: "Magazine Reader")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Read Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showReviewDialog) {
        var reviewText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showReviewDialog = false },
            title = { Text("Write a Review", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Share your thoughts on this edition...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isSubmitting
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewText.isNotBlank() && userId != null && !savedAppKey.isNullOrBlank() && magazine.hash != null) {
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val key = savedAppKey!!.trim()
                                    val response = RetrofitInstance.api.addReview(
                                        key, userId, "magazine", magazine.hash, reviewText.trim(),
                                        key, userId, "magazine", magazine.hash, reviewText.trim()
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                                        showReviewDialog = false
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to submit review", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    enabled = reviewText.isNotBlank() && !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }, enabled = !isSubmitting) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray)
    }
}

@Composable
fun MagazineThumbnail(magazine: MagazineItem, isSelected: Boolean, onClick: () -> Unit) {
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        config?.imageUrl1 ?: config?.imageUrl2 ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = placeholderUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.6f
                )
                
                AsyncImage(
                    model = magazine.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = magazine.date ?: "",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
