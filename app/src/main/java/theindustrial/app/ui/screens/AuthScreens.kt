package theindustrial.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import theindustrial.app.BuildConfig
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.DynamicLogo
import theindustrial.app.ui.theme.ThemeManager

sealed class AuthState {
    object PlatformSelection : AuthState()
    object Login : AuthState()
    object Signup : AuthState()
    object Verification : AuthState()
    object ForgotPassword : AuthState()
}

// Simple data class to hold signup details between screens
data class SignupDetails(
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val secc: String = "",
    val dob: String = ""
)

@Composable
fun AuthContainer(onAuthSuccess: (Int, String?) -> Unit) {
    var currentState by remember { mutableStateOf<AuthState>(AuthState.PlatformSelection) }
    var selectedAppKey by remember { mutableStateOf("") }
    var signupDetails by remember { mutableStateOf(SignupDetails()) }

    when (currentState) {
        AuthState.PlatformSelection -> {
            PlatformSelectionScreen(
                onPlatformSelected = { appKey ->
                    selectedAppKey = appKey
                    currentState = AuthState.Login
                }
            )
        }
        AuthState.Login -> {
            LoginScreen(
                appKey = selectedAppKey,
                onLoginSuccess = { userId, userName -> onAuthSuccess(userId, userName) },
                onNavigateToSignUp = { currentState = AuthState.Signup },
                onNavigateToForgotPassword = { currentState = AuthState.ForgotPassword },
                onBackToPlatform = { currentState = AuthState.PlatformSelection }
            )
        }
        AuthState.Signup -> {
            SignupScreen(
                onContinue = { details ->
                    signupDetails = details
                    currentState = AuthState.Verification
                },
                onBackToLogin = { currentState = AuthState.Login }
            )
        }
        AuthState.Verification -> {
            VerificationScreen(
                appKey = selectedAppKey,
                signupDetails = signupDetails,
                onVerificationSuccess = { userId, userName -> onAuthSuccess(userId, userName) },
                onBackToSignup = { currentState = AuthState.Signup }
            )
        }
        AuthState.ForgotPassword -> {
            ForgotPasswordScreen(
                onBackToLogin = { currentState = AuthState.Login }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformSelectionScreen(onPlatformSelected: (String) -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    var selectedPlatform by remember { mutableStateOf("Select Platform") }
    var expanded by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val platforms = mapOf(
        "Factory Future" to BuildConfig.KEY_FACTORY_FUTURE,
        "Platform B" to BuildConfig.KEY_PLATFORM_B
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please select your industrial platform to continue",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedPlatform,
                onValueChange = {},
                readOnly = true,
                label = { Text("Industrial Platform") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                platforms.keys.forEach { platform ->
                    DropdownMenuItem(
                        text = { Text(platform) },
                        onClick = {
                            selectedPlatform = platform
                            expanded = false
                            
                            val appKey = platforms[platform]?.trim()
                            if (!appKey.isNullOrBlank()) {
                                scope.launch {
                                    isFetching = true
                                    val response = RetrofitInstance.api.getConfig(appKey, appKey)
                                    if (response.isSuccessful) {
                                        val config = response.body()?.responseDetails?.firstOrNull()
                                        if (config != null) {
                                            ThemeManager.updateConfig(config)
                                            preferenceManager.setAppKey(appKey)
                                            preferenceManager.setCachedConfig(config) // Save to cache immediately
                                            onPlatformSelected(appKey)
                                        }
                                    }
                                    isFetching = false
                                }
                            }
                        }
                    )
                }
            }
        }
        
        if (isFetching) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun LoginScreen(
    appKey: String,
    onLoginSuccess: (Int, String?) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onBackToPlatform: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicLogo(modifier = Modifier.size(120.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Login to your account",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot Password?",
            modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgotPassword() },
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()
                        val trimmedAppKey = appKey.trim()
                        
                        val response = RetrofitInstance.api.login(
                            appKey = trimmedAppKey, 
                            email = trimmedEmail, 
                            secc = trimmedPassword
                        )
                        if (response.isSuccessful) {
                            val body = response.body()
                            android.util.Log.d("LOGIN_DEBUG", "Body: $body")
                            
                            if (body?.userHeader == 200 && (body.total ?: 0) > 0 && !body.userDetails.isNullOrEmpty()) {
                                val user = body.userDetails.first()
                                if (user.id != null) {
                                    onLoginSuccess(user.id, user.name)
                                } else {
                                    errorMessage = "User ID missing from server response."
                                }
                            } else {
                                val total = body?.total ?: 0
                                val header = body?.userHeader ?: "null"
                                errorMessage = "Invalid credentials (H:$header, T:$total)."
                            }
                        } else {
                            errorMessage = "Server error (${response.code()})"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Sign Up",
                modifier = Modifier.clickable { onNavigateToSignUp() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackToPlatform) {
            Text("Change Platform")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onContinue: (SignupDetails) -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= Calendar.getInstance().get(Calendar.YEAR)
            }
        }
    )
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dob = formatter.format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicLogo(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Create Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = dob,
            onValueChange = { },
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onContinue(SignupDetails(name.trim(), email.trim(), mobile.trim(), password.trim(), dob.trim())) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && dob.isNotBlank()
        ) {
            Text("Continue")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToLogin) { Text("Already have an account? Login") }
    }
}

@Composable
fun VerificationScreen(
    appKey: String,
    signupDetails: SignupDetails,
    onVerificationSuccess: (Int, String?) -> Unit,
    onBackToSignup: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    var otp by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Send OTP on launch
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.sendVerification(appKey.trim(), signupDetails.email.trim())
            if (response.isSuccessful && response.body()?.responseDetails?.firstOrNull()?.success == true) {
                isSendingOtp = false
            } else {
                errorMessage = "Failed to send verification code."
            }
        } catch (e: Exception) {
            errorMessage = "Network error."
        } finally {
            isSendingOtp = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicLogo(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Verify Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Code sent to ${signupDetails.email}", textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            label = { Text("6-Digit Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isSendingOtp
        )

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isVerifying = true
                    errorMessage = null
                    try {
                        // 1. Verify Email
                        val verifyResponse = RetrofitInstance.api.verifyEmail(appKey.trim(), signupDetails.email.trim(), otp.trim())
                        if (verifyResponse.isSuccessful && verifyResponse.body()?.responseDetails?.firstOrNull()?.verified == true) {
                            // 2. Final Signup
                            val signupResponse = RetrofitInstance.api.signup(
                                appKey.trim(), signupDetails.name.trim(), signupDetails.email.trim(),
                                signupDetails.mobile.trim(), signupDetails.secc.trim(), signupDetails.dob.trim()
                            )
                            if (signupResponse.isSuccessful && signupResponse.body()?.responseHeader == 200) {
                                val user = signupResponse.body()?.responseDetails?.firstOrNull()
                                if (user?.id != null) {
                                    onVerificationSuccess(user.id, user.name)
                                } else {
                                    errorMessage = "Account created, but ID missing. Please Login."
                                }
                            } else {
                                errorMessage = "Signup failed after verification."
                            }
                        } else {
                            errorMessage = "Invalid verification code."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error during verification."
                    } finally {
                        isVerifying = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = otp.length == 6 && !isVerifying && !isSendingOtp
        ) {
            if (isVerifying || isSendingOtp) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Verify & Create Account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToSignup) { Text("Back to Edit Details") }
    }
}

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Please contact your platform administrator or support team to reset your industrial account password.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }
    }
}
