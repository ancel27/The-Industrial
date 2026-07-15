package kivaa.app.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kivaa.app.BuildConfig
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.DynamicLogo
import kivaa.app.ui.theme.ThemeManager
import java.util.*

enum class AuthState {
    Login,
    Signup,
    Verification,
    ForgotPassword
}

data class SignupDetails(
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val secc: String = "",
    val dob: String = ""
)

@Composable
fun AuthContainer(onAuthSuccess: (Int, String?) -> Unit) {
    var currentState by remember { mutableStateOf<AuthState>(AuthState.Login) }
    val selectedAppKey = BuildConfig.PLATFORM_KEY
    var signupDetails by remember { mutableStateOf(SignupDetails()) }

    when (currentState) {
        AuthState.Login -> {
            LoginScreen(
                appKey = selectedAppKey,
                onLoginSuccess = { userId, userName -> onAuthSuccess(userId, userName) },
                onNavigateToSignUp = { currentState = AuthState.Signup },
                onNavigateToForgotPassword = { currentState = AuthState.ForgotPassword }
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
                appKey = selectedAppKey,
                onSuccess = { currentState = AuthState.Login },
                onBackToLogin = { currentState = AuthState.Login }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    appKey: String,
    onLoginSuccess: (Int, String?) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val performLogin = {
        if (!isLoading) {
            scope.launch {
                isLoading = true
                try {
                    val cleanKey = appKey.trim()
                    val cleanEmail = email.trim()
                    val cleanPass = password.trim()
                    
                    val response = RetrofitInstance.api.login(
                        cleanKey, cleanEmail, cleanPass,
                        cleanKey, cleanEmail, cleanPass
                    )
                    val body = response.body()
                    if (body?.userHeader == 200 && (body.total ?: 0) > 0 && !body.userDetails.isNullOrEmpty()) {
                        val user = body.userDetails.first()
                        if (user.id != null) {
                            ThemeManager.setUserId(user.id)
                            ThemeManager.setUserName(user.name)
                            onLoginSuccess(user.id, user.name)
                        } else {
                            Toast.makeText(context, "Invalid User Data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DynamicLogo(modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { performLogin() }),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
        
        TextButton(onClick = onNavigateToForgotPassword, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot Password?")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { performLogin() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Login", fontWeight = FontWeight.Bold)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?")
            TextButton(onClick = onNavigateToSignUp) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SignupScreen(onContinue: (SignupDetails) -> Unit, onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            dob = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val validateAndContinue = {
        if (name.isNotBlank() && email.isNotBlank() && mobile.isNotBlank() && password.isNotBlank() && dob.isNotBlank()) {
            onContinue(SignupDetails(name, email, mobile, password, dob))
        } else {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler { onBackToLogin() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        DynamicLogo(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Create Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), 
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = mobile, onValueChange = { mobile = it },
            label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), 
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), 
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = dob, onValueChange = { },
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
            shape = RoundedCornerShape(12.dp), readOnly = true, enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { validateAndContinue() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue to Verify", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBackToLogin) { Text("Back to Login") }
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
    val scope = rememberCoroutineScope()
    var otp by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }

    val performVerification = {
        if (otp.length == 6 && !isVerifying) {
            scope.launch {
                isVerifying = true
                try {
                    val verifyRes = RetrofitInstance.api.verifyEmail(appKey, signupDetails.email, otp)
                    if (verifyRes.isSuccessful && verifyRes.body()?.responseDetails?.firstOrNull()?.success == true) {
                        val signupResponse = RetrofitInstance.api.signup(
                            appKey, signupDetails.name, signupDetails.email,
                            signupDetails.mobile, signupDetails.secc, signupDetails.dob
                        )
                        if (signupResponse.isSuccessful && signupResponse.body()?.responseHeader == 200) {
                            val user = signupResponse.body()?.responseDetails?.firstOrNull()
                            if (user?.id != null) {
                                ThemeManager.setUserId(user.id)
                                ThemeManager.setUserName(user.name)
                                onVerificationSuccess(user.id, user.name)
                            } else {
                                Toast.makeText(context, "Account created, please login", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Verification Error", Toast.LENGTH_SHORT).show()
                } finally { isVerifying = false }
            }
        }
    }

    BackHandler { onBackToSignup() }

    LaunchedEffect(Unit) {
        if (!otpSent) {
            isSending = true
            try {
                val res = RetrofitInstance.api.sendVerification(appKey, signupDetails.email)
                if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.success == true) {
                    otpSent = true
                    Toast.makeText(context, "Verification code sent to email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send code", Toast.LENGTH_SHORT).show()
            } finally { isSending = false }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verify Email", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Sent to ${signupDetails.email}", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otp, onValueChange = { if (it.length <= 6) otp = it },
            label = { Text("6-Digit Code") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), 
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { performVerification() })
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { performVerification() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = otp.length == 6 && !isVerifying
        ) {
            if (isVerifying) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Verify & Sign Up", fontWeight = FontWeight.Bold)
        }
        
        TextButton(onClick = { /* Resend Logic */ }, enabled = !isSending) {
            Text("Resend Code")
        }
        TextButton(onClick = onBackToSignup) { Text("Back") }
    }
}

@Composable
fun ForgotPasswordScreen(
    appKey: String,
    onSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var step by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP & Password
    var isLoading by remember { mutableStateOf(false) }

    val handleAction = {
        if (!isLoading) {
            scope.launch {
                isLoading = true
                try {
                    if (step == 1) {
                        val res = RetrofitInstance.api.sendVerification(appKey, email.trim())
                        if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.success == true) {
                            step = 2
                            Toast.makeText(context, "Reset code sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Email not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val res = RetrofitInstance.api.resetPassword(appKey, email.trim(), otp.trim(), newPassword.trim())
                        if (res.isSuccessful && res.body()?.responseDetails?.firstOrNull()?.success == true) {
                            Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Invalid code or failed to reset", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    BackHandler { onBackToLogin() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (step == 1) "Reset Password" else "Verify & Reset",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (step == 1) "Enter your email to receive a reset code" else "Enter the code sent to your email",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (step == 1) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { handleAction() })
            )
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("6-Digit Code") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleAction() }),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { handleAction() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && (if (step == 1) email.isNotBlank() else otp.length == 6 && newPassword.isNotBlank())
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (step == 1) "Send Code" else "Reset Password", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = onBackToLogin) {
            Text("Cancel")
        }
    }
}
