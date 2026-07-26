package kivaa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.EntitlementDetail
import kivaa.app.data.model.PlatformConfig
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntitlementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var entitlements by remember { mutableStateOf<List<EntitlementDetail>>(emptyList()) }
    var platforms by remember { mutableStateOf<List<PlatformConfig>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedEntitlement by remember { mutableStateOf<EntitlementDetail?>(null) }

    val fetchEntitlements = suspend {
        if (!appKey.isNullOrBlank() && userId != null) {
            isLoading = true
            try {
                val cleanKey = appKey!!.trim()
                val res = RetrofitInstance.api.getEntitlements(cleanKey, userId)
                if (res.isSuccessful) {
                    entitlements = res.body()?.responseDetails?.filter { it.status == "unused" } ?: emptyList()
                }
                
                // Fetch Platforms for redemption
                val pRes = RetrofitInstance.api.getActivePlatforms(cleanKey)
                if (pRes.isSuccessful) {
                    platforms = pRes.body()?.responseDetails ?: emptyList()
                }
            } catch (e: Exception) { } finally { isLoading = false }
        }
    }

    LaunchedEffect(appKey, userId) {
        fetchEntitlements()
    }

    BackHandler { onBack() }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (isLoading && entitlements.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (entitlements.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No unused benefits found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Available to Claim",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(entitlements) { item ->
                        EntitlementCard(item) { selectedEntitlement = item }
                    }
                }
            }
        }
    }

    if (selectedEntitlement != null) {
        RedeemDialog(
            entitlement = selectedEntitlement!!,
            availablePlatforms = platforms,
            onDismiss = { selectedEntitlement = null },
            onSuccess = {
                scope.launch { fetchEntitlements() }
                selectedEntitlement = null
            }
        )
    }
}

@Composable
fun EntitlementCard(item: EntitlementDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.planName ?: "Benefit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Validity: ${item.duration} days", fontSize = 12.sp, color = Color.Gray)
                Text("Order: ${item.orderNo}", fontSize = 11.sp, color = Color.LightGray)
            }
            Button(onClick = onClick, shape = RoundedCornerShape(8.dp)) {
                Text("Claim", fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemDialog(entitlement: EntitlementDetail, availablePlatforms: List<PlatformConfig>, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: Platform & Target, 2: OTP (if gift)
    var targetType by remember { mutableStateOf("self") } // self, gift
    var selectedPlatformId by remember { mutableStateOf<Int?>(null) }
    
    // Gift Details
    var recipientEmail by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var otpValue by remember { mutableStateOf("") }
    var isOperating by remember { mutableStateOf(false) }

    LaunchedEffect(availablePlatforms) {
        if (selectedPlatformId == null && availablePlatforms.isNotEmpty()) {
            selectedPlatformId = availablePlatforms.find { it.isCurrentPlatform == 1 }?.platformId ?: availablePlatforms.firstOrNull()?.platformId
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isOperating) onDismiss() },
        title = { Text(if (step == 1) "Claim Benefit" else "Verify Gifting", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (step == 1) {
                    Text("Select where to activate this benefit:", fontSize = 14.sp)
                    
                    var expanded by remember { mutableStateOf(false) }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(52.dp).clickable { expanded = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = availablePlatforms.find { it.platformId == selectedPlatformId }?.platformName ?: "Select Platform", 
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availablePlatforms.forEach { p ->
                                DropdownMenuItem(text = { Text(p.platformName ?: "") }, onClick = { selectedPlatformId = p.platformId; expanded = false })
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = targetType == "self", onClick = { targetType = "self" })
                        Text("For Myself", modifier = Modifier.clickable { targetType = "self" })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = targetType == "gift", onClick = { targetType = "gift" })
                        Text("Gift to Others", modifier = Modifier.clickable { targetType = "gift" })
                    }

                    if (targetType == "gift") {
                        OutlinedTextField(value = recipientName, onValueChange = { recipientName = it }, label = { Text("Recipient Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = recipientEmail, onValueChange = { recipientEmail = it }, label = { Text("Recipient Email") }, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Text("Enter the OTP sent to your registered email to confirm this gift.", fontSize = 14.sp)
                    OutlinedTextField(
                        value = otpValue, onValueChange = { if (it.length <= 6) otpValue = it },
                        label = { Text("6-Digit OTP") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (appKey.isNullOrBlank() || userId == null || selectedPlatformId == null) return@launch
                        val k = appKey!!.trim()
                        isOperating = true
                        
                        try {
                            if (targetType == "self") {
                                val res = RetrofitInstance.api.redeemEntitlementSelf(k, userId, entitlement.id!!, selectedPlatformId!!)
                                if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.success == true) {
                                    Toast.makeText(context, "Benefit activated successfully!", Toast.LENGTH_LONG).show()
                                    onSuccess()
                                }
                            } else {
                                if (step == 1) {
                                    val res = RetrofitInstance.api.sendGiftOtp(k, userId, entitlement.id!!)
                                    if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.sent == true) {
                                        step = 2
                                        Toast.makeText(context, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val res = RetrofitInstance.api.createRecipientAndGift(
                                        k, userId, entitlement.id!!, recipientName, recipientEmail, null, selectedPlatformId!!, otpValue
                                    )
                                    if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.success == true) {
                                        Toast.makeText(context, "Benefit gifted successfully!", Toast.LENGTH_LONG).show()
                                        onSuccess()
                                    } else {
                                        Toast.makeText(context, "Invalid OTP or failed to gift", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
                        } finally { isOperating = false }
                    }
                },
                enabled = !isOperating && selectedPlatformId != null && (if (targetType == "gift") recipientEmail.isNotBlank() && (if (step == 2) otpValue.length == 6 else true) else true)
            ) {
                if (isOperating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text(if (targetType == "self") "Claim Now" else if (step == 1) "Send OTP" else "Confirm Gift")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isOperating) { Text("Cancel") }
        }
    )
}
