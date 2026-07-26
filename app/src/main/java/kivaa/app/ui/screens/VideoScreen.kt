package kivaa.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kivaa.app.R
import kivaa.app.ui.theme.ThemeManager

data class VideoItem(
    val id: Int,
    val title: String,
    val source: String,
    val views: String,
    val date: String,
    val duration: String,
    val thumbnail: String,
    val description: String = ""
)

@Composable
fun VideoScreen() {
    var selectedVideo by remember { mutableStateOf<VideoItem?>(null) }
    
    val sampleVideos = listOf(
        VideoItem(1, "Xinhua Silk Road: Feeling warmth of Silk Road, discovering vibrant everyday life of...", "Xinhua Silk Road", "0 views", "3 Jul 2026", "00:00", "https://d28f47c49dt9uw.cloudfront.net/content/1780338802-GameChange_Energy.webp"),
        VideoItem(2, "International Naval Review on deck.", "New York Stock Exchange", "0 views", "2 Jul 2026", "00:00", "https://d28f47c49dt9uw.cloudfront.net/content/1780338800-DXC_Technology_Company-DXC_Launches_One_of_Its_Most_Powerful_Gro.mp4?id=OA2643880"),
        VideoItem(3, "Evernorth Launches AI-Powered 'Pharmacy Forward'", "New York Stock Exchange", "0 views", "1 Jul 2026", "00:00", "https://d28f47c49dt9uw.cloudfront.net/content/1780338801-5486596"),
        VideoItem(4, "Zerohash Onchain Brokerage Summit unfolds at NYSE", "New York Stock Exchange", "0 views", "30 Jun 2026", "00:00", "https://d28f47c49dt9uw.cloudfront.net/content/1780338798-5996642"),
        VideoItem(5, "Orlando Mayor Talks City&#8217;s Business Transformation.", "New York Stock Exchange", "1.2k views", "29 Jun 2026", "05:12", "https://d28f47c49dt9uw.cloudfront.net/content/1780338802-GameChange_Energy.webp"),
        VideoItem(6, "How can a company navigate business cycles and achieve lasting success?", "South", "800 views", "28 Jun 2026", "12:45", "https://d28f47c49dt9uw.cloudfront.net/content/1780338800-DXC_Technology_Company-DXC_Launches_One_of_Its_Most_Powerful_Gro.mp4?id=OA2643880")
    )

    if (selectedVideo != null) {
        VideoDetailView(
            video = selectedVideo!!, 
            relatedVideos = sampleVideos.filter { it.id != selectedVideo!!.id },
            onBack = { selectedVideo = null }
        )
    } else {
        VideoListView(
            videos = sampleVideos,
            onVideoClick = { selectedVideo = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListView(videos: List<VideoItem>, onVideoClick: (VideoItem) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) { // Complete black background
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    // Audivue Logo
                    AsyncImage(
                        model = "https://www.audivue.in/assets/Logo.png",
                        contentDescription = "Audivue Logo",
                        modifier = Modifier.height(40.dp).width(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Videos worth\nwatching.",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 52.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "User-first media platform for videos, audio and radio. Login to like, subscribe and bookmark. Sharing stays open for everyone.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Search Bar (Transparent with White Border)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Search videos", color = Color.Gray, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            
                            // Branded Gradient Search Button
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFF7E33), Color(0xFFFF4500))
                                        )
                                    )
                                    .clickable { /* Search Action */ }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Search", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Videos", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${videos.size} items", color = Color.Gray, fontSize = 12.sp)
                }
            }

            items(videos) { video ->
                VideoCard(video = video, onClick = { onVideoClick(video) })
            }
            
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun VideoCard(video: VideoItem, onClick: () -> Unit) {
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        config?.imageUrl1 ?: config?.imageUrl2 ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = video.thumbnail,
                error = rememberAsyncImagePainter(model = placeholderUrl),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Play Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                }
            }
            
            // Duration
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(video.duration, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = video.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(video.source, color = Color.Gray, fontSize = 12.sp)
        Text("${video.views} • ${video.date}", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun VideoDetailView(video: VideoItem, relatedVideos: List<VideoItem>, onBack: () -> Unit) {
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        config?.imageUrl1 ?: config?.imageUrl2 ?: ""
    }

    BackHandler { onBack() }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) { // Complete black
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    error = rememberAsyncImagePainter(model = placeholderUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
                
                // Back Button Overlay
                IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(video.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${video.source} • ${video.views} • ${video.date}", color = Color.Gray, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoActionChip(Icons.Outlined.ThumbUp, "Like")
                    VideoActionChip(Icons.Outlined.Bookmark, "Bookmark")
                    VideoActionChip(Icons.Outlined.AddCircleOutline, "Subscribe")
                    VideoActionChip(Icons.Outlined.Share, "Share")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("More to watch", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                relatedVideos.forEach { item ->
                    RelatedVideoItem(video = item)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun VideoActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent, // Transparent background
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)), // White border
        modifier = Modifier.height(30.dp) // Smaller height
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)) // Smaller icon
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium) // Smaller text
        }
    }
}

@Composable
fun RelatedVideoItem(video: VideoItem) {
    val config = ThemeManager.currentConfig.value
    val placeholderUrl = remember(config) {
        config?.imageUrl1 ?: config?.imageUrl2 ?: ""
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.width(120.dp).aspectRatio(1.77f).clip(RoundedCornerShape(8.dp))) {
            AsyncImage(
                model = video.thumbnail, 
                error = rememberAsyncImagePainter(model = placeholderUrl),
                contentDescription = null, 
                modifier = Modifier.fillMaxSize(), 
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(video.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(video.source, color = Color.Gray, fontSize = 11.sp)
        }
    }
}
