package theindustrial.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import theindustrial.app.data.model.AddressDetail
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var addresses by remember { mutableStateOf<List<AddressDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val ptrState = rememberPullToRefreshState()

    BackHandler { onBack() }

    val fetchAddresses = suspend {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                val cleanKey = appKey!!.trim()
                val response = RetrofitInstance.api.getAddresses(cleanKey, userId, cleanKey, userId)
                if (response.isSuccessful) {
                    addresses = response.body()?.addressDetails ?: emptyList()
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(appKey, userId) {
        isLoading = true
        fetchAddresses()
        isLoading = false
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            state = ptrState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    fetchAddresses()
                    isRefreshing = false
                }
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = ptrState,
                    isRefreshing = isRefreshing,
                    containerColor = Color.White,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) {
            if (isLoading && addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No addresses added.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "My Addresses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 0.dp)
                        )
                    }
                    items(addresses) { address ->
                        AddressCard(address)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onAddressAdded = {
                scope.launch {
                    fetchAddresses()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddressCard(address: AddressDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), // Subtle grey
        border = if (address.isDefault == 1) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (address.type?.lowercase() == "home") Icons.Outlined.Home else Icons.Outlined.WorkOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = address.name ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (address.isDefault == 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "DEFAULT", 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = address.line1 ?: "", color = Color.DarkGray, fontSize = 14.sp)
                Text(text = "${address.city}, ${address.state} - ${address.pincode}", color = Color.DarkGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Phone: ${address.phone}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(onDismiss: () -> Unit, onAddressAdded: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("home") } // home/office

    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = line1, onValueChange = { line1 = it }, label = { Text("Address Line 1") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = line2, onValueChange = { line2 = it }, label = { Text("Address Line 2") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "home", onClick = { type = "home" })
                    Text("Home", modifier = Modifier.clickable { type = "home" })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = type == "office", onClick = { type = "office" })
                    Text("Office", modifier = Modifier.clickable { type = "office" })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && line1.isNotBlank() && pincode.isNotBlank() && !appKey.isNullOrBlank() && userId != null) {
                        scope.launch {
                            isSubmitting = true
                            try {
                                val k = appKey!!.trim()
                                val response = RetrofitInstance.api.createAddress(
                                    k, userId, name, phone, pincode, line1, line2, city, landmark, state, type,
                                    k, userId, name, phone, pincode, line1, line2, city, landmark, state, type
                                )
                                if (response.isSuccessful) {
                                    onAddressAdded()
                                }
                            } catch (e: Exception) { } finally { isSubmitting = false }
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
