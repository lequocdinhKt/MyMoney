package com.example.mymoney.ui.camera

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/** Các mức tiền phổ biến ở Việt Nam */
private val PRESET_AMOUNTS = listOf<Long>(
    10_000, 15_000, 20_000, 25_000, 30_000, 50_000, 75_000,
    100_000, 150_000, 200_000, 300_000, 500_000, 1_000_000, 2_000_000, 5_000_000
)

/** Format chuỗi chữ số thành dạng 500.000 */
private fun formatVndDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val reversed = digits.reversed()
    return reversed.chunked(3).joinToString(".").reversed()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    walletId: Long,
    onNavigateBack: () -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    val cameraViewModel: CameraViewModel = viewModel(
        factory = CameraViewModel.factory(context, walletId)
    )
    val saveState by cameraViewModel.saveState.collectAsState()
    val photoTransactions by cameraViewModel.photoTransactions.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is CameraViewModel.SaveState.Success) {
            capturedPhotoUri?.let { onPhotoTaken(it) }
            cameraViewModel.resetState()
            onNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            CameraPreviewWithControls(
                flashEnabled = flashEnabled,
                lensFacing = lensFacing,
                capturedPhotoUri = capturedPhotoUri,
                saveState = saveState,
                onFlashToggle = { flashEnabled = !flashEnabled },
                onSwitchCamera = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                },
                onClose = onNavigateBack,
                onPhotoCaptured = { uri -> capturedPhotoUri = uri },
                onRetake = {
                    capturedPhotoUri = null
                    cameraViewModel.resetState()
                },
                onSave = { amount ->
                    capturedPhotoUri?.let { cameraViewModel.savePhoto(it, amount) }
                },
                onShowHistory = { showHistory = true }
            )
        } else {
            // Permission denied state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Cần quyền truy cập camera", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Cho phép") }
                }
            }
        }

        // ── History BottomSheet ──
        if (showHistory) {
            ModalBottomSheet(
                onDismissRequest = { showHistory = false },
                containerColor = Color(0xFF1A1A2E),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
            ) {
                PhotoHistorySheet(
                    transactions = photoTransactions,
                    onSaveToAlbum = { tx -> cameraViewModel.savePhotoToAlbum(tx.imagePath) },
                    onDelete = { tx ->
                        cameraViewModel.deletePhotoTransaction(tx.id, tx.walletId, tx.amount, tx.type, tx.imagePath)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewWithControls(
    flashEnabled: Boolean,
    lensFacing: Int,
    capturedPhotoUri: Uri?,
    saveState: CameraViewModel.SaveState,
    onFlashToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onClose: () -> Unit,
    onPhotoCaptured: (Uri) -> Unit,
    onRetake: () -> Unit,
    onSave: (Double) -> Unit,
    onShowHistory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var rawDigits by remember { mutableStateOf("") }

    // Reset amount khi chụp lại
    LaunchedEffect(capturedPhotoUri) {
        if (capturedPhotoUri == null) rawDigits = ""
    }

    LaunchedEffect(flashEnabled, camera) {
        camera?.cameraControl?.enableTorch(flashEnabled)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top controls ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(icon = Icons.Filled.Close, contentDescription = "Quay lại", onClick = onClose)
            if (capturedPhotoUri == null) {
                CircularIconButton(
                    icon = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Flash",
                    onClick = onFlashToggle,
                    tint = if (flashEnabled) Color(0xFFFFD700) else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // ── Preview / Camera box ──
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(40.dp))
        ) {
            if (capturedPhotoUri != null) {
                // Show captured photo preview
                val bitmap = remember(capturedPhotoUri) {
                    BitmapFactory.decodeFile(capturedPhotoUri.path)?.asImageBitmap()
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Ảnh đã chụp",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // ── Amount input overlay at bottom of photo ──
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.50f))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val displayText = if (rawDigits.isEmpty()) ""
                                      else "${formatVndDigits(rawDigits)} đ"
                    BasicTextField(
                        value = TextFieldValue(
                            text = displayText,
                            selection = TextRange(displayText.length)
                        ),
                        onValueChange = { tfv ->
                            rawDigits = tfv.text.filter(Char::isDigit).take(12)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (rawDigits.isEmpty()) {
                                    Text(
                                        text = "Nhập số tiền...",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Show live camera preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder()
                                .setFlashMode(
                                    if (flashEnabled) ImageCapture.FLASH_MODE_ON
                                    else ImageCapture.FLASH_MODE_OFF
                                ).build()
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing).build()
                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageCapture
                                )
                            } catch (e: Exception) { e.printStackTrace() }
                        }, ContextCompat.getMainExecutor(context))
                    }
                )
            }
        }

        // ── Bottom section ──
        if (capturedPhotoUri == null) {
            // Camera mode: spacer + shutter row
            Spacer(modifier = Modifier.weight(0.35f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(icon = Icons.Filled.PhotoLibrary, contentDescription = "Lịch sử ảnh", onClick = { /* TODO */ }, useThemeColor = true)
                ShutterButton(onClick = { capturePhoto(context, imageCapture, onPhotoCaptured) })
                CircularIconButton(icon = Icons.Filled.Category, contentDescription = "Chọn danh mục", onClick = { /* TODO */ }, useThemeColor = true)
            }
        } else {
            // Preview mode: presets + save/retake
            Spacer(modifier = Modifier.height(12.dp))

            // Preset amounts row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PRESET_AMOUNTS) { amount ->
                    val isSelected = rawDigits == amount.toString()
                    val bg = if (isSelected) MaterialTheme.colorScheme.primary
                             else Color.White.copy(alpha = 0.15f)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bg)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { rawDigits = amount.toString() }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${formatVndDigits(amount.toString())}đ",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Retake + Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = Icons.Filled.Replay,
                    contentDescription = "Chụp lại",
                    onClick = onRetake,
                    modifier = Modifier.size(64.dp)
                )
                SaveButton(
                    isSaving = saveState is CameraViewModel.SaveState.Saving,
                    onClick = { onSave(rawDigits.toLongOrNull()?.toDouble() ?: 0.0) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Footer ──
        Row(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onShowHistory() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Lịch sử chi tiêu", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CircularIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    useThemeColor: Boolean = false
) {
    val iconTint = if (useThemeColor) MaterialTheme.colorScheme.primary else tint
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = iconTint, modifier = Modifier.size(24.dp))
    }
}

// Shutter button: vòng ngoài màu theme, trong trắng
@Composable
private fun ShutterButton(onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(primaryColor)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(66.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(primaryColor)
            .clickable(enabled = !isSaving, indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSaving) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        } else {
            Icon(imageVector = Icons.Filled.Check, contentDescription = "Lưu ảnh", tint = Color.White, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun PhotoHistorySheet(
    transactions: List<TransactionEntity>,
    onSaveToAlbum: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Lịch sử chi tiêu bằng ảnh",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chưa có ảnh chi tiêu nào", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    PhotoHistoryItem(
                        tx = tx,
                        timeFmt = timeFmt,
                        onSaveToAlbum = { onSaveToAlbum(tx) },
                        onDelete = { onDelete(tx) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PhotoHistoryItem(
    tx: TransactionEntity,
    timeFmt: SimpleDateFormat,
    onSaveToAlbum: () -> Unit,
    onDelete: () -> Unit
) {
    var bitmap by remember(tx.imagePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(tx.imagePath) {
        val path = tx.imagePath ?: return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1:1 photo thumbnail
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.Center).size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (tx.amount > 0.0) "-${formatVndDigits(tx.amount.toLong().toString())}đ"
                       else "0đ",
                color = Color(0xFFFF6B6B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFmt.format(java.util.Date(tx.transactionDate)),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        }

        // Action buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onSaveToAlbum, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Filled.Download, contentDescription = "Lưu về máy", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Xóa", tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoCaptured: (Uri) -> Unit
) {
    val capture = imageCapture ?: return
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )
    capture.takePicture(
        ImageCapture.OutputFileOptions.Builder(photoFile).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhotoCaptured(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) { exception.printStackTrace() }
        }
    )
}
