package com.deliverydriver.resources.scanner

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: ScanViewModel = ScanViewModel,
    onNavigateToResults: () -> Unit = {},
    onNavigateToResources: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var permissionDeniedForever by remember { mutableStateOf(false) }

    // Track whether the system permission dialog was actually shown
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            try {
                cameraPermissionState.launchPermissionRequest()
            } catch (_: Exception) {
                // Permission request failed — likely "Don't ask again" was checked
                permissionDeniedForever = true
            }
        }
    }

    // If permission was granted after a previous denial
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            permissionDeniedForever = !cameraPermissionState.status.shouldShowRationale
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraScannerView(viewModel, state, onNavigateToResults, onNavigateToResources)
    } else {
        PermissionDeniedScreen(
            isPermanentlyDenied = permissionDeniedForever,
            onRequestPermission = {
                try {
                    cameraPermissionState.launchPermissionRequest()
                } catch (_: Exception) {
                    permissionDeniedForever = true
                }
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun PermissionDeniedScreen(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isPermanentlyDenied) Icons.Filled.Settings else Icons.Filled.NoPhotography,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (isPermanentlyDenied)
                Color(0xFFFFB800)
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isPermanentlyDenied) "Camera access blocked" else "Camera permission needed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPermanentlyDenied)
                "You previously denied camera access and checked \"Don't ask again\".\n\nPlease enable camera permission in Settings to use the scanner."
            else
                "Point your camera at the yellow Driver Aid stickers — numbers are detected automatically.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isPermanentlyDenied) {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Settings")
            }
        } else {
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Camera Permission")
            }
        }
    }
}

// ── Camera Scanner UI ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraScannerView(
    viewModel: ScanViewModel,
    state: ScanState,
    onNavigateToResults: () -> Unit,
    onNavigateToResources: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "📷 Package Scanner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Point camera at yellow Driver Aid stickers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            imageVector = if (state.flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Toggle flash",
                            tint = if (state.flashOn) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToResources) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "Resources"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (state.scannedPackages.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToResults,
                    icon = { Icon(Icons.Filled.Sort, contentDescription = null) },
                    text = { Text("${state.scannedPackages.size} scanned — Organize") },
                    containerColor = Color(0xFF00A67E),
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Camera preview
            CameraPreview(
                onNumberDetected = { viewModel.onNumberDetected(it) },
                flashOn = state.flashOn,
                onError = { msg -> viewModel.setError(msg) },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning overlay - bracket frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            // Scanner instruction text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Frame the yellow sticker in the square",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Error banner
            state.error?.let { errorMsg ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF4444).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp, top = 40.dp)
            ) {
                // Last detected number
                AnimatedVisibility(
                    visible = state.lastDetectedNumber != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.lastDetectedNumber?.let { num ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00A67E).copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF00A67E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Detected: #$num",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00A67E)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Count badge
                if (state.scannedPackages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${state.scannedPackages.size} packages logged",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(
                                    onClick = { viewModel.clearAll() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Clear",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Rolling log of scanned numbers
                if (state.scannedPackages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .padding(8.dp)
                        ) {
                            items(state.scannedPackages.takeLast(8).reversed()) { pkg ->
                                Text(
                                    text = "#${pkg.driverAidNumber}${if (pkg.count > 1) " (×${pkg.count})" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                            if (state.scannedPackages.size > 8) {
                                item {
                                    Text(
                                        text = "... and ${state.scannedPackages.size - 8} more",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Camera Preview with ML Kit OCR ─────────────────────────────────

@Composable
fun CameraPreview(
    onNumberDetected: (Int) -> Unit,
    flashOn: Boolean,
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var lastDetectionTime = remember { mutableLongStateOf(0L) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val DETECTION_THROTTLE_MS = 500L

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            analyzerExecutor.shutdown()
            textRecognizer.close()
        }
    }

    // Flash toggle
    LaunchedEffect(flashOn) {
        try {
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                @Suppress("DEPRECATION")
                camera?.cameraControl?.enableTorch(flashOn)
            }
        } catch (_: Exception) {
            // Flash toggle failed silently
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(640, 480))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                textRecognizer.process(inputImage)
                                    .addOnSuccessListener { visionText ->
                                        val now = System.currentTimeMillis()
                                        if (now - lastDetectionTime.value < DETECTION_THROTTLE_MS) return@addOnSuccessListener

                                        val numbers = mutableListOf<Int>()
                                        for (block in visionText.textBlocks) {
                                            for (line in block.lines) {
                                                val text = line.text.trim()
                                                val digits = text.filter { it.isDigit() }
                                                if (digits.length in 2..4) {
                                                    val num = digits.toIntOrNull()
                                                    if (num != null && num in 1..9999) {
                                                        numbers.add(num)
                                                    }
                                                }
                                            }
                                        }

                                        if (numbers.isNotEmpty()) {
                                            lastDetectionTime.value = now
                                            onNumberDetected(numbers.first())
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        provider.unbindAll()

                        val cam = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        camera = cam

                    } catch (e: Exception) {
                        onError("Camera failed: ${e.localizedMessage ?: "unknown error"}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

            } catch (e: Exception) {
                onError("Camera init failed: ${e.localizedMessage ?: "unknown error"}")
            }

            previewView
        },
        modifier = modifier
    )
}
