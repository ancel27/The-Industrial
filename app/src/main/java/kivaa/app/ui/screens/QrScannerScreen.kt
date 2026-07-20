package kivaa.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kivaa.app.data.local.PreferenceManager
import kivaa.app.data.model.QrLoginRequest
import kivaa.app.data.remote.RetrofitInstance
import kivaa.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(onQrScanned: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var lastScannedValue by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler { onBack() }

    suspend fun handleQrData(qrData: String) {
        if (isProcessing) return
        
        if (!qrData.startsWith("kivaa://")) {
            errorMessage = "Invalid QR. This doesn't look like a Kivaa code!"
            return
        }

        if (qrData.contains("kivaa://login")) {
            try {
                val uri = android.net.Uri.parse(qrData)
                val platform = uri.getQueryParameter("pl") ?: ""
                val sessionUid = uri.getQueryParameter("uid") ?: ""
                val secretKey = uri.getQueryParameter("sk") ?: ""
                val timestamp = uri.getQueryParameter("ts")?.toLongOrNull() ?: 0L

                val currentAppLabel = when {
                    appKey?.contains("factory_future") == true -> "FACTORY_FUTURE"
                    appKey?.contains("the_industrial") == true -> "THE_INDUSTRIAL"
                    appKey?.contains("things_of_business") == true -> "THINGS_OF_BUSINESS"
                    else -> ""
                }

                if (platform != currentAppLabel && currentAppLabel.isNotEmpty()) {
                    errorMessage = when (platform) {
                        "THE_INDUSTRIAL" -> "I know you want to explore The Industrial, but you're in the Factory Future app! Switch apps to dive in."
                        "FACTORY_FUTURE" -> "Wait! That's a Factory Future login. You'll need the right app to see the future of manufacturing."
                        else -> "Wrong neighborhood! This login belongs to $platform, but you're using ${currentAppLabel.replace("_", " ")}."
                    }
                } else if (userId != null && !appKey.isNullOrBlank()) {
                    isProcessing = true
                    val response = RetrofitInstance.api.approveQrLogin(
                        appKey!!.trim(), userId,
                        QrLoginRequest(platform, sessionUid, secretKey, timestamp)
                    )
                    if (response.isSuccessful && response.body()?.success == true) {
                        successMessage = "Login Approved! You are being signed in on your browser."
                        delay(2000)
                        onBack()
                    } else {
                        errorMessage = response.body()?.message ?: "Login session expired or invalid."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to parse login details. Try scanning again."
            } finally {
                isProcessing = false
            }
        } else if (qrData.contains("kivaa://read")) {
            try {
                val uri = android.net.Uri.parse(qrData)
                val resourceId = uri.getQueryParameter("rid") ?: ""
                if (resourceId.isNotBlank()) {
                    onQrScanned(resourceId)
                } else {
                    errorMessage = "Invalid read code. Content ID missing."
                }
            } catch (e: Exception) {
                errorMessage = "Failed to open content. Try scanning again."
            }
        } else {
            errorMessage = "This Kivaa feature is coming soon!"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // ... camera preview ...
                
                // Floating Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val barcodeScanner = BarcodeScanning.getClient()
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                @OptIn(ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { value ->
                                                    if (value != lastScannedValue) {
                                                        lastScannedValue = value
                                                        scope.launch { handleQrData(value) }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                                )
                            } catch (exc: Exception) { }
                        }, ContextCompat.getMainExecutor(context))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Align QR code within the frame",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp)
                    )
                }

                if (isProcessing) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            } else {
                Text(
                    "Camera permission is required to scan QR codes.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null; lastScannedValue = null },
            title = { Text("Oops!", fontWeight = FontWeight.ExtraBold) },
            text = { Text(errorMessage!!, fontSize = 16.sp) },
            confirmButton = {
                Button(onClick = { errorMessage = null; lastScannedValue = null }) {
                    Text("Try Again")
                }
            }
        )
    }

    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Success", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = { Text(successMessage!!, fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = { /* Auto-handled */ }) {
                    Text("OK")
                }
            }
        )
    }
}
