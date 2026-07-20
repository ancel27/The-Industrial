package kivaa.app.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.*
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager
import java.util.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var plans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }
    var offers by remember { mutableStateOf<List<OfferDetail>>(emptyList()) }
    var paymentMethods by remember { mutableStateOf<List<PaymentMethodDetail>>(emptyList()) }
    var userAddresses by remember { mutableStateOf<List<AddressDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // UI State
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var selectedOffer by remember { mutableStateOf<OfferDetail?>(null) }
    var selectedAddress by remember { mutableStateOf<AddressDetail?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethodDetail?>(null) }
    
    var currentView by remember { mutableStateOf("selection") } // selection, summary, manual_payment, verification
    var verificationOrderId by remember { mutableStateOf<String?>(null) }
    
    // Captured manual payment details for ticket reference
    var manualRefNo by remember { mutableStateOf("") }
    var manualPaidAt by remember { mutableStateOf("") }
    var manualNotes by remember { mutableStateOf("") }
    var finalAmountCaptured by remember { mutableDoubleStateOf(0.0) }
    var isSubmittingManual by remember { mutableStateOf(false) }

    // Razorpay Launcher
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val success = result.data?.getBooleanExtra("SUCCESS", false) ?: false
            if (success) {
                // Capture Razorpay Details for ticket reference
                manualRefNo = result.data?.getStringExtra("MESSAGE") ?: "Success"
                manualPaidAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
                manualNotes = "Paid via Razorpay Online"
                currentView = "verification"
            } else {
                val message = result.data?.getStringExtra("MESSAGE") ?: ""
                Toast.makeText(context, "Payment Failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler {
        when (currentView) {
            "summary" -> currentView = "selection"
            "manual_payment" -> currentView = "summary"
            "verification" -> currentView = "summary"
            else -> onBack()
        }
    }

    // Initial Fetch
    LaunchedEffect(appKey) {
        if (!appKey.isNullOrBlank()) {
            isLoading = true
            try {
                val res = RetrofitInstance.api.getSubscriptionPlans(appKey!!.trim())
                if (res.isSuccessful) {
                    val fetched = res.body()?.plans ?: emptyList()
                    plans = fetched
                    if (selectedPlan == null && fetched.isNotEmpty()) selectedPlan = fetched.first()
                }
            } catch (e: Exception) { } finally { isLoading = false }
        }
    }

    // Dynamic Summary Data
    LaunchedEffect(currentView, appKey, userId) {
        if (currentView == "summary" && !appKey.isNullOrBlank() && userId != null) {
            scope.launch {
                try {
                    val cleanKey = appKey!!.trim()
                    val oRes = RetrofitInstance.api.getOffers(cleanKey); if (oRes.isSuccessful) offers = oRes.body()?.responseDetails ?: emptyList()
                    val aRes = RetrofitInstance.api.getAddresses(cleanKey, userId, cleanKey, userId); if (aRes.isSuccessful) {
                        val addrs = aRes.body()?.addressDetails ?: emptyList(); userAddresses = addrs
                        if (selectedAddress == null) selectedAddress = addrs.find { it.isDefault == 1 } ?: addrs.firstOrNull()
                    }
                    val pRes = RetrofitInstance.api.getPaymentMethods(cleanKey); if (pRes.isSuccessful) {
                        val pms = pRes.body()?.responseDetails ?: emptyList(); paymentMethods = pms
                        if (selectedPaymentMethod == null && pms.isNotEmpty()) selectedPaymentMethod = pms.first()
                    }
                } catch (e: Exception) { }
            }
        }
    }

    Scaffold(
        topBar = {
            if (currentView != "verification" && currentView != "manual_payment") {
                Surface(color = Color.White, shadowElevation = 0.dp) {
                    Row(modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { 
                            if (currentView == "summary") currentView = "selection" else onBack() 
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                        Text(text = if (currentView == "summary") "Order Summary" else "Subscription Plans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070), modifier = Modifier.weight(1f))
                        IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = Color.Gray) }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(if (currentView == "verification" || currentView == "manual_payment") PaddingValues(0.dp) else padding).background(Color.White)) {
            if (isLoading && plans.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (currentView) {
                    "selection" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                item { selectedPlan?.let { BenefitCard(it) } }
                                item {
                                    Text("Choose your plan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070), modifier = Modifier.padding(bottom = 12.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                                        items(plans) { plan -> SmallPlanSelectorCard(plan = plan, isSelected = selectedPlan?.id == plan.id, onClick = { selectedPlan = plan }) }
                                    }
                                }
                            }
                            Button(onClick = { currentView = "summary" }, modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366))) {
                                Text("Subscribe Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    "summary" -> {
                        selectedPlan?.let { plan ->
                            val base = plan.amount?.toDoubleOrNull() ?: 0.0
                            val gst = plan.gst?.toDoubleOrNull() ?: 0.0
                            val discount = selectedOffer?.let { if (it.type == "fixed") it.discount?.toDoubleOrNull() ?: 0.0 else (base * (it.discount?.toDoubleOrNull() ?: 0.0)) / 100.0 } ?: 0.0
                            val totalPayable = (base + gst - discount).coerceAtLeast(0.0)

                            OrderSummaryContent(
                                plan = plan, offers = offers, userAddresses = userAddresses, selectedAddress = selectedAddress, 
                                onAddressSelected = { selectedAddress = it }, paymentMethods = paymentMethods, 
                                selectedPaymentMethod = selectedPaymentMethod, onPaymentMethodSelected = { selectedPaymentMethod = it }, 
                                selectedOffer = selectedOffer, onOfferSelected = { selectedOffer = it }, 
                                onContinue = {
                                    finalAmountCaptured = totalPayable
                                    if (selectedPaymentMethod?.type == "bank") {
                                        currentView = "manual_payment"
                                    } else if (selectedPaymentMethod?.providerCode == "razorpay" && userId != null && !appKey.isNullOrBlank()) {
                                        scope.launch {
                                            try {
                                                val k = appKey!!.trim()
                                                val addrId = selectedAddress?.id ?: 0
                                                val res = RetrofitInstance.api.createOrder(
                                                    appKey = k, userId = userId, planId = plan.id ?: 0, addressId = addrId,
                                                    offerId = selectedOffer?.id,
                                                    appKeyQ = k, userIdQ = userId, planIdQ = plan.id ?: 0, addressIdQ = addrId,
                                                    offerIdQ = selectedOffer?.id
                                                )
                                                if (res.isSuccessful) {
                                                    val orderData = res.body()?.responseDetails?.firstOrNull()
                                                    verificationOrderId = orderData?.orderNo
                                                    val amount = (plan.amount?.toDoubleOrNull() ?: 0.0) + (plan.gst?.toDoubleOrNull() ?: 0.0)
                                                    val config = ThemeManager.currentConfig.value
                                                    val intent = Intent(context, kivaa.app.PaymentActivity::class.java).apply {
                                                        putExtra("AMOUNT", amount); putExtra("NAME", plan.name); putExtra("DESCRIPTION", "Order: ${orderData?.orderNo}")
                                                        putExtra("EMAIL", ThemeManager.userName.value ?: ""); putExtra("CONTACT", selectedAddress?.phone ?: "")
                                                        putExtra("LOGO_URL", config?.logoUrl ?: ""); putExtra("THEME_COLOR", config?.theme?.primary ?: "#003366")
                                                    }
                                                    paymentLauncher.launch(intent)
                                                }
                                            } catch (e: Exception) { }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    "manual_payment" -> {
                        selectedPlan?.let { plan ->
                            ManualPaymentScreen(
                                plan = plan,
                                method = selectedPaymentMethod!!,
                                payableAmount = finalAmountCaptured,
                                isSubmitting = isSubmittingManual,
                                onSubmit = { ref, date, notes ->
                                    manualRefNo = ref
                                    manualPaidAt = date
                                    manualNotes = notes
                                    if (userId != null && !appKey.isNullOrBlank()) {
                                        scope.launch {
                                            try {
                                                isSubmittingManual = true
                                                val k = appKey!!.trim()
                                                val addrId = selectedAddress?.id ?: 0
                                                val res = RetrofitInstance.api.createOrder(
                                                    appKey = k, userId = userId, planId = plan.id ?: 0, addressId = addrId,
                                                    offerId = selectedOffer?.id,
                                                    providerId = selectedPaymentMethod!!.id, mode = "bank_transfer",
                                                    referenceNo = ref, paidAt = date, notes = notes,
                                                    appKeyQ = k, userIdQ = userId, planIdQ = plan.id ?: 0, addressIdQ = addrId,
                                                    offerIdQ = selectedOffer?.id,
                                                    providerIdQ = selectedPaymentMethod!!.id, modeQ = "bank_transfer",
                                                    referenceNoQ = ref, paidAtQ = date, notesQ = notes
                                                )
                                                if (res.isSuccessful) {
                                                    val data = res.body()?.responseDetails?.firstOrNull()
                                                    verificationOrderId = data?.orderNo ?: data?.orderId?.toString()
                                                    currentView = "verification"
                                                } else {
                                                    Toast.makeText(context, "Failed to create order. Please try again.", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Network error. Check your connection.", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isSubmittingManual = false
                                            }
                                        }
                                    }
                                },
                                onBack = { currentView = "summary" }
                            )
                        }
                    }
                    "verification" -> {
                        VerificationTimerScreen(
                            orderId = verificationOrderId ?: "N/A", 
                            refNo = manualRefNo,
                            paidAt = manualPaidAt,
                            notes = manualNotes,
                            amount = finalAmountCaptured,
                            onSuccess = { onBack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManualPaymentScreen(plan: SubscriptionPlan, method: PaymentMethodDetail, payableAmount: Double, isSubmitting: Boolean, onSubmit: (String, String, String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var refNo by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> paymentDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", y, m + 1, d) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            Column {
                Text("Payment", fontSize = 12.sp, color = Color.Gray)
                Text(method.providerName ?: "Bank Transfer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070))
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(200.dp).background(Color.White).padding(8.dp)) {
                    AsyncImage(model = "https://kivaa.io.in/assets/qr_placeholder.png", contentDescription = "UPI QR", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Open UPI App", color = Color(0xFF004080), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.clickable { })
                Text("Amount and order reference are already included.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                BankDetailItem("ACCOUNT NAME", method.providerInfo?.accountName ?: "Kivaa Digital LLP")
                BankDetailItem("ACCOUNT NUMBER", method.providerInfo?.accountNumber ?: "000000000000")
                BankDetailItem("IFSC", method.providerInfo?.ifsc ?: "UBIN0000000")
                BankDetailItem("ACCOUNT TYPE", method.providerInfo?.accountType ?: "Current Account")
                BankDetailItem("BRANCH", method.providerInfo?.branch ?: "Nasik")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Submit your payment reference for verification.", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))

        ManualInputLabel("Transaction / UTR / Cheque No.")
        OutlinedTextField(value = refNo, onValueChange = { refNo = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
        
        Spacer(modifier = Modifier.height(16.dp))
        ManualInputLabel("Payment Date")
        OutlinedTextField(
            value = paymentDate, onValueChange = { }, readOnly = true, enabled = false, 
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, shape = RoundedCornerShape(8.dp),
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF1A4070)) },
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Surface(modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF8FAFC)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AMOUNT PAYABLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${plan.currency ?: "₹"}${String.format(Locale.ENGLISH, "%.0f", payableAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ManualInputLabel("Notes")
        OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSubmit(refNo, paymentDate, notes) },
            enabled = refNo.isNotBlank() && paymentDate.isNotBlank() && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004080))
        ) {
            if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Submit Payment Details", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ManualInputLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070), modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun BankDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun BenefitCard(plan: SubscriptionPlan) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = plan.name ?: "Plan Details", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003366))
            Spacer(modifier = Modifier.height(20.dp))
            val benefits = listOf("Single Edition (Print + Digital)", "Instant Digital Access", "Interviews", "Research Reports", "Event Passes", "Podcasts & Videos", "Webinars")
            benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Surface(modifier = Modifier.size(20.dp), shape = CircleShape, color = Color(0xFF003366)) {
                        Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = benefit, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SmallPlanSelectorCard(plan: SubscriptionPlan, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(110.dp).height(130.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF003366) else Color.LightGray.copy(alpha = 0.5f)), shadowElevation = if (isSelected) 4.dp else 0.dp) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = plan.name?.split(" ")?.firstOrNull() ?: "", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Text(text = plan.name?.split(" ")?.getOrNull(1) ?: "", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = plan.currency ?: "INR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003366))
            Text(text = plan.amount ?: "0.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003366))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${plan.term} days", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryContent(
    plan: SubscriptionPlan, offers: List<OfferDetail>, userAddresses: List<AddressDetail>, selectedAddress: AddressDetail?, 
    onAddressSelected: (AddressDetail) -> Unit, paymentMethods: List<PaymentMethodDetail>, 
    selectedPaymentMethod: PaymentMethodDetail?, onPaymentMethodSelected: (PaymentMethodDetail) -> Unit, 
    selectedOffer: OfferDetail?, onOfferSelected: (OfferDetail?) -> Unit, onContinue: () -> Unit
) {
    val baseAmount = plan.amount?.toDoubleOrNull() ?: 0.0
    val gstAmount = plan.gst?.toDoubleOrNull() ?: 0.0
    val discountAmount = selectedOffer?.let { offer -> if (offer.type == "fixed") offer.discount?.toDoubleOrNull() ?: 0.0 else (baseAmount * (offer.discount?.toDoubleOrNull() ?: 0.0)) / 100.0 } ?: 0.0
    val totalPayable = (baseAmount + gstAmount - discountAmount).coerceAtLeast(0.0)

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("ORDER SUMMARY", color = Color(0xFF00ACC1), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = plan.name ?: "Subscription Plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A4070))
        }
        item {
            Text("SELECT ADDRESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            if (userAddresses.isEmpty()) Text("No addresses found.", color = Color.Red, fontSize = 12.sp)
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(userAddresses) { address -> AddressMiniCard(address = address, isSelected = selectedAddress?.id == address.id, onClick = { onAddressSelected(address) }) } }
        }
        item {
            Text("AVAILABLE OFFER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Surface(modifier = Modifier.fillMaxWidth().height(56.dp).clickable { if (offers.isNotEmpty()) expanded = true }, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = selectedOffer?.let { "${it.code} - ₹${it.discount} off" } ?: "Select an offer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    offers.forEach { offer -> DropdownMenuItem(text = { Text("${offer.code} - ${offer.name}") }, onClick = { onOfferSelected(offer); expanded = false }) }
                    if (selectedOffer != null) DropdownMenuItem(text = { Text("Remove offer", color = Color.Red) }, onClick = { onOfferSelected(null); expanded = false })
                }
            }
        }
        item {
            Column {
                PriceRow("Plan amount", "${plan.currency ?: "₹"}${plan.amount}")
                PriceRow("Discount", "-${plan.currency ?: "₹"}${String.format(Locale.ENGLISH, "%.0f", discountAmount)}")
                PriceRow("GST", "${plan.currency ?: "₹"}${plan.gst ?: "0"}")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total payable", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A4070))
                    Text("${plan.currency ?: "₹"}${String.format(Locale.ENGLISH, "%.0f", totalPayable)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A4070))
                }
            }
        }
        item {
            Text("PAYMENT OPTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentMethods.forEach { method -> PaymentMethodRow(method = method, isSelected = selectedPaymentMethod?.id == method.id, onClick = { onPaymentMethodSelected(method) }) }
            }
        }
        item {
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366))) {
                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun VerificationTimerScreen(orderId: String, refNo: String, paidAt: String, notes: String, amount: Double, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()
    var timeLeft by remember { mutableIntStateOf(300) }
    var isVerified by remember { mutableStateOf(false) }
    var isTimedOut by remember { mutableStateOf(false) }
    var isCreatingTicket by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { while (timeLeft > 0 && !isVerified) { delay(1.seconds); timeLeft-- }; if (!isVerified) isTimedOut = true }
    // Polling Logic (Every 3 seconds using specific order-status API)
    LaunchedEffect(orderId) { 
        while (timeLeft > 0 && !isVerified) { 
            if (!appKey.isNullOrBlank() && userId != null && orderId != "N/A") { 
                try { 
                    val cleanKey = appKey!!.trim()
                    val res = RetrofitInstance.api.getSpecificOrderStatus(
                        cleanKey, userId, orderId,
                        cleanKey, userId, orderId
                    )
                    if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.valid == true) {
                        isVerified = true 
                    }
                } catch (e: Exception) { } 
            }; 
            delay(3.seconds) 
        } 
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            if (!isVerified && !isTimedOut) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                Spacer(modifier = Modifier.height(32.dp))
                Text("Confirming Activation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("We are verifying your transaction. This usually takes a few moments.", textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = String.format(Locale.ENGLISH, "%02d:%02d", timeLeft / 60, timeLeft % 60), fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A4070))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Due to high platform traffic, activation might be slightly delayed.", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
            } else if (isVerified) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Subscription Active!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Your payment was successful and your account is now activated.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(48.dp))
                Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) { Text("OK", fontWeight = FontWeight.Bold) }
            } else {
                Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Activation in Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Due to high traffic, activation is taking longer than expected. It will be resolved within 30 minutes. We have raised a support ticket for you.", textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { 
                    scope.launch { 
                        isCreatingTicket = true
                        try { 
                            if (!appKey.isNullOrBlank() && userId != null) { 
                                val k = appKey!!.trim()
                                    val detailMesg = "Manual Verification Required. \nOrder ID: $orderId \nRef No: $refNo \nPaid At: $paidAt \nAmount: $amount \nNotes: $notes".replace("\n", " ")
                                    val response = RetrofitInstance.api.createTicket(
                                        appKey = k, userId = userId, type = "order", department = "payment",
                                        subject = "Manual Activation Req - $orderId", message = detailMesg,
                                        appKeyQ = k, userIdQ = userId, typeQ = "order", departmentQ = "payment",
                                        subjectQ = "Manual Activation Req - $orderId", messageQ = detailMesg
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Support ticket raised successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error raising ticket: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                            } 
                        } catch (e: Exception) {} finally { isCreatingTicket = false; onSuccess() } 
                    } 
                }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), enabled = !isCreatingTicket) { 
                    if (isCreatingTicket) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("OK", fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}

@Composable
fun AddressMiniCard(address: AddressDetail, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(200.dp).height(100.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = if (isSelected) Color(0xFFF0F7FF) else Color(0xFFF8FAFC), border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF1A4070) else Color.LightGray.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = if (address.type?.lowercase() == "home") Icons.Default.Home else Icons.Default.Business, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isSelected) Color(0xFF1A4070) else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = address.name ?: "", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = address.line1 ?: "", fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = "${address.city}, ${address.state}", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun PaymentMethodRow(method: PaymentMethodDetail, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFF0F7FF) else Color.White,
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF1A4070) else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1A4070)))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = method.providerName ?: "", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (!method.displayNote.isNullOrBlank()) Text(text = method.displayNote, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A4070))
    }
}
