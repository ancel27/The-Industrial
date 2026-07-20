package kivaa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.NewsItem
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.screens.NewsCard
import kivaa.app.ui.screens.NewsDetailScreen
import kivaa.app.ui.theme.DynamicLogo
import kivaa.app.ui.theme.TheIndustrialTheme
import kivaa.app.ui.theme.ThemeManager

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            TheIndustrialTheme {
                SearchScreenContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId by ThemeManager.userId
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var selectedNewsHash by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val performSearch = {
        if (searchQuery.isNotBlank() && !appKey.isNullOrBlank()) {
            scope.launch {
                isLoading = true
                hasSearched = true
                focusManager.clearFocus()
                try {
                    val cleanKey = appKey!!.trim()
                    val response = RetrofitInstance.api.searchContent(
                        appKey = cleanKey, query = searchQuery.trim(),
                        appKeyQ = cleanKey, queryQ = searchQuery.trim()
                    )
                    if (response.isSuccessful) {
                        searchResults = response.body()?.responseDetails ?: emptyList()
                    }
                } catch (e: Exception) { } finally {
                    isLoading = false
                }
            }
        }
    }

    if (selectedNewsHash != null) {
        NewsDetailScreen(newsHash = selectedNewsHash!!, onBack = { selectedNewsHash = null })
    } else {
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
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).height(52.dp),
                            placeholder = { Text("Publishers, categories or topics", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(26.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF2F2F2),
                                unfocusedContainerColor = Color(0xFFF2F2F2),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp, 
                                lineHeight = 18.sp 
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { performSearch() })
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color.White)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (hasSearched && searchResults.isEmpty()) {
                    Text("No results found.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                } else if (!hasSearched) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Search industrial insights", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { item ->
                            NewsCard(
                                item = item,
                                onClick = { item.hash?.let { selectedNewsHash = it } }
                            )
                        }
                    }
                }
            }
        }
    }
}
