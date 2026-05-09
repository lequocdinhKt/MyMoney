package com.example.mymoney.ui.components.shimmer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mymoney.common.UiState

/**
 * Container tự động toggle giữa **Skeleton ↔ Content ↔ Error** dựa trên [UiState].
 * Dùng [AnimatedContent] để crossfade mượt (300ms) giữa các trạng thái.
 *
 * ## Dành cho ViewModel extend [com.example.mymoney.presentation.base.BaseViewModel]:
 * ```kotlin
 * val uiState by viewModel.uiState.collectAsState()
 *
 * UiStateContainer(
 *     uiState  = uiState,
 *     skeleton = { MySkeletonScreen() },
 *     content  = { data -> MyContent(data, onEvent) }
 * )
 * ```
 *
 * @param uiState  Trạng thái từ ViewModel
 * @param skeleton Composable skeleton (shimmer) – hiển thị khi [UiState.Loading]
 * @param content  Composable nội dung thật – hiển thị khi [UiState.Success]
 * @param error    Composable lỗi – mặc định text đơn giản khi [UiState.Error]
 * @param modifier Modifier áp dụng lên Box bọc ngoài
 */
@Composable
fun <T> UiStateContainer(
    uiState:  UiState<T>,
    skeleton: @Composable () -> Unit,
    content:  @Composable (data: T) -> Unit,
    modifier: Modifier = Modifier,
    error:    @Composable (message: String) -> Unit = { message ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text  = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
) {
    AnimatedContent(
        targetState   = uiState,
        modifier      = modifier,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        contentKey    = { state ->
            when (state) {
                is UiState.Loading    -> 0
                is UiState.Success<*> -> 1
                is UiState.Error      -> 2
            }
        },
        label = "UiStateContainer"
    ) { state ->
        @Suppress("UNCHECKED_CAST")
        when (state) {
            is UiState.Loading    -> skeleton()
            is UiState.Success<*> -> content(state.data as T)
            is UiState.Error      -> error(state.message)
        }
    }
}

/**
 * Overload tiện lợi cho ViewModel dùng `Boolean isLoading` (không extend BaseViewModel).
 * Cho phép tích hợp từng bước mà **không cần** refactor ViewModel hiện có.
 *
 * ## Dành cho HomeScreen (HomeUiState dùng isLoading: Boolean nội tuyến):
 * ```kotlin
 * UiStateContainer(
 *     isLoading = uiState.isLoading,
 *     modifier  = modifier.fillMaxSize(),
 *     skeleton  = { HomeSkeletonScreen() }
 * ) {
 *     HomeRealContent(uiState, onEvent)
 * }
 * ```
 *
 * @param isLoading `true` → hiện skeleton, `false` → hiện content
 * @param skeleton  Composable skeleton hiển thị khi loading
 * @param content   Composable nội dung thật hiển thị khi không loading
 * @param modifier  Modifier áp dụng lên container
 */
@Composable
fun UiStateContainer(
    isLoading: Boolean,
    skeleton:  @Composable () -> Unit,
    modifier:  Modifier = Modifier,
    content:   @Composable () -> Unit
) {
    AnimatedContent(
        targetState   = isLoading,
        modifier      = modifier,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label         = "UiStateContainerBoolean"
    ) { loading ->
        if (loading) skeleton() else content()
    }
}

