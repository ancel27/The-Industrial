package kivaa.app.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import kivaa.app.SupportChatActivity
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.TicketDetail
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var tickets by remember { mutableStateOf<List<TicketDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val fetchTickets = suspend {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val response = RetrofitInstance.api.getTickets(appKey!!.trim(), userId)
                if (response.isSuccessful) {
                    tickets = response.body()?.responseDetails ?: emptyList()
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchTickets()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Center", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Ticket")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (tickets.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No active tickets", color = Color.Gray)
                        Button(onClick = { showCreateDialog = true }, modifier = Modifier.padding(16.dp)) {
                            Text("Raise a Ticket")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        items(tickets) { ticket ->
                            TicketItem(ticket = ticket, onClick = { 
                                val intent = Intent(context, SupportChatActivity::class.java).apply {
                                    putExtra("TICKET_TOKEN", ticket.token)
                                    putExtra("TICKET_SUBJECT", ticket.subject)
                                    putExtra("TICKET_STATUS", ticket.status)
                                    putExtra("TICKET_CATEGORY", ticket.category)
                                }
                                context.startActivity(intent)
                            })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { dept, subj, msg ->
                scope.launch {
                    val cleanKey = appKey!!.trim()
                    val safeMsg = msg.replace("\n", " ") // Header safety
                    val response = RetrofitInstance.api.createTicket(
                        cleanKey, userId!!, dept, subj, safeMsg,
                        cleanKey, userId!!, dept, subj, safeMsg
                    )
                    if (response.isSuccessful) {
                        showCreateDialog = false
                        fetchTickets()
                    }
                }
            }
        )
    }
}

@Composable
fun TicketItem(ticket: TicketDetail, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ticket.subject ?: "No Subject", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "ID: ${ticket.token}", fontSize = 12.sp, color = Color.Gray)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Badge(containerColor = if (ticket.status == "open") Color(0xFFE8F5E9) else Color(0xFFEEEEEE)) {
                Text(
                    text = ticket.status?.uppercase() ?: "UNKNOWN",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = if (ticket.status == "open") Color(0xFF2E7D32) else Color.Gray
                )
            }
        }
    }
}

// Removed duplicate ChatBubble

@Composable
fun CreateTicketDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("technical") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Support Ticket") },
        text = {
            Column {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Department", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("technical", "account", "content").forEach { dept ->
                        FilterChip(
                            selected = department == dept,
                            onClick = { department = dept },
                            label = { Text(dept, fontSize = 10.sp) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(department, subject, message) },
                enabled = subject.isNotBlank() && message.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
