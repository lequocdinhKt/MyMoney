package com.example.mymoney.ui.addtransaction

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
// rememberSaveable not needed after persisting via DataStore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.addtransaction.AddTransactionViewModel
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionNavEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionUiState
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatMessage
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatSender
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.VoiceRecordingState
import com.example.mymoney.ui.theme.MyMoneyTheme

// Chat tone options for chatbot
enum class ChatTone { FRIENDLY, STERN }

// ─────────────────────────────────────────────────────────────────────────────
// AIChatScreen — Màn hình chat giữa người dùng và AI để thêm giao dịch
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Màn hình chat AI — entry point có ViewModel.
 * Người dùng nhắn "bữa tối 20k" → AI phản hồi và thêm giao dịch tự động.
 *
 * @param walletId  ID ví đang active trên HomeScreen (0L = fallback về ví mặc định)
 */
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToRecurring: (walletId: Long) -> Unit = {},
    onNavigateToCamera: (walletId: Long) -> Unit = {},
    registerCameraResultListener: (((String) -> Unit) -> Unit) = {},
    walletId: Long = 0L,
) {
    val context = LocalContext.current
    val viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.factory(context, walletId)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Chatbot tone persisted in DataStore
    val prefs = com.example.mymoney.data.local.datastore.SettingPreferences(context)
    val chatToneName by prefs.chatTone.collectAsState(initial = "FRIENDLY")
    val chatTone = try { ChatTone.valueOf(chatToneName) } catch (_: Exception) { ChatTone.FRIENDLY }
    var showToneSettings by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is AddTransactionNavEvent.NavigateBack -> onNavigateBack()
                is AddTransactionNavEvent.NavigateToParseSettings -> { /* TODO */ }
                is AddTransactionNavEvent.NavigateToRecurring -> onNavigateToRecurring(event.walletId)
                is AddTransactionNavEvent.NavigateToCameraCapture -> onNavigateToCamera(event.walletId)
            }
        }
    }

    // Đăng ký listener để nhận kết quả OCR trả về từ màn hình Camera (qua savedStateHandle)
    LaunchedEffect(Unit) {
        registerCameraResultListener { text ->
            viewModel.onEvent(AddTransactionEvent.OnOcrResult(text))
        }
    }

    // Wrap content in a Box to overlay a small settings icon at top-left
    Box(modifier = modifier.fillMaxSize()) {
        AIChatContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            onOpenChatSettings = { showToneSettings = true },
            modifier = Modifier.fillMaxSize(),
            chatTone = chatTone,
        )

        // Small back icon at top-left (below title to avoid overlap)
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 56.dp)
                .statusBarsPadding()
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

                if (showToneSettings) {
                    ChatToneSettingsDialog(
                        current = chatTone,
                        onDismiss = { showToneSettings = false },
                        onSelect = { tone ->
                            // persist selection
                            coroutineScope.launch { prefs.setChatTone(tone.name) }
                            showToneSettings = false
                        }
                    )
                }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Content — Layout chính: TopBar cố định + Chat list + Bottom input
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIChatContent(
    uiState: AddTransactionUiState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenChatSettings: () -> Unit,
    modifier: Modifier = Modifier,
    chatTone: ChatTone = ChatTone.FRIENDLY
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── TopBar CỐ ĐỊNH — nằm ngoài bất kỳ IME inset nào ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Chat với AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = { /* handled by overlay back button */ },
                    actions = {
                        IconButton(onClick = onOpenChatSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // ── Wallet chip ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = uiState.walletName,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ── Thông báo tự xóa tin nhắn sau 48h ──
                Text(
                    text = "🕐 Tin nhắn sẽ tự xóa sau 48 giờ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }

        // ── Phần content dưới TopBar: imePadding() ở đây ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
        ) {
            // ── Vùng chat co giãn ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.isEmpty -> {
                        // Trạng thái trống — hướng dẫn người dùng
                        EmptyChatState(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        ChatMessageList(
                            messages = uiState.messages,
                            chatTone = chatTone,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // ── Bottom Input Card ──
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomInputCard(
                    noteInput = uiState.noteInput,
                    voiceState = uiState.voiceState,
                    isVoicePlaying = uiState.isVoicePlaying,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EmptyChatState — Trạng thái chưa có tin nhắn
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Text(
            text = "Xin chào! Tôi là trợ lý AI 🤖",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Hãy nhắn cho tôi giao dịch của bạn.\nVí dụ: \"Bữa tối 20k\" hoặc \"Lương tháng 10tr\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BottomInputCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomInputCard(
    noteInput: String,
    voiceState: VoiceRecordingState,
    isVoicePlaying: Boolean,
    onEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Launcher yêu cầu quyền RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Người dùng có thể thử nhấn giữ mic lại sau khi cấp quyền */ }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // ── Chips ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AssistChip(
                onClick = { onEvent(AddTransactionEvent.OnTransferFundClicked) },
                label = { Text("Di chuyển quỹ", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            AssistChip(
                onClick = { onEvent(AddTransactionEvent.OnRecurringClicked) },
                label = { Text("Giao dịch định kỳ", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Thanh phát lại / nhận dạng (hiện khi PROCESSING hoặc RECORDED) ──
        VoicePlaybackBar(
            voiceState = voiceState,
            isVoicePlaying = isVoicePlaying,
            onPlayback = { onEvent(AddTransactionEvent.OnVoicePlayback) },
            onCancel   = { onEvent(AddTransactionEvent.OnVoiceCancel) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── TextField + Submit FAB cùng hàng ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = noteInput,
                onValueChange = { onEvent(AddTransactionEvent.OnNoteChanged(it)) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Bữa tối 20k, cà phê 35k...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            FloatingActionButton(
                onClick = { onEvent(AddTransactionEvent.OnSubmitClicked) },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, "Gửi tin nhắn", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Hàng icon: Camera + Mic (nhấn giữ) + Settings ──
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { onEvent(AddTransactionEvent.OnCameraClicked) }) {
                Icon(Icons.Default.CameraAlt, "Chụp hoá đơn",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }

            // ── Nút Mic — nhấn giữ để ghi âm ──
            HoldToRecordMicButton(
                voiceState = voiceState,
                context = context,
                permissionLauncher = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onEvent = onEvent
            )

            IconButton(onClick = { onEvent(AddTransactionEvent.OnParseSettingsClicked) }) {
                Icon(Icons.Default.Settings, "Cài đặt phân tích",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HoldToRecordMicButton — Nhấn giữ để ghi, thả để nhận dạng
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HoldToRecordMicButton(
    voiceState: VoiceRecordingState,
    context: android.content.Context,
    permissionLauncher: () -> Unit,
    onEvent: (AddTransactionEvent) -> Unit
) {
    // Giữ tham chiếu mới nhất mà không reset pointerInput
    val currentVoiceState = rememberUpdatedState(voiceState)
    val currentOnEvent    = rememberUpdatedState(onEvent)
    val currentPermLauncher = rememberUpdatedState(permissionLauncher)

    // Animation pulse khi đang ghi
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val isRecording = voiceState == VoiceRecordingState.RECORDING
    val isProcessing = voiceState == VoiceRecordingState.PROCESSING

    val bgColor by animateColorAsState(
        targetValue = when {
            isRecording -> MaterialTheme.colorScheme.errorContainer
            else        -> Color.Transparent
        },
        label = "mic_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            isRecording  -> MaterialTheme.colorScheme.error
            isProcessing -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            else         -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "mic_tint"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(if (isRecording) pulseScale else 1f)
            .clip(CircleShape)
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val state = currentVoiceState.value
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        when {
                            !hasPermission -> {
                                tryAwaitRelease()
                                currentPermLauncher.value()
                            }
                            state == VoiceRecordingState.IDLE -> {
                                currentOnEvent.value(AddTransactionEvent.OnMicPressStart)
                                tryAwaitRelease()
                                currentOnEvent.value(AddTransactionEvent.OnMicPressEnd)
                            }
                            else -> tryAwaitRelease() // tiêu thụ gesture, không làm gì
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isRecording) "Đang ghi..." else "Nhấn giữ để ghi âm",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VoicePlaybackBar — Thanh nghe lại và hủy (hiện khi PROCESSING / RECORDED)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VoicePlaybackBar(
    voiceState: VoiceRecordingState,
    isVoicePlaying: Boolean,
    onPlayback: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = voiceState == VoiceRecordingState.PROCESSING ||
                    voiceState == VoiceRecordingState.RECORDED

    AnimatedVisibility(visible = isVisible, modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (voiceState == VoiceRecordingState.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "🎙️ Đang nhận dạng giọng nói...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Nút Play / Stop
                    IconButton(
                        onClick = onPlayback,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isVoicePlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isVoicePlaying) "Dừng phát" else "Nghe lại",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = if (isVoicePlaying) "Đang phát..." else "Nghe lại giọng nói",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    // Nút hủy
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hủy ghi âm",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChatMessageList — Danh sách tin nhắn dạng chat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatMessageList(
    messages: List<ChatMessage>,
    chatTone: ChatTone = ChatTone.FRIENDLY,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll xuống tin nhắn mới nhất
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 16.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp)
    ) {
        items(items = messages.reversed(), key = { it.id }) { message ->
            ChatBubble(message = message, chatTone = chatTone)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChatBubble — Bong bóng chat: AI bên trái, User bên phải
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    chatTone: ChatTone = ChatTone.FRIENDLY,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == ChatSender.USER

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // ── Avatar AI — bên trái ──
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // ── Bong bóng tin nhắn — tối đa 75% chiều rộng ──
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            val displayText = if (isUser) message.content else applyToneTransformation(message.content, chatTone)
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatToneSettingsDialog(
    current: ChatTone,
    onDismiss: () -> Unit,
    onSelect: (ChatTone) -> Unit
) {
    val selected = remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cài đặt Chatbot") },
        text = {
            Column {
                Text("Chọn phong cách phản hồi của AI:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected.value == ChatTone.FRIENDLY, onClick = { selected.value = ChatTone.FRIENDLY })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thân thiện – dùng emoji, ngôn ngữ mềm mại")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected.value == ChatTone.STERN, onClick = { selected.value = ChatTone.STERN })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nghiêm khắc – ngắn gọn, cảnh báo khi cần")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Xem trước:")
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = applyToneTransformation("Đã ghi nhận! • Bữa tối: -20,000đ", selected.value),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected.value) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}

// Simple tone transformer — heuristic rules to show different styles without changing backend AI
private fun applyToneTransformation(text: String, tone: ChatTone): String {
    return when (tone) {
        ChatTone.FRIENDLY -> {
            // ensure friendly emojis and softening phrases
            var t = text
            if (!t.contains("🤖") && !t.contains("🎉")) {
                t = t + " 🎉"
            }
            t.replace("⚠️", "")
        }
        ChatTone.STERN -> {
            // remove emojis, make concise and add a warning prefix when it contains "Đã lưu" or "Đã ghi nhận"
            var t = text.replace(Regex("[\\p{So}\\p{C}]"), "") // remove symbols/emojis
            t = t.replace("Đã ghi nhận", "Được ghi nhận").trim()
            if (t.contains("Đã lưu") || t.contains("Được ghi nhận") || t.contains("Đã ghi")) {
                t = "⚠️ Lưu ý: " + t
            }
            t
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIChatEmptyPreview() {
    MyMoneyTheme(darkTheme = false) {
        AIChatContent(
            uiState = AddTransactionUiState(isLoading = false, isEmpty = true),
            onEvent = {},
            onNavigateBack = {},
            onOpenChatSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIChatWithMessagesPreview() {
    MyMoneyTheme(darkTheme = false) {
        AIChatContent(
            uiState = AddTransactionUiState(
                isLoading = false,
                isEmpty = false,
                messages = listOf(
                    ChatMessage(id = 1, content = "Bữa tối 20k", sender = ChatSender.USER),
                    ChatMessage(id = 2, content = "Oh tuyệt vời! Bữa tối chỉ có 20k thôi á 🎉\nMình đã thêm giao dịch:\n• Bữa tối: -20,000đ\nvào ví chính cho bạn rồi nhé!", sender = ChatSender.AI),
                    ChatMessage(id = 3, content = "Cà phê sáng 35k", sender = ChatSender.USER),
                    ChatMessage(id = 4, content = "Đã ghi nhận ☕\n• Cà phê sáng: -35,000đ\nTổng chi hôm nay: 55,000đ", sender = ChatSender.AI),
                    ChatMessage(id = 5, content = "Lương tháng 10tr", sender = ChatSender.USER),
                    ChatMessage(id = 6, content = "Wow xin chúc mừng! 💰\n• Lương tháng: +10,000,000đ\nĐã thêm vào mục Thu nhập.", sender = ChatSender.AI),
                )
            ),
            onEvent = {},
            onNavigateBack = {},
            onOpenChatSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIChatDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        AIChatContent(
            uiState = AddTransactionUiState(
                isLoading = false,
                isEmpty = false,
                messages = listOf(
                    ChatMessage(id = 1, content = "Mua sắm 400k", sender = ChatSender.USER),
                    ChatMessage(id = 2, content = "Đã ghi nhận 🛍️\n• Mua sắm: -400,000đ\nCẩn thận chi tiêu nhé!", sender = ChatSender.AI),
                )
            ),
            onEvent = {},
            onNavigateBack = {},
            onOpenChatSettings = {}
        )
    }
}


