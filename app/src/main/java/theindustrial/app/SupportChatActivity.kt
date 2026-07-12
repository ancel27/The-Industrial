package theindustrial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import theindustrial.app.data.model.MessageDetail
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.screens.ChatBubble
import theindustrial.app.ui.theme.DynamicLogo
import theindustrial.app.ui.theme.TheIndustrialTheme
import theindustrial.app.ui.theme.ThemeManager

class SupportChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ticketToken = intent.getStringExtra("TICKET_TOKEN") ?: ""
        val ticketSubject = intent.getStringExtra("TICKET_SUBJECT") ?: "Support Chat"
        val ticketStatus = intent.getStringExtra("TICKET_STATUS") ?: "open"
        val ticketCategory = intent.getStringExtra("TICKET_CATEGORY") ?: "general"

        setContent {
            TheIndustrialTheme {
                SupportChatContent(
                    token = ticketToken,
                    subject = ticketSubject,
                    status = ticketStatus,
                    category = ticketCategory,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatContent(
    token: String,
    subject: String,
    status: String,
    category: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var currentStatus by remember { mutableStateOf(status) }
    var messages by remember { mutableStateOf<List<MessageDetail>>(emptyList()) }
    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isOperating by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    BackHandler { onBack() }

    val fetchConversation = suspend {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getTicketConversation(
                    cleanKey, userId, token,
                    cleanKey, userId, token
                )
                if (response.isSuccessful) {
                    messages = response.body()?.responseDetails ?: emptyList()
                    // Scroll to bottom after loading messages
                    if (messages.isNotEmpty()) {
                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchConversation()
        isLoading = false
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
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        Text("ID: $token", fontSize = 10.sp, color = Color.Gray)
                    }

                    if (currentStatus == "open") {
                        TextButton(
                            onClick = {
                                if (!appKey.isNullOrBlank() && userId != null) {
                                    scope.launch {
                                        isOperating = true
                                        val cleanKey = appKey!!.trim()
                                        val response = RetrofitInstance.api.closeTicket(
                                            cleanKey, userId, token,
                                            cleanKey, userId, token
                                        )
                                        if (response.isSuccessful) {
                                            currentStatus = "closed"
                                        }
                                        isOperating = false
                                    }
                                }
                            },
                            enabled = !isOperating
                        ) {
                            Text("Close", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
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
        ) {
            // Chat Body
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading && messages.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No messages found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            ChatBubble(
                                message = msg.message ?: "",
                                isUser = msg.senderType == "user"
                            )
                        }
                        
                        if (currentStatus == "closed") {
                            item {
                                Text(
                                    "This ticket is closed.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Input Area
            if (currentStatus == "open") {
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Reply to support...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isOperating
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                if (replyText.isNotBlank() && !appKey.isNullOrBlank() && userId != null) {
                                    scope.launch {
                                        isOperating = true
                                        val cleanKey = appKey!!.trim()
                                        val msg = replyText.trim().replace("\n", " ")
                                        val response = RetrofitInstance.api.replyToTicket(
                                            cleanKey, userId, token, msg,
                                            cleanKey, userId, token, msg
                                        )
                                        if (response.isSuccessful) {
                                            replyText = ""
                                            fetchConversation() // Refresh list
                                        }
                                        isOperating = false
                                    }
                                }
                            },
                            enabled = replyText.isNotBlank() && !isOperating
                        ) {
                            if (isOperating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
