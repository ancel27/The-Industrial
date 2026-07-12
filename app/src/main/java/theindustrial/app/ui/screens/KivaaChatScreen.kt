package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.DynamicLogo

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KivaaChatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var messages by remember { mutableStateOf(listOf(ChatMessage("Hello! I'm Kivaa, your industrial AI assistant. How can I help you today?", false))) }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    BackHandler { onBack() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
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
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ask Kivaa", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        Text("Industrial AI Assistant", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .navigationBarsPadding() // Ensures it sits flush against bottom
        ) {
            // Chat History
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(message = msg.text, isUser = msg.isUser)
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }

            // Input Area
            Surface(
                color = Color.White, // Enforced White
                shadowElevation = 0.dp, // Removed shadow
                tonalElevation = 0.dp   // Removed elevation tint
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask Kivaa...", fontSize = 14.sp) },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isTyping,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Dynamic Border
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), // Subtle Dynamic Border
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp, 
                                color = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isTyping) {
                                if (inputText.isNotBlank() && !appKey.isNullOrBlank()) {
                                    val userPrompt = inputText.trim()
                                    messages = messages + ChatMessage(userPrompt, true)
                                    inputText = ""
                                    isTyping = true
                                    
                                    scope.launch {
                                        try {
                                            val cleanKey = appKey!!.trim()
                                            val response = RetrofitInstance.api.askKivaa(
                                                cleanKey, userPrompt, cleanKey, userPrompt
                                            )
                                            if (response.isSuccessful) {
                                                val aiResponse = response.body()?.responseDetails?.firstOrNull()?.aiText
                                                    ?: "I'm sorry, I couldn't generate a response right now."
                                                messages = messages + ChatMessage(aiResponse, false)
                                            } else {
                                                messages = messages + ChatMessage("Technical error occurred. Please try again.", false)
                                            }
                                        } catch (e: Exception) {
                                            messages = messages + ChatMessage("Network error. Please check your connection.", false)
                                        } finally {
                                            isTyping = false
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTyping) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFF2F2F2),
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isUser) 16.dp else 2.dp, 
                bottomEnd = if (isUser) 2.dp else 16.dp
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
            modifier = Modifier.padding(end = 40.dp)
        ) {
            Text(
                "Kivaa is thinking...",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 12.sp,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
