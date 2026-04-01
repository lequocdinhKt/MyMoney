package com.example.mymoney.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.setting.SettingViewModel
import com.example.mymoney.ui.setting.SettingScreen

/**
 * Drawer overlay tự viết — thay thế ModalNavigationDrawer của Material3.
 *
 * Tại sao không dùng ModalNavigationDrawer:
 *   Material3 ModalNavigationDrawer luôn compose drawerContent ngay frame đầu tiên
 *   → gây flash drawer khi app khởi động dù drawer đang đóng.
 *
 * Tại sao SettingViewModel được tạo ở đây (ngoài AnimatedVisibility):
 *   AnimatedVisibility add/remove SettingScreen khỏi composition mỗi lần mở/đóng.
 *   Nếu ViewModel tạo bên trong SettingScreen → bị recreate mỗi lần → loadUsername()
 *   gọi lại Supabase network → UI bị delay hoặc treo.
 *   Giải pháp: tạo ViewModel ở đây, truyền xuống → ViewModel sống cùng MainScreen.
 *
 * @param isOpen      true = drawer đang mở
 * @param onClose     callback khi user đóng drawer (nhấn scrim hoặc item)
 * @param onSignOut   callback khi đăng xuất thành công → navigate về SignIn
 */
@Composable
fun MainDrawerOverlay(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    // ViewModel tạo ở đây — ngoài AnimatedVisibility
    // → tồn tại suốt vòng đời MainScreen, loadUsername() chỉ chạy 1 lần
    val context = LocalContext.current
    val settingViewModel: SettingViewModel = viewModel(
        factory = SettingViewModel.factory(context)
    )

    AnimatedVisibility(
        visible = isOpen,
        // Chỉ fade in/out scrim — không trượt
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        // Scrim — nền tối phía sau drawer, nhấn để đóng
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )
    }

    // Drawer content trượt độc lập — KHÔNG bị scrim che
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SettingScreen(
                    onItemClick = onClose,
                    onSignOut = onSignOut,
                    viewModel = settingViewModel
                )
            }
        }
    }
}
