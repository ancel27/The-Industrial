package theindustrial.app.ui.screens

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
import androidx.compose.material.icons.filled.Info
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
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.MagazineItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

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
                        // Fetch full detail for the first magazine to get intro
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
        if (isLoading && !isRefreshing && magazines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null && magazines.isEmpty()) {
            Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
        } else if (magazines.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No magazines available.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Featured Magazine Header Card
                selectedMagazine?.let { mag ->
                    item(span = { GridItemSpan(3) }) {
                        MagazineDetailCard(mag)
                    }
                }

                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "All Editions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(magazines) { magazine ->
                    MagazineThumbnail(
                        magazine = magazine,
                        isSelected = selectedMagazine?.id == magazine.id,
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

@Composable
fun MagazineDetailCard(magazine: MagazineItem) {
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        val cdn = config?.cdnUrl ?: ""
        if (cdn.endsWith("/")) "${cdn}content/placeholder.jpg" else "${cdn}/content/placeholder.jpg"
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
                // Magazine Cover
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

                // Details
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
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailLabelValue("Published by", magazine.publishedBy ?: "Kivaa Digital LLP")
                    DetailLabelValue("Language", magazine.language ?: "English")
                    DetailLabelValue("Frequency", magazine.frequency ?: "Monthly")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Outlined.ThumbUp, contentDescription = "Like", modifier = Modifier.size(22.dp))
                        Text("|", color = Color.LightGray)
                        Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(22.dp))
                    }
                }
            }
            
            // --- Expandable Description Section ---
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
                    onClick = { /* TODO: Open Reader */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Read Now", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { /* TODO: Subscribe */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Read Now", fontWeight = FontWeight.Bold)
                }
            }
        }
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
        val cdn = config?.cdnUrl ?: ""
        if (cdn.endsWith("/")) "${cdn}content/placeholder.jpg" else "${cdn}/content/placeholder.jpg"
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
                // Layer 1: Branded Placeholder (Always at the base)
                AsyncImage(
                    model = placeholderUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.6f
                )
                
                // Layer 2: Actual Magazine Cover (Loads on top)
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
