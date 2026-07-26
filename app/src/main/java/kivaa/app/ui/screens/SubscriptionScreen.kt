package kivaa.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onGoToOrders: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()
    
    // Dynamic Theme Integration
    val config = ThemeManager.currentConfig.value
    val primaryColor = ThemeManager.getColor(config?.theme?.primary, Color(0xFF003366))
    val accentColor = ThemeManager.getColor(config?.theme?.accent, Color(0xFF00ACC1))

    var checkoutUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorOccurred by remember { mutableStateOf(false) }

    // Direct Portal Launch
    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank() && userId != null) {
            try {
                isLoading = true
                errorOccurred = false
                val k = appKey!!.trim()
                val res = RetrofitInstance.api.getPaymentPageUrl(k, userId, k, userId)
                if (res.isSuccessful) {
                    val data = res.body()?.responseDetails?.firstOrNull()
                    if (!data?.paymentUrl.isNullOrBlank()) {
                        checkoutUrl = data?.paymentUrl
                    } else {
                        errorOccurred = true
                    }
                } else {
                    errorOccurred = true
                }
            } catch (e: Exception) {
                errorOccurred = true
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Initializing Secure Payment...", color = Color.Gray)
            }
        } else if (errorOccurred) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Could not load payment portal.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Retry logic
                        scope.launch {
                            val k = appKey!!.trim()
                            val res = RetrofitInstance.api.getPaymentPageUrl(k, userId!!, k, userId)
                            if (res.isSuccessful) {
                                checkoutUrl = res.body()?.responseDetails?.firstOrNull()?.paymentUrl
                                errorOccurred = checkoutUrl == null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Retry")
                }
            }
        } else if (checkoutUrl != null) {
            // Embedded Checkout WebView with Pull to Refresh
            EmbeddedCheckoutWebView(
                url = checkoutUrl!!,
                accentColor = accentColor,
                onPaymentSuccess = { email, token ->
                    scope.launch {
                        if (!email.isNullOrBlank()) preferenceManager.saveUserEmail(email)
                        if (!token.isNullOrBlank()) preferenceManager.saveAppKey(token)
                        Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()
                        onGoToOrders()
                    }
                },
                onPaymentFailure = { reason ->
                    Toast.makeText(context, reason ?: "Payment Failed", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbeddedCheckoutWebView(
    url: String,
    accentColor: Color,
    onPaymentSuccess: (String?, String?) -> Unit,
    onPaymentFailure: (String?) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            val swipeRefreshLayout = SwipeRefreshLayout(ctx).apply {
                setColorSchemeColors(accentColor.toArgb())
            }
            val webView = WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.databaseEnabled = true
                
                webChromeClient = WebChromeClient()
                
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun postMessage(jsonString: String) {
                        try {
                            val data = JSONObject(jsonString)
                            if (data.optString("type") == "subscriptionPaymentResult") {
                                val status = data.optString("status")
                                if (status == "success") {
                                    onPaymentSuccess(data.optString("user_email"), data.optString("token"))
                                } else {
                                    onPaymentFailure(data.optString("reason"))
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        swipeRefreshLayout.isRefreshing = false
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val nextUrl = request?.url?.toString() ?: ""
                        if (nextUrl.startsWith("http")) return false
                        
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(nextUrl))
                            ctx.startActivity(intent)
                            return true
                        } catch (e: Exception) { return true }
                    }
                }
                
                loadUrl(url)
            }
            
            swipeRefreshLayout.addView(webView)
            swipeRefreshLayout.setOnRefreshListener {
                webView.reload()
            }
            swipeRefreshLayout
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Utility to convert Compose Color to Int for SwipeRefreshLayout
fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
           (this.red * 255.0f + 0.5f).toInt() shl 16 or
           (this.green * 255.0f + 0.5f).toInt() shl 8 or
           (this.blue * 255.0f + 0.5f).toInt()
}
