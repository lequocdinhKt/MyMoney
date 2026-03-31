package com.example.mymoney.ui.addtransaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.addtransaction.AddTransactionViewModel
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionNavEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionUiState
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatMessage
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatSender
import com.example.mymoney.ui.theme.MyMoneyTheme

// ─────────────────────────────────────────────────────────────────────────────
// AIChatScreen — Màn hình chat giữa người dùng và AI để thêm giao dịch
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Màn hình chat AI — entry point có ViewModel.
 * Người dùng nhắn "bữa tối 20k" → AI phản hồi và thêm giao dịch tự động.
 */
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is AddTransactionNavEvent.NavigateBack -> onNavigateBack()
                is AddTransactionNavEvent.NavigateToParseSettings -> { /* TODO */ }
            }
        }
    }

    AIChatContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
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
    modifier: Modifier = Modifier
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
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEvent(AddTransactionEvent.OnParseSettingsClicked) }) {
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
                        .padding(bottom = 12.dp),
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
    onEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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

        Spacer(modifier = Modifier.height(8.dp))

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

        // ── Hàng icon: Camera + Mic + Settings ──
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { onEvent(AddTransactionEvent.OnCameraClicked) }) {
                Icon(Icons.Default.CameraAlt, "Chụp hoá đơn",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { onEvent(AddTransactionEvent.OnMicClicked) }) {
                Icon(Icons.Default.Mic, "Nhập bằng giọng nói",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { onEvent(AddTransactionEvent.OnParseSettingsClicked) }) {
                Icon(Icons.Default.Settings, "Cài đặt phân tích",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
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
            ChatBubble(message = message)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChatBubble — Bong bóng chat: AI bên trái, User bên phải
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
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
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            onNavigateBack = {}
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
            onNavigateBack = {}
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
            onNavigateBack = {}
        )
    }
}


