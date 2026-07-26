package kivaa.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.SubscriptionPlan
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.DynamicLogo
import kivaa.app.ui.theme.ThemeManager
import org.json.JSONObject

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    val config = ThemeManager.currentConfig.value
    val primaryColor = ThemeManager.getColor(config?.theme?.primary, Color(0xFF003366))

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Skip Button
        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
        ) {
            Text("Skip", color = Color.Gray, fontWeight = FontWeight.Medium)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "OnboardingStep"
                ) { step ->
                    when (step) {
                        1 -> WelcomeStep(primaryColor)
                        2 -> PreferencesStep(primaryColor)
                        3 -> SubscriptionStep(primaryColor)
                    }
                }
            }

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (currentStep == index + 1) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (currentStep == index + 1) primaryColor else Color.LightGray)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 3) currentStep++
                        else onFinish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp).padding(horizontal = 8.dp)
                ) {
                    Text(if (currentStep == 3) "Get Started" else "Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(primaryColor: Color) {
    val platformName = ThemeManager.currentConfig.value?.platformName ?: "Industrial"
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicLogo(modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to $platformName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your gateway to the latest industrial insights, technology trends, and professional growth. Let's personalize your experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesStep(primaryColor: Color) {
    val keywords = listOf(
        "Automation", "Industry 4.0", "Robotics", "IoT", "Smart Factory",
        "Sustainability", "AI", "Manufacturing", "Supply Chain", "Technology",
        "Energy", "Cybersecurity", "Electric Vehicles", "Aerospace", "Pharma"
    )
    var selectedKeywords by remember { mutableStateOf(setOf<String>()) }
    var customKeyword by remember { mutableStateOf("") }
    
    val userId = ThemeManager.userId.value
    val config = ThemeManager.currentConfig.value
    // Using explicit dbKey or platform API key for calls
    val appKey = config?.dbKey ?: "kivaa_factory_future_mobile_Kt9AKaR7Q3pJ_Y9WO1NxOogvE6nTnhbj"
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val onAddKeyword = { kw: String ->
        if (kw.isNotBlank() && userId != null) {
            val cleanKw = kw.trim()
            if (!selectedKeywords.contains(cleanKw)) {
                selectedKeywords = selectedKeywords + cleanKw
                scope.launch {
                    try {
                        RetrofitInstance.api.addPreference(appKey, userId, cleanKw, appKey, userId, cleanKw)
                    } catch (e: Exception) { 
                        android.util.Log.e("Onboarding", "Pref Error", e)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp)) {
        Text(
            text = "Personalize Your Feed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Text(
            text = "Select topics that interest you or add your own.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        // Custom Keyword Input
        OutlinedTextField(
            value = customKeyword,
            onValueChange = { customKeyword = it },
            placeholder = { Text("Add custom topic (e.g. 5G)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { 
                    onAddKeyword(customKeyword)
                    customKeyword = ""
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = primaryColor)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAddKeyword(customKeyword)
                customKeyword = ""
                focusManager.clearFocus()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Chips Group
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedKeywords.forEach { kw ->
                InputChip(
                    selected = true,
                    onClick = { selectedKeywords = selectedKeywords - kw },
                    label = { Text(kw, fontSize = 11.sp) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.1f),
                        selectedLabelColor = primaryColor
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        selectedBorderColor = primaryColor, 
                        selectedBorderWidth = 1.dp,
                        enabled = true,
                        selected = true
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(keywords) { keyword ->
                val isSelected = selectedKeywords.contains(keyword)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            onAddKeyword(keyword)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) primaryColor else Color.LightGray.copy(alpha = 0.5f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = keyword,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) primaryColor else Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionStep(primaryColor: Color) {
    var plans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    
    // Explicitly fetching appKey from current config to avoid dbKey mismatch
    val appKey = ThemeManager.currentConfig.value?.dbKey ?: "kivaa_factory_future_mobile_Kt9AKaR7Q3pJ_Y9WO1NxOogvE6nTnhbj"

    LaunchedEffect(appKey) {
        try {
            val res = RetrofitInstance.api.getSubscriptionPlans(appKey.trim())
            if (res.isSuccessful) {
                val fetched = res.body()?.plans ?: emptyList()
                plans = fetched
                if (fetched.isNotEmpty()) selectedPlan = fetched.first()
            }
        } catch (e: Exception) {
            android.util.Log.e("Onboarding", "Plan fetch failed", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp)) {
        Text(
            text = "Membership Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Text(
            text = "Experience premium industrial insights.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (plans.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            // Top: Selected Plan Details Card
            selectedPlan?.let { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = plan.name ?: "Plan Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val benefitLines = plan.details?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
                        benefitLines.take(5).forEach { benefit ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = primaryColor) {
                                    Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp)) }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = benefit.trim(), fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Choose your plan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom: Selection Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(plans) { plan ->
                    val isSelected = selectedPlan?.id == plan.id
                    Surface(
                        modifier = Modifier
                            .width(110.dp)
                            .height(120.dp)
                            .clickable { selectedPlan = plan },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp, 
                            color = if (isSelected) primaryColor else Color.LightGray.copy(alpha = 0.5f)
                        ),
                        shadowElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp), 
                            horizontalAlignment = Alignment.CenterHorizontally, 
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = plan.name?.split(" ")?.firstOrNull() ?: "", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray, 
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${plan.currency ?: "INR"} ${plan.amount ?: "0"}", 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
